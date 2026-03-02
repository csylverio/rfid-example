package br.com.example.rfid.zebra

import com.zebra.rfid.api3.MEMORY_BANK

interface RfidHardware {
    fun connect(): String?
    fun disconnect()
    fun isConnected(): Boolean
    fun startInventory()
    fun stopInventory()
    fun getReadTags(limit: Int): List<String>
    fun readBank(tagId: String, bank: MEMORY_BANK): String?
    fun writeEpc(tagId: String, epc: String)
    fun setTagReadListener(listener: (String) -> Unit)
}
