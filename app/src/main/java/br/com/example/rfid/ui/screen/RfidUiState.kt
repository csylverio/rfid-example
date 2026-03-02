package br.com.example.rfid.ui.screen

import androidx.compose.runtime.Immutable

@Immutable
data class RfidUiState(
    val status: String,
    val connected: Boolean,
    val inventoryRunning: Boolean,
    val epcs: List<String>
) {
    val canConnect: Boolean
        get() = !connected

    val canDisconnect: Boolean
        get() = connected

    val canStartInventory: Boolean
        get() = connected && !inventoryRunning

    val canStopInventory: Boolean
        get() = connected && inventoryRunning

    val canOpenTagDetails: Boolean
        get() = connected

    val canEncode: Boolean
        get() = connected

    val epcCount: Int
        get() = epcs.size
}
