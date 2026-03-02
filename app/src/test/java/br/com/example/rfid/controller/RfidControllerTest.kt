package br.com.example.rfid.controller

import com.zebra.rfid.api3.MEMORY_BANK
import br.com.example.rfid.model.TagDetails
import br.com.example.rfid.zebra.RfidHardware
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RfidControllerTest {
    @Test
    fun connectWithReader_updatesStatusAndConnection() {
        val hardware = FakeRfidHardware().apply { connectResult = "Reader-1" }
        val statusUpdates = mutableListOf<String>()
        val statusLatch = CountDownLatch(2)
        val connectionLatch = CountDownLatch(1)
        var connectedState: Boolean? = null

        val controller = createController(
            hardware = hardware,
            onStatus = {
                statusUpdates.add(it)
                statusLatch.countDown()
            },
            onConnectionChanged = {
                connectedState = it
                connectionLatch.countDown()
            }
        )

        controller.connect()

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertTrue(connectionLatch.await(1, TimeUnit.SECONDS))
        assertEquals("Conectado: Reader-1", statusUpdates.last())
        assertEquals(true, connectedState)
    }

    @Test
    fun connectWithoutReaders_postsEmptyStatus() {
        val hardware = FakeRfidHardware().apply { connectResult = null }
        val statusUpdates = mutableListOf<String>()
        val statusLatch = CountDownLatch(2)

        val controller = createController(
            hardware = hardware,
            onStatus = {
                statusUpdates.add(it)
                statusLatch.countDown()
            },
            onConnectionChanged = { }
        )

        controller.connect()

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertEquals("Nenhum reader encontrado (Bluetooth).", statusUpdates.last())
    }

    @Test
    fun startInventoryWithoutConnection_postsStatus() {
        val hardware = FakeRfidHardware().apply { connected = false }
        val statusLatch = CountDownLatch(1)
        var status: String? = null

        val controller = createController(
            hardware = hardware,
            onStatus = {
                status = it
                statusLatch.countDown()
            },
            onConnectionChanged = { }
        )

        controller.startInventory()

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertEquals("Reader não está conectado.", status)
        assertFalse(hardware.inventoryRunning)
    }

    @Test
    fun readTagDetails_emitsBanks() {
        val hardware = FakeRfidHardware().apply {
            connected = true
            readTags = listOf("EPC123")
            bankData[MEMORY_BANK.MEMORY_BANK_RESERVED] = "MB01"
            bankData[MEMORY_BANK.MEMORY_BANK_EPC] = "MB02"
            bankData[MEMORY_BANK.MEMORY_BANK_TID] = "MB03"
            bankData[MEMORY_BANK.MEMORY_BANK_USER] = "MB04"
        }
        val latch = CountDownLatch(1)
        var details: TagDetails? = null

        val controller = createController(
            hardware = hardware,
            onStatus = { },
            onConnectionChanged = { },
            onTagDetails = {
                details = it
                latch.countDown()
            }
        )

        controller.readTagDetails()

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        val result = details
        assertEquals("EPC123", result?.tagId)
        assertEquals("MB01", result?.mb01)
        assertEquals("MB02", result?.mb02)
        assertEquals("MB03", result?.mb03)
        assertEquals("MB04", result?.mb04)
    }

    @Test
    fun readTagDetailsWithoutConnection_postsStatus() {
        val hardware = FakeRfidHardware().apply { connected = false }
        val statusLatch = CountDownLatch(1)
        var status: String? = null

        val controller = createController(
            hardware = hardware,
            onStatus = {
                status = it
                statusLatch.countDown()
            },
            onConnectionChanged = { },
            onTagDetails = { }
        )

        controller.readTagDetails()

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertEquals("Reader não conectado.", status)
    }

    @Test
    fun tagReadListener_updatesCallback() {
        val hardware = FakeRfidHardware()
        val latch = CountDownLatch(1)
        var lastTag: String? = null

        createController(
            hardware = hardware,
            onStatus = { },
            onConnectionChanged = { },
            onTagRead = {
                lastTag = it
                latch.countDown()
            }
        )

        hardware.emitTag("EPC999")

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals("EPC999", lastTag)
    }

    @Test
    fun encodeTag_withValidEpc_writesTag() {
        val hardware = FakeRfidHardware().apply {
            connected = true
            readTags = listOf("OLDTAG")
        }
        val statusUpdates = mutableListOf<String>()
        val statusLatch = CountDownLatch(2)

        val controller = createController(
            hardware = hardware,
            onStatus = {
                statusUpdates.add(it)
                statusLatch.countDown()
            },
            onConnectionChanged = { }
        )

        controller.encodeTag("abcd 1234")

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertEquals("OLDTAG", hardware.lastWriteTagId)
        assertEquals("ABCD1234", hardware.lastWriteEpc)
        assertEquals("Tag codificada: ABCD1234", statusUpdates.last())
    }

    @Test
    fun encodeTag_withInvalidEpc_postsStatus() {
        val hardware = FakeRfidHardware().apply { connected = true }
        val statusLatch = CountDownLatch(1)
        var lastStatus: String? = null

        val controller = createController(
            hardware = hardware,
            onStatus = {
                lastStatus = it
                statusLatch.countDown()
            },
            onConnectionChanged = { }
        )

        controller.encodeTag("ZZ")

        assertTrue(statusLatch.await(1, TimeUnit.SECONDS))
        assertEquals("EPC inválido: use apenas hexadecimal.", lastStatus)
        assertEquals(null, hardware.lastWriteEpc)
    }

    private fun createController(
        hardware: FakeRfidHardware,
        onStatus: (String) -> Unit,
        onConnectionChanged: (Boolean) -> Unit,
        onInventoryChanged: (Boolean) -> Unit = { },
        onTagRead: (String) -> Unit = { },
        onTagDetails: (TagDetails) -> Unit = { }
    ): RfidController {
        return RfidController(
            hardware = hardware,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            onStatus = onStatus,
            onConnectionChanged = onConnectionChanged,
            onInventoryChanged = onInventoryChanged,
            onTagRead = onTagRead,
            onTagDetails = onTagDetails,
            ioDispatcher = Dispatchers.Unconfined,
            mainDispatcher = Dispatchers.Unconfined
        )
    }

    private class FakeRfidHardware : RfidHardware {
        var connectResult: String? = "Reader"
        var connected: Boolean = false
        var inventoryRunning: Boolean = false
        var readTags: List<String> = emptyList()
        val bankData = mutableMapOf<MEMORY_BANK, String?>()
        var lastWriteTagId: String? = null
        var lastWriteEpc: String? = null
        private var tagReadListener: ((String) -> Unit)? = null

        override fun connect(): String? {
            connected = connectResult != null
            return connectResult
        }

        override fun disconnect() {
            connected = false
        }

        override fun isConnected(): Boolean = connected

        override fun startInventory() {
            inventoryRunning = true
        }

        override fun stopInventory() {
            inventoryRunning = false
        }

        override fun getReadTags(limit: Int): List<String> {
            return readTags.take(limit)
        }

        override fun readBank(tagId: String, bank: MEMORY_BANK): String? {
            return bankData[bank]
        }

        override fun writeEpc(tagId: String, epc: String) {
            lastWriteTagId = tagId
            lastWriteEpc = epc
        }

        override fun setTagReadListener(listener: (String) -> Unit) {
            tagReadListener = listener
        }

        fun emitTag(tagId: String) {
            tagReadListener?.invoke(tagId)
        }
    }
}
