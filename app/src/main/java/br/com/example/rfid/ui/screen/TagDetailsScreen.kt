package br.com.example.rfid.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.example.rfid.model.TagDetails

@Composable
fun TagDetailsScreen(
    state: RfidUiState,
    details: TagDetails?,
    onReadTag: () -> Unit,
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
                Button(onClick = onReadTag, enabled = state.canOpenTagDetails) {
                    Text("Ler tag")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Status: ${state.status}")
            Spacer(Modifier.height(12.dp))

            Text("EPC: ${formatValue(details?.tagId)}")
            Spacer(Modifier.height(8.dp))
            Text("MB01 (Reserved): ${formatValue(details?.mb01)}")
            Spacer(Modifier.height(8.dp))
            Text("MB02 (EPC): ${formatValue(details?.mb02)}")
            Spacer(Modifier.height(8.dp))
            Text("MB03 (TID): ${formatValue(details?.mb03)}")
            Spacer(Modifier.height(8.dp))
            Text("MB04 (User): ${formatValue(details?.mb04)}")
        }
    }
}

private fun formatValue(value: String?): String {
    return if (value.isNullOrBlank()) "-" else value
}
