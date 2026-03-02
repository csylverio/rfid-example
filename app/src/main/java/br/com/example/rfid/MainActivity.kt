package br.com.example.rfid

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import br.com.example.rfid.controller.RfidController
import br.com.example.rfid.model.TagDetails
import br.com.example.rfid.ui.screen.InventoryScreen
import br.com.example.rfid.ui.screen.MainScreen
import br.com.example.rfid.ui.screen.EncodeScreen
import br.com.example.rfid.ui.screen.RfidUiState
import br.com.example.rfid.ui.screen.TagDetailsScreen
import br.com.example.rfid.ui.theme.RFIDExampleTheme
import br.com.example.rfid.zebra.ZebraRfidHardware

class MainActivity : ComponentActivity() {

    // ===== UI state =====
    private val epcs = mutableStateListOf<String>()
    private var statusText by mutableStateOf("Desconectado")
    private var isConnected by mutableStateOf(false)
    private var isInventoryRunning by mutableStateOf(false)
    private var currentScreen by mutableStateOf(Screen.MAIN)
    private lateinit var rfidController: RfidController
    private var tagDetails by mutableStateOf<TagDetails?>(null)
    private var encodeEpcInput by mutableStateOf("")

    // Runtime permissions (Android 12+)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestBtPermissionsIfNeeded()

        rfidController = RfidController(
            hardware = ZebraRfidHardware(applicationContext),
            scope = lifecycleScope,
            onStatus = { statusText = it },
            onConnectionChanged = { isConnected = it },
            onInventoryChanged = { isInventoryRunning = it },
            onTagRead = { epcs.add(0, it) },
            onTagDetails = { tagDetails = it }
        )

        setContent {
            RFIDExampleTheme {
                val state = RfidUiState(
                    status = statusText,
                    connected = isConnected,
                    inventoryRunning = isInventoryRunning,
                    epcs = epcs
                )

                when (currentScreen) {
                    Screen.MAIN -> MainScreen(
                        state = state,
                        onConnect = { rfidController.connect() },
                        onDisconnect = { rfidController.disconnect() },
                        onOpenInventory = { currentScreen = Screen.INVENTORY },
                        onOpenTagDetails = { currentScreen = Screen.TAG_DETAILS },
                        onOpenEncode = { currentScreen = Screen.ENCODE }
                    )
                    Screen.INVENTORY -> InventoryScreen(
                        state = state,
                        onStartInventory = { rfidController.startInventory() },
                        onStopInventory = { rfidController.stopInventory() },
                        onClear = { epcs.clear() },
                        onBack = { currentScreen = Screen.MAIN }
                    )
                    Screen.TAG_DETAILS -> TagDetailsScreen(
                        state = state,
                        details = tagDetails,
                        onReadTag = { rfidController.readTagDetails() },
                        onBack = { currentScreen = Screen.MAIN }
                    )
                    Screen.ENCODE -> EncodeScreen(
                        state = state,
                        epcInput = encodeEpcInput,
                        onEpcChange = { encodeEpcInput = it.uppercase() },
                        onEncode = { rfidController.encodeTag(encodeEpcInput) },
                        onBack = { currentScreen = Screen.MAIN }
                    )
                }
            }
        }
    }

    private fun requestBtPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    override fun onDestroy() {
        rfidController.disconnect()
        super.onDestroy()
    }

    private enum class Screen {
        MAIN,
        INVENTORY,
        TAG_DETAILS,
        ENCODE
    }
}
