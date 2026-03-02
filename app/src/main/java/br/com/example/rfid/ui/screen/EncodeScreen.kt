package br.com.example.rfid.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EncodeScreen(
    state: RfidUiState,
    epcInput: String,
    onEpcChange: (String) -> Unit,
    onEncode: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) { Text("Voltar") }
                Button(
                    onClick = onEncode,
                    enabled = state.canEncode && epcInput.isNotBlank()
                ) {
                    Text("Codificar")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Status: ${state.status}")

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = epcInput,
                onValueChange = onEpcChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("EPC (hex)") },
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Text("Use apenas hexadecimal, sem espaços.")
        }
    }
}
