package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity

/**
 * Diálogo de Registro Simple de Leads y Prospectos para Asesores Comerciales.
 *
 * Contiene únicamente datos de contacto y dirección, sin parámetros técnicos
 * complejos (como splitters, cajas NAP, dBm ni facturación), permitiendo una
 * captura rápida y sin fricción para el equipo de ventas.
 */
@Composable
fun SimpleLeadRegistrationDialog(
    initialLead: ClientEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (ClientEntity) -> Unit
) {
    val isEditing = initialLead != null

    // Datos de contacto
    var fullName by remember { mutableStateOf(initialLead?.fullName ?: "") }
    var phone by remember { mutableStateOf(initialLead?.phone ?: "") }
    var email by remember { mutableStateOf(initialLead?.email ?: "") }
    var category by remember { mutableStateOf(initialLead?.category ?: ClientCategory.LEAD.key) }

    // Datos de dirección
    var address by remember { mutableStateOf(initialLead?.address ?: "") }
    var colonyOrSector by remember { mutableStateOf(initialLead?.colonyOrSector ?: "") }
    var city by remember { mutableStateOf(initialLead?.city ?: "Guadalajara, JAL") }
    var locationReference by remember { mutableStateOf(initialLead?.notes?.let { extractReference(it) } ?: "") }

    // Interés comercial
    var selectedPlan by remember { mutableStateOf(initialLead?.notes?.let { extractPlan(it) } ?: "300 Mbps Familiar") }
    var generalNotes by remember { mutableStateOf(initialLead?.notes?.let { extractCommercialNotes(it) } ?: "") }

    // Errores de validación
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var colonyError by remember { mutableStateOf(false) }

    val popularPlans = listOf(
        "100 Mbps Básico",
        "300 Mbps Familiar",
        "500 Mbps Gamer / Pro",
        "1 Gbps Negocio Simétrico"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (category == ClientCategory.LEAD.key) Color(0xFFFB8C00) else Color(0xFF0288D1),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isEditing) "Editar Prospecto" else "Nuevo Registro Comercial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Datos de contacto y dirección",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de Tipo: Lead vs Prospecto Potencial
                Text(
                    text = "Tipo de Registro Comercial:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = category == ClientCategory.LEAD.key,
                        onClick = { category = ClientCategory.LEAD.key },
                        label = { Text("Lead (Consulta Nueva)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFF3E0),
                            selectedLabelColor = Color(0xFFE65100)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_type_lead")
                    )

                    FilterChip(
                        selected = category == ClientCategory.POTENCIAL.key,
                        onClick = { category = ClientCategory.POTENCIAL.key },
                        label = { Text("Prospecto (Cotización)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE1F5FE),
                            selectedLabelColor = Color(0xFF01579B)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_type_prospect")
                    )
                }

                HorizontalDivider()

                // ==================== SECCIÓN 1: DATOS DE CONTACTO ====================
                Text(
                    text = "1. DATOS DE CONTACTO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Nombre completo
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nombre Completo del Interesado *") },
                    placeholder = { Text("Ej. Juan Pérez González") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_name")
                )

                // Teléfono / WhatsApp
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (it.isNotBlank()) phoneError = false
                    },
                    label = { Text("Teléfono / WhatsApp *") },
                    placeholder = { Text("Ej. 33 1234 5678") },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    supportingText = {
                        if (phoneError) Text("Ingresa un número telefónico de contacto", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_phone")
                )

                // Correo Electrónico (Opcional)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico (Opcional)") },
                    placeholder = { Text("ejemplo@correo.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_email")
                )

                HorizontalDivider()

                // ==================== SECCIÓN 2: DIRECCIÓN DEL INMUEBLE ====================
                Text(
                    text = "2. DIRECCIÓN DE INSTALACIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Calle y Número
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        if (it.isNotBlank()) addressError = false
                    },
                    label = { Text("Calle y Número Exterior / Interior *") },
                    placeholder = { Text("Ej. Av. Chapultepec 450 Int. B") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    isError = addressError,
                    supportingText = {
                        if (addressError) Text("La calle y número son obligatorios", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_address")
                )

                // Colonia / Sector
                OutlinedTextField(
                    value = colonyOrSector,
                    onValueChange = {
                        colonyOrSector = it
                        if (it.isNotBlank()) colonyError = false
                    },
                    label = { Text("Colonia / Fraccionamiento / Sector *") },
                    placeholder = { Text("Ej. Col. Americana / Sector Centro") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    isError = colonyError,
                    supportingText = {
                        if (colonyError) Text("La colonia es requerida para validar cobertura", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_colony")
                )

                // Ciudad / Municipio
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad / Municipio") },
                    placeholder = { Text("Ej. Guadalajara, JAL") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_city")
                )

                // Referencias de ubicación
                OutlinedTextField(
                    value = locationReference,
                    onValueChange = { locationReference = it },
                    label = { Text("Referencias de Ubicación") },
                    placeholder = { Text("Ej. Frente al OXXO, casa portón negro, entre calles X e Y") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_references")
                )

                HorizontalDivider()

                // ==================== SECCIÓN 3: INTERÉS COMERCIAL ====================
                Text(
                    text = "3. PLAN DE INTERÉS Y NOTAS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Paquete de fibra deseado:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularPlans.forEach { plan ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = { selectedPlan = plan },
                            label = { Text(plan, fontSize = 12.sp) },
                            leadingIcon = {
                                if (selectedPlan == plan) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // Notas adicionales de venta
                OutlinedTextField(
                    value = generalNotes,
                    onValueChange = { generalNotes = it },
                    label = { Text("Notas Comerciales / Horario de Contacto") },
                    placeholder = { Text("Ej. Llamar después de las 4 PM. Le interesa contratar para home office.") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_lead_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (fullName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    if (phone.isBlank()) {
                        phoneError = true
                        hasError = true
                    }
                    if (address.isBlank()) {
                        addressError = true
                        hasError = true
                    }
                    if (colonyOrSector.isBlank()) {
                        colonyError = true
                        hasError = true
                    }

                    if (hasError) return@Button

                    // Construcción limpia del campo notas para guardar el interés comercial
                    val formattedNotes = buildCommercialNotes(
                        plan = selectedPlan,
                        reference = locationReference.trim(),
                        userNotes = generalNotes.trim()
                    )

                    val updatedClient = (initialLead ?: ClientEntity(
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        category = category,
                        address = address.trim(),
                        colonyOrSector = colonyOrSector.trim(),
                        city = city.trim(),
                        notes = formattedNotes,
                        // Defaults automáticos de factibilidad técnica para que no dé errores
                        napBoxId = "PENDIENTE-VALIDAR",
                        oltNode = "OLT-ZONA-CENTRAL",
                        dropMeters = 40,
                        opticalPowerDbm = -19.0
                    )).copy(
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        category = category,
                        address = address.trim(),
                        colonyOrSector = colonyOrSector.trim(),
                        city = city.trim(),
                        notes = formattedNotes,
                        updatedAt = System.currentTimeMillis()
                    )

                    onConfirm(updatedClient)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (category == ClientCategory.LEAD.key) Color(0xFFFB8C00) else Color(0xFF0288D1)
                ),
                modifier = Modifier.testTag("btn_save_lead")
            ) {
                Text(
                    text = if (isEditing) "Actualizar" else "Guardar Registro",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_lead")
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun buildCommercialNotes(plan: String, reference: String, userNotes: String): String {
    val builder = StringBuilder()
    builder.append("[Interés: $plan]")
    if (reference.isNotBlank()) {
        builder.append(" | Ref: $reference")
    }
    if (userNotes.isNotBlank()) {
        builder.append(" | $userNotes")
    }
    return builder.toString()
}

private fun extractPlan(notes: String): String {
    val match = Regex("""\[Interés:\s*([^\]]+)\]""").find(notes)
    return match?.groupValues?.get(1)?.trim() ?: "300 Mbps Familiar"
}

private fun extractReference(notes: String): String {
    val match = Regex("""\|\s*Ref:\s*([^|]+)""").find(notes)
    return match?.groupValues?.get(1)?.trim() ?: ""
}

private fun extractCommercialNotes(notes: String): String {
    val withoutPlan = notes.replace(Regex("""\[Interés:[^\]]+\]"""), "")
    val withoutRef = withoutPlan.replace(Regex("""\|\s*Ref:[^|]+"""), "")
    return withoutRef.replace("|", "").trim()
}
