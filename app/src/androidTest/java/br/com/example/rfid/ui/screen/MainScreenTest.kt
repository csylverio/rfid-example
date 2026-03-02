package br.com.example.rfid.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun disconnectedState_showsButtonsAndDisablesInventory() {
        val state = RfidUiState(
            status = "Desconectado",
            connected = false,
            inventoryRunning = false,
            epcs = emptyList()
        )
        var connectClicked = false

        composeRule.setContent {
            MainScreen(
                state = state,
                onConnect = { connectClicked = true },
                onDisconnect = {},
                onOpenInventory = {},
                onOpenTagDetails = {}
            )
        }

        composeRule.onNodeWithText("Status: Desconectado").assertExists()
        composeRule.onNodeWithText("Conectar").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Desconectar").assertIsNotEnabled()
        composeRule.onNodeWithText("Inventário").assertIsNotEnabled()
        composeRule.onNodeWithText("Tag detalhes").assertIsNotEnabled()

        composeRule.runOnIdle {
            assertTrue(connectClicked)
        }
    }

    @Test
    fun connectedState_enablesInventoryNavigation() {
        val state = RfidUiState(
            status = "Conectado",
            connected = true,
            inventoryRunning = false,
            epcs = emptyList()
        )
        var inventoryClicked = false
        var tagDetailsClicked = false

        composeRule.setContent {
            MainScreen(
                state = state,
                onConnect = {},
                onDisconnect = {},
                onOpenInventory = { inventoryClicked = true },
                onOpenTagDetails = { tagDetailsClicked = true }
            )
        }

        composeRule.onNodeWithText("Inventário").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Tag detalhes").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertTrue(inventoryClicked)
            assertTrue(tagDetailsClicked)
        }
    }
}
