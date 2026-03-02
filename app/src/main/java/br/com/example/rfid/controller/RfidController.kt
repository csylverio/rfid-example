package br.com.example.rfid.controller

import android.util.Log
import com.zebra.rfid.api3.MEMORY_BANK
import br.com.example.rfid.model.TagDetails
import br.com.example.rfid.zebra.RfidHardware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class RfidController(
    private val hardware: RfidHardware,
    private val scope: CoroutineScope,
    private val onStatus: (String) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onInventoryChanged: (Boolean) -> Unit,
    private val onTagRead: (String) -> Unit,
    private val onTagDetails: (TagDetails) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    private var isConnected = false
    private var isInventoryRunning = false
    private var lastSeenTagId: String? = null

    companion object {
        private const val TAG = "RfidController"
    }

    init {
        hardware.setTagReadListener { epc ->
            lastSeenTagId = epc
            postTag(epc)
        }
    }

    /**
     * Conecta no primeiro reader encontrado.
     * Para produção: você vai listar e deixar o usuário escolher.
     */
    fun connect() {
        scope.launch(ioDispatcher) {
            try {
                Log.d(TAG, "connect() chamado")
                postStatus("Procurando readers...")
                val readerName = hardware.connect()
                if (readerName.isNullOrBlank()) {
                    Log.d(TAG, "Nenhum reader encontrado")
                    updateConnected(false)
                    postStatus("Nenhum reader encontrado (Bluetooth).")
                    return@launch
                }

                Log.d(TAG, "Conectado ao reader: $readerName")
                updateConnected(true)
                postStatus("Conectado: $readerName")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao conectar: ${e.message}", e)
                updateConnected(false)
                postStatus("Falha ao conectar: ${e.message}")
            }
        }
    }

    fun disconnect() {
        scope.launch(ioDispatcher) {
            try {
                Log.d(TAG, "disconnect() chamado")
                stopInventoryInternal()
                hardware.disconnect()

                updateConnected(false)
                postStatus("Desconectado")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao desconectar: ${e.message}", e)
                postStatus("Falha ao desconectar: ${e.message}")
            }
        }
    }

    fun startInventory() {
        scope.launch(ioDispatcher) {
            try {
                Log.d(TAG, "startInventory() chamado")
                if (!hardware.isConnected()) {
                    postStatus("Reader não está conectado.")
                    return@launch
                }

                hardware.startInventory()
                Log.d(TAG, "Inventário iniciado")
                updateInventoryRunning(true)
                postStatus("Inventário rodando...")
            } catch (e: Exception) {
                Log.e(TAG, "Falha no inventário: ${e.message}", e)
                postStatus("Falha no inventário: ${e.message}")
            }
        }
    }

    fun stopInventory() {
        scope.launch(ioDispatcher) {
            Log.d(TAG, "stopInventory() chamado")
            stopInventoryInternal()
        }
    }

    private fun stopInventoryInternal() {
        try {
            hardware.stopInventory()
            updateInventoryRunning(false)
            if (isConnected) postStatus("Conectado (parado)")
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao parar inventário: ${e.message}", e)
        }
    }

    fun readTagDetails() {
        scope.launch(ioDispatcher) {
            Log.d(TAG, "readTagDetails() chamado")
            if (!hardware.isConnected()) {
                postStatus("Reader não conectado.")
                return@launch
            }

            postStatus("Aproxime uma tag para leitura...")

            val tagId = resolveTagId()
            if (tagId.isNullOrBlank()) {
                postStatus("Nenhuma tag encontrada.")
                return@launch
            }
            Log.d(TAG, "Lendo detalhes para tag: $tagId")
            val details = TagDetails(
                tagId = tagId,
                mb01 = readBank(tagId, MEMORY_BANK.MEMORY_BANK_RESERVED),
                mb02 = readBank(tagId, MEMORY_BANK.MEMORY_BANK_EPC),
                mb03 = readBank(tagId, MEMORY_BANK.MEMORY_BANK_TID),
                mb04 = readBank(tagId, MEMORY_BANK.MEMORY_BANK_USER)
            )

            postTagDetails(details)
            postStatus("Detalhes lidos para $tagId")
        }
    }

    fun encodeTag(epcInput: String) {
        scope.launch(ioDispatcher) {
            Log.d(TAG, "encodeTag() chamado")
            if (!hardware.isConnected()) {
                postStatus("Reader não conectado.")
                return@launch
            }

            val epc = sanitizeEpc(epcInput)
            if (epc.isBlank()) {
                postStatus("Informe um EPC para codificar.")
                return@launch
            }
            if (!epc.matches(Regex("^[0-9A-F]+$"))) {
                postStatus("EPC inválido: use apenas hexadecimal.")
                return@launch
            }
            if (epc.length % 4 != 0) {
                postStatus("EPC inválido: tamanho deve ser múltiplo de 4.")
                return@launch
            }

            Log.d(TAG, "Codificando EPC: $epc")

            if (isInventoryRunning) {
                stopInventoryInternal()
            }

            postStatus("Aproxime uma tag para codificação...")

            val tagId = resolveTagId()
            if (tagId.isNullOrBlank()) {
                postStatus("Nenhuma tag encontrada.")
                return@launch
            }

            Log.d(TAG, "Escrevendo EPC na tag: $tagId")

            try {
                hardware.writeEpc(tagId, epc)
                lastSeenTagId = epc
                postTag(epc)
                postStatus("Tag codificada: $epc")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao codificar tag: ${e.message}", e)
                postStatus("Falha ao codificar: ${e.message}")
            }
        }
    }

    private fun updateConnected(connected: Boolean) {
        if (isConnected == connected) return
        isConnected = connected
        scope.launch(mainDispatcher) {
            onConnectionChanged(connected)
        }
        if (!connected) {
            updateInventoryRunning(false)
        }
    }

    private fun updateInventoryRunning(running: Boolean) {
        if (isInventoryRunning == running) return
        isInventoryRunning = running
        scope.launch(mainDispatcher) {
            onInventoryChanged(running)
        }
    }

    private fun postStatus(text: String) {
        scope.launch(mainDispatcher) {
            onStatus(text)
        }
    }

    private fun postTag(epc: String) {
        scope.launch(mainDispatcher) {
            onTagRead(epc)
        }
    }

    private fun postTagDetails(details: TagDetails) {
        scope.launch(mainDispatcher) {
            onTagDetails(details)
        }
    }

    private suspend fun resolveTagId(): String? {
        Log.d(TAG, "resolveTagId() iniciado")
        val buffered = readTagIdFromBuffer()
        if (!buffered.isNullOrBlank()) {
            Log.d(TAG, "Tag encontrada no buffer: $buffered")
            return buffered
        }

        if (!isInventoryRunning) {
            try {
                hardware.startInventory()
                delay(300)
                hardware.stopInventory()
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao iniciar inventário temporário: ${e.message}", e)
            }

            val afterInventory = readTagIdFromBuffer()
            if (!afterInventory.isNullOrBlank()) {
                Log.d(TAG, "Tag encontrada após inventário: $afterInventory")
                return afterInventory
            }
        }

        if (!lastSeenTagId.isNullOrBlank()) {
            Log.d(TAG, "Tag encontrada no último EPC: $lastSeenTagId")
        }
        return lastSeenTagId
    }

    private fun readTagIdFromBuffer(): String? {
        return hardware.getReadTags(1)
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun sanitizeEpc(raw: String): String {
        return raw.replace("\\s".toRegex(), "").uppercase(Locale.US)
    }

    private fun readBank(tagId: String, bank: MEMORY_BANK): String? {
        return try {
            hardware.readBank(tagId, bank)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao ler banco de memória: ${e.message}", e)
            null
        }
    }
}
