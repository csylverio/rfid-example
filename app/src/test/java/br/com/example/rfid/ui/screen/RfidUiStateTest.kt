package br.com.example.rfid.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RfidUiStateTest {
    @Test
    fun disconnectedState_disablesInventoryActions() {
        val state = RfidUiState(
            status = "Desconectado",
            connected = false,
            inventoryRunning = false,
            epcs = emptyList()
        )

        assertTrue(state.canConnect)
        assertFalse(state.canDisconnect)
        assertFalse(state.canStartInventory)
        assertFalse(state.canStopInventory)
        assertFalse(state.canOpenTagDetails)
    }

    @Test
    fun connectedIdleState_allowsStartInventory() {
        val state = RfidUiState(
            status = "Conectado",
            connected = true,
            inventoryRunning = false,
            epcs = emptyList()
        )

        assertFalse(state.canConnect)
        assertTrue(state.canDisconnect)
        assertTrue(state.canStartInventory)
        assertFalse(state.canStopInventory)
        assertTrue(state.canOpenTagDetails)
    }

    @Test
    fun inventoryRunningState_allowsStopInventory() {
        val state = RfidUiState(
            status = "Inventário",
            connected = true,
            inventoryRunning = true,
            epcs = emptyList()
        )

        assertFalse(state.canStartInventory)
        assertTrue(state.canStopInventory)
    }

    @Test
    fun epcCount_reflectsListSize() {
        val state = RfidUiState(
            status = "Ok",
            connected = true,
            inventoryRunning = false,
            epcs = listOf("EPC1", "EPC2")
        )

        assertEquals(2, state.epcCount)
    }
}
