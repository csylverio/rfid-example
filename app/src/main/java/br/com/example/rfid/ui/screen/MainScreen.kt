package br.com.example.rfid.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    state: RfidUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenTagDetails: () -> Unit,
    onOpenEncode: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Status: ${state.status}")

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onConnect, enabled = state.canConnect) { Text("Conectar") }
                Button(onClick = onDisconnect, enabled = state.canDisconnect) { Text("Desconectar") }
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onOpenInventory, enabled = state.connected) {
                Text("Inventário")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onOpenTagDetails, enabled = state.canOpenTagDetails) {
                Text("Tag detalhes")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onOpenEncode, enabled = state.canEncode) {
                Text("Codificar")
            }
        }
    }
}
