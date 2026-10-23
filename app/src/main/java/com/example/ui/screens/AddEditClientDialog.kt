package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity

@Composable
fun AddEditClientDialog(
    initialClient: ClientEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (ClientEntity) -> Unit
) {
    val isEditing = initialClient != null

    var fullName by remember { mutableStateOf(initialClient?.fullName ?: "") }
    var businessOrDocument by remember { mutableStateOf(initialClient?.businessOrDocument ?: "") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var category by remember { mutableStateOf(initialClient?.category ?: ClientCategory.LEAD.key) }
    var address by remember { mutableStateOf(initialClient?.address ?: "") }
    var colonyOrSector by remember { mutableStateOf(initialClient?.colonyOrSector ?: "") }
    var city by remember { mutableStateOf(initialClient?.city ?: "Guadalajara, JAL") }
    var postalCode by remember { mutableStateOf(initialClient?.postalCode ?: "") }
    var latitude by remember { mutableDoubleStateOf(initialClient?.latitude ?: 20.6767) }
    var longitude by remember { mutableDoubleStateOf(initialClient?.longitude ?: -103.3475) }

    var napBoxId by remember { mutableStateOf(initialClient?.napBoxId ?: "NAP-C05") }
    var napPort by remember { mutableIntStateOf(initialClient?.napPort ?: 1) }
    var opticalPowerDbm by remember { mutableDoubleStateOf(initialClient?.opticalPowerDbm ?: -19.5) }
    var dropMeters by remember { mutableIntStateOf(initialClient?.dropMeters ?: 50) }
    var oltNode by remember { mutableStateOf(initialClient?.oltNode ?: "OLT-ZTE-CENTRAL") }
    var notes by remember { mutableStateOf(initialClient?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Registro" else "Nuevo Lead / Cliente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Chips Selector
                Text(
                    text = "Categoría en el CRM:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClientCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat.key, ignoreCase = true),
                            onClick = { category = cat.key },
                            label = { Text(cat.title, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_cat_${cat.key}")
                        )
                    }
                }

                // General Contact Info
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nombre Completo / Razón Social *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("El nombre es obligatorio") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_client_name")
                )

                OutlinedTextField(
                    value = businessOrDocument,
                    onValueChange = { businessOrDocument = it },
                    label = { Text("DNI / RFC / Identificación") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_client_doc")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (it.isNotBlank()) phoneError = false
                    },
                    label = { Text("Teléfono / WhatsApp *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    supportingText = if (phoneError) { { Text("El teléfono es obligatorio") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_client_phone")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Address & Location Section
                Text(
                    text = "Ubicación del Servicio:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Calle y Número (Exterior/Interior)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = colonyOrSector,
                        onValueChange = { colonyOrSector = it },
                        label = { Text("Colonia / Sector") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("C.P.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(90.dp)
                    )
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad / Municipio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Coordinates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format("%.5f", latitude),
                        onValueChange = { it.toDoubleOrNull()?.let { lat -> latitude = lat } },
                        label = { Text("Latitud") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format("%.5f", longitude),
                        onValueChange = { it.toDoubleOrNull()?.let { lng -> longitude = lng } },
                        label = { Text("Longitud") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // FTTH Infrastructure Parameters
                Text(
                    text = "Infraestructura de Fibra (FTTH):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = napBoxId,
                        onValueChange = { napBoxId = it },
                        label = { Text("Caja NAP") },
                        placeholder = { Text("NAP-01") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = napPort.toString(),
                        onValueChange = { it.toIntOrNull()?.let { p -> napPort = p } },
                        label = { Text("Puerto (1-16)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = String.format("%.1f", opticalPowerDbm),
                        onValueChange = { it.toDoubleOrNull()?.let { p -> opticalPowerDbm = p } },
                        label = { Text("Potencia (dBm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dropMeters.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> dropMeters = m } },
                        label = { Text("Drop (Metros)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = oltNode,
                    onValueChange = { oltNode = it },
                    label = { Text("Nodo / OLT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de Seguimiento") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validName = fullName.isNotBlank()
                    val validPhone = phone.isNotBlank()
                    if (!validName) nameError = true
                    if (!validPhone) phoneError = true

                    if (validName && validPhone) {
                        val clientToSave = (initialClient ?: ClientEntity(
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            colonyOrSector = colonyOrSector.trim(),
                            city = city.trim()
                        )).copy(
                            fullName = fullName.trim(),
                            businessOrDocument = businessOrDocument.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            category = category,
                            address = address.trim(),
                            colonyOrSector = colonyOrSector.trim(),
                            city = city.trim(),
                            postalCode = postalCode.trim(),
                            latitude = latitude,
                            longitude = longitude,
                            napBoxId = napBoxId.trim(),
                            napPort = napPort,
                            opticalPowerDbm = opticalPowerDbm,
                            dropMeters = dropMeters,
                            oltNode = oltNode.trim(),
                            notes = notes.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onConfirm(clientToSave)
                    }
                },
                modifier = Modifier.testTag("btn_save_client")
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Registro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
