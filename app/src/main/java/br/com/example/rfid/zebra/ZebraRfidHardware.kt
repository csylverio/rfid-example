package br.com.example.rfid.zebra

import android.content.Context
import android.util.Log
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.MEMORY_BANK
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents

class ZebraRfidHardware(private val context: Context) : RfidHardware, RfidEventsListener {
    private var readers: Readers? = null
    private var reader: RFIDReader? = null
    private var tagReadListener: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "ZebraRfidHardware"
    }

    override fun setTagReadListener(listener: (String) -> Unit) {
        tagReadListener = listener
    }

    override fun connect(): String? {
        Log.d(TAG, "connect() chamado")
        readers = Readers(context, ENUM_TRANSPORT.SERVICE_USB)

        val availableReaders = readers?.GetAvailableRFIDReaderList()
        if (availableReaders.isNullOrEmpty()) {
            return null
        }

        val device = availableReaders.first()
        reader = device.rfidReader

        Log.d(TAG, "Conectando ao reader: ${device.name}")
        reader?.connect()

        reader?.Events?.apply {
            setTagReadEvent(true)
            setAttachTagDataWithReadEvent(false)
            setReaderDisconnectEvent(true)
            setBatteryEvent(true)
            addEventsListener(this@ZebraRfidHardware)
        }

        return device.name
    }

    override fun disconnect() {
        try {
            Log.d(TAG, "disconnect() chamado")
            reader?.Events?.removeEventsListener(this@ZebraRfidHardware)
            reader?.disconnect()
        } finally {
            reader = null
            readers?.Dispose()
            readers = null
        }
    }

    override fun isConnected(): Boolean = reader?.isConnected == true

    override fun startInventory() {
        Log.d(TAG, "startInventory() chamado")
        reader?.Actions?.Inventory?.perform()
    }

    override fun stopInventory() {
        Log.d(TAG, "stopInventory() chamado")
        reader?.Actions?.Inventory?.stop()
    }

    override fun getReadTags(limit: Int): List<String> {
        val tags = reader?.Actions?.getReadTags(limit)
            ?.mapNotNull { tag -> tag.tagID?.takeIf { it.isNotBlank() } }
            ?: emptyList()
        if (tags.isNotEmpty()) {
            Log.d(TAG, "Tags lidas: ${tags.size}")
        }
        return tags
    }

    override fun readBank(tagId: String, bank: MEMORY_BANK): String? {
        val tagAccess = reader?.Actions?.TagAccess ?: return null
        return try {
            Log.d(TAG, "readBank() tag=$tagId bank=$bank")
            val params = tagAccess.ReadAccessParams()
            params.setMemoryBank(bank)
            params.setAccessPassword(0L)
            params.setOffset(0)
            params.setCount(0)

            tagAccess.readWait(tagId, params, null)?.memoryBankData
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao ler banco de memória: ${e.message}", e)
            null
        }
    }

    override fun writeEpc(tagId: String, epc: String) {
        val tagAccess = reader?.Actions?.TagAccess ?: return
        try {
            Log.d(TAG, "writeEpc() tag=$tagId epc=$epc")
            val writeAccessParams = tagAccess.WriteAccessParams()
            writeAccessParams.setAccessPassword(0L)
            writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC)
            writeAccessParams.setOffset(2)
            writeAccessParams.setWriteData(epc)

            tagAccess.writeWait(tagId, writeAccessParams, null, null)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao escrever EPC: ${e.message}", e)
            throw e
        }
    }

    override fun eventReadNotify(e: RfidReadEvents?) {
        val tags = reader?.Actions?.getReadTags(100) ?: return
        if (tags.isNotEmpty()) {
            Log.d(TAG, "eventReadNotify() tags=${tags.size}")
        }
        tags.forEach { tag ->
            val epc = tag.tagID
            if (!epc.isNullOrBlank()) {
                tagReadListener?.invoke(epc)
            }
        }
    }

    override fun eventStatusNotify(e: RfidStatusEvents?) {
    }
}
