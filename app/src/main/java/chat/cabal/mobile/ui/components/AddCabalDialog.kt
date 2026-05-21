package chat.cabal.mobile.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddCabalDialog(
    onDismiss: () -> Unit,
    onConfirm: (key: String, name: String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Cabal") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Cabal Name") }
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Cabal Key") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(key, name) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
