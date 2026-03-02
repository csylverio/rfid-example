package br.com.example.rfid.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InventoryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEpcsAndInventoryControls() {
        val state = RfidUiState(
            status = "Conectado",
            connected = true,
            inventoryRunning = false,
            epcs = listOf("EPC1", "EPC2")
        )
        var startClicked = false
        var backClicked = false

        composeRule.setContent {
            InventoryScreen(
                state = state,
                onStartInventory = { startClicked = true },
                onStopInventory = {},
                onClear = {},
                onBack = { backClicked = true }
            )
        }

        composeRule.onNodeWithText("EPCs lidos: 2").assertExists()
        composeRule.onNodeWithText("EPC1").assertExists()
        composeRule.onNodeWithText("EPC2").assertExists()

        composeRule.onNodeWithText("Start Inventory").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Stop Inventory").assertIsNotEnabled()
        composeRule.onNodeWithText("Voltar").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertTrue(startClicked)
            assertTrue(backClicked)
        }
    }

    @Test
    fun runningInventory_enablesStopButton() {
        val state = RfidUiState(
            status = "Inventário",
            connected = true,
            inventoryRunning = true,
            epcs = emptyList()
        )
        var stopClicked = false

        composeRule.setContent {
            InventoryScreen(
                state = state,
                onStartInventory = {},
                onStopInventory = { stopClicked = true },
                onClear = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("Stop Inventory").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Start Inventory").assertIsNotEnabled()

        composeRule.runOnIdle {
            assertTrue(stopClicked)
        }
    }
}
