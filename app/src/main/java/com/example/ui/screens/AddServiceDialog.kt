package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.ServiceStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddServiceDialog(
    clientId: Long,
    onDismiss: () -> Unit,
    onConfirm: (InstalledServiceEntity) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    var planName by remember { mutableStateOf("Fibra Simétrica 300 Mbps") }
    var downloadSpeed by remember { mutableIntStateOf(300) }
    var uploadSpeed by remember { mutableIntStateOf(300) }
    var monthlyCost by remember { mutableDoubleStateOf(549.00) }
    var installationDate by remember { mutableStateOf(currentDateStr) }
    var ontModel by remember { mutableStateOf("Huawei HG8245H5 Dual-Band") }
    var ontSerial by remember { mutableStateOf("HWTC${(1000000..9999999).random()}") }
    var wifiSsid by remember { mutableStateOf("FibraOptica_5G") }
    var wifiPassword by remember { mutableStateOf("Fibra2026*") }
    var technicianName by remember { mutableStateOf("Ing. Carlos Mendoza") }
    var status by remember { mutableStateOf(ServiceStatus.ACTIVO.key) }
    var notes by remember { mutableStateOf("Instalación estándar con cable drop y roseta óptica.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Instalación / Servicio FTTH",
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
                Text(
                    text = "Planes de Fibra Óptica Predefinidos:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = downloadSpeed == 100,
                        onClick = {
                            planName = "Fibra Básica 100 Mbps"
                            downloadSpeed = 100
                            uploadSpeed = 100
                            monthlyCost = 399.00
                        },
                        label = { Text("100 Mbps ($399)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = downloadSpeed == 300,
                        onClick = {
                            planName = "Fibra Simétrica 300 Mbps"
                            downloadSpeed = 300
                            uploadSpeed = 300
                            monthlyCost = 549.00
                        },
                        label = { Text("300 Mbps ($549)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = downloadSpeed == 600,
                        onClick = {
                            planName = "Fibra Gamer 600 Mbps"
                            downloadSpeed = 600
                            uploadSpeed = 600
                            monthlyCost = 799.00
                        },
                        label = { Text("600 Mbps ($799)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = downloadSpeed == 1000,
                        onClick = {
                            planName = "Fibra GigaPro 1 Gbps"
                            downloadSpeed = 1000
                            uploadSpeed = 1000
                            monthlyCost = 1499.00
                        },
                        label = { Text("1 Gbps ($1,499)", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Nombre del Plan") },
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = downloadSpeed.toString(),
                        onValueChange = { it.toIntOrNull()?.let { s -> downloadSpeed = s; uploadSpeed = s } },
                        label = { Text("Bajada/Subida (Mbps)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format("%.2f", monthlyCost),
                        onValueChange = { it.toDoubleOrNull()?.let { c -> monthlyCost = c } },
                        label = { Text("Costo Mensual ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Datos del Equipo ONT y Router:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                OutlinedTextField(
                    value = ontModel,
                    onValueChange = { ontModel = it },
                    label = { Text("Modelo de ONT (Terminal Óptica)") },
                    leadingIcon = { Icon(Icons.Default.Router, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ontSerial,
                    onValueChange = { ontSerial = it },
                    label = { Text("Número de Serie ONT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_ont_serial")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = { Text("Nombre WiFi (SSID)") },
                        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it },
                        label = { Text("Contraseña WiFi") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = installationDate,
                    onValueChange = { installationDate = it },
                    label = { Text("Fecha de Instalación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = technicianName,
                    onValueChange = { technicianName = it },
                    label = { Text("Técnico Responsable") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observaciones / Ubicación ONT") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        InstalledServiceEntity(
                            clientId = clientId,
                            planName = planName.trim(),
                            downloadSpeedMbps = downloadSpeed,
                            uploadSpeedMbps = uploadSpeed,
                            monthlyCost = monthlyCost,
                            installationDate = installationDate.trim(),
                            ontModel = ontModel.trim(),
                            ontSerial = ontSerial.trim(),
                            wifiSsid = wifiSsid.trim(),
                            wifiPassword = wifiPassword.trim(),
                            status = status,
                            technicianName = technicianName.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("btn_confirm_add_service")
            ) {
                Text("Registrar Instalación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
