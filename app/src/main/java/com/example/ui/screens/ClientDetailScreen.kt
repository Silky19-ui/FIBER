package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithDetails
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import com.example.ui.components.CategoryBadge
import com.example.ui.components.InvoiceStatusBadge
import com.example.ui.components.OpticalPowerIndicator
import com.example.ui.components.ServiceStatusBadge
import com.example.ui.components.launchIntentSafely
import com.example.ui.theme.ColorCliente
import com.example.ui.theme.ColorLead
import com.example.ui.theme.ColorSuspendido

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientDetailScreen(
    clientWithDetails: ClientWithDetails,
    onBack: () -> Unit,
    onEditClient: () -> Unit,
    onDeleteClient: () -> Unit,
    onCategoryChange: (ClientCategory) -> Unit,
    onAddService: () -> Unit,
    onDeleteService: (InstalledServiceEntity) -> Unit,
    onAddInvoice: () -> Unit,
    onDeleteInvoice: (InvoiceItemEntity) -> Unit,
    onPayInvoice: (InvoiceItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Android hardware/back gesture handler
    BackHandler { onBack() }

    val context = LocalContext.current
    val client = clientWithDetails.client
    val currentCategory = ClientCategory.fromKey(client.category)
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = currentCategory.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_from_detail")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al listado"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEditClient,
                        modifier = Modifier.testTag("btn_edit_client")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar información del cliente"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("btn_delete_client")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar cliente",
                            tint = ColorSuspendido
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // CRM Stage Selector (Lead -> Potencial -> Cliente -> Suspendido)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Fase en el Ciclo de Vida CRM:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ClientCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = currentCategory == cat,
                                    onClick = { onCategoryChange(cat) },
                                    label = { Text(cat.title, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Contact & Location Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contacto y Ubicación",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            CategoryBadge(category = currentCategory)
                        }

                        if (client.businessOrDocument.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Documento: ${client.businessOrDocument}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Phone & Email
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = client.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        if (client.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = client.email, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Address & GPS
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${client.address}, ${client.colonyOrSector}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${client.city} ${if (client.postalCode.isNotBlank()) "C.P. ${client.postalCode}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (client.latitude != 0.0 && client.longitude != 0.0) {
                                    Text(
                                        text = "GPS: ${String.format("%.5f", client.latitude)}, ${String.format("%.5f", client.longitude)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons: Call, WhatsApp, Open Navigation Maps, Copy Coords
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${client.phone.replace(" ", "")}")
                                    }
                                    launchIntentSafely(context, intent, "No se pudo abrir marcador telefónico")
                                },
                                contentPadding = ButtonDefaults.ContentPadding
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Llamar", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val cleanNumber = client.phone.replace("+", "").replace(" ", "").replace("-", "")
                                    val url = "https://wa.me/$cleanNumber?text=Hola%20${Uri.encode(client.fullName)},%20nos%20comunicamos%20del%20servicio%20de%20Fibra%20%C3%93ptica."
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    launchIntentSafely(context, intent, "WhatsApp no disponible")
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = ColorCliente, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val uri = if (client.latitude != 0.0 && client.longitude != 0.0) {
                                        Uri.parse("geo:${client.latitude},${client.longitude}?q=${client.latitude},${client.longitude}(${Uri.encode(client.fullName)})")
                                    } else {
                                        Uri.parse("geo:0,0?q=${Uri.encode("${client.address}, ${client.colonyOrSector}, ${client.city}")}")
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    launchIntentSafely(context, intent, "No se encontró aplicación de Mapas")
                                }
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Navegar", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = if (client.latitude != 0.0) "${client.latitude}, ${client.longitude}" else "${client.address}, ${client.colonyOrSector}"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Ubicacion", text))
                                    Toast.makeText(context, "Ubicación copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copiar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // FTTH Infrastructure Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Parámetros Técnicos de Fibra Óptica (FTTH)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Caja NAP y Puerto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (client.napBoxId.isNotBlank()) "${client.napBoxId} - Puerto ${client.napPort}" else "Sin asignar",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Potencia Óptica RX", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OpticalPowerIndicator(powerDbm = client.opticalPowerDbm)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Nodo OLT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = client.oltNode.ifBlank { "OLT-CENTRAL" },
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Longitud Cable Drop", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${client.dropMeters} metros",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (client.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notas: ${client.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tab Navigation: Historial de Servicios vs Estado de Cuenta
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Servicios Instalados (${clientWithDetails.services.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Router, contentDescription = null) },
                        modifier = Modifier.testTag("tab_services")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Estado de Cuenta (${clientWithDetails.invoices.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        modifier = Modifier.testTag("tab_invoices")
                    )
                }
            }

            // TAB 0: HISTORIAL DE SERVICIOS INSTALADOS
            if (selectedTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de Servicios FTTH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onAddService,
                            modifier = Modifier.testTag("btn_add_service")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo Servicio", fontSize = 12.sp)
                        }
                    }
                }

                if (clientWithDetails.services.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Router, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sin servicios instalados aún", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Pulsa '+ Nuevo Servicio' para registrar la ONT, plan de velocidad y WiFi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(clientWithDetails.services, key = { it.id }) { service ->
                        ServiceItemCard(
                            service = service,
                            onDelete = { onDeleteService(service) }
                        )
                    }
                }
            }

            // TAB 1: ESTADO DE CUENTA DETALLADO
            if (selectedTab == 1) {
                // Financial summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Resumen de Cuenta",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                IconButton(
                                    onClick = {
                                        shareAccountStatement(context, clientWithDetails)
                                    },
                                    modifier = Modifier.testTag("btn_share_statement")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartir estado de cuenta")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Total Facturado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$${String.format("%,.2f", clientWithDetails.totalInvoiced)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Cobrado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$${String.format("%,.2f", clientWithDetails.totalPaid)}",
                                        fontWeight = FontWeight.Bold,
                                        color = ColorCliente,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Saldo Pendiente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$${String.format("%,.2f", clientWithDetails.balanceDue)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (clientWithDetails.balanceDue > 0) ColorSuspendido else ColorCliente,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Invoices Action Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Facturas y Recibos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onAddInvoice,
                            modifier = Modifier.testTag("btn_add_invoice")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generar Factura", fontSize = 12.sp)
                        }
                    }
                }

                if (clientWithDetails.invoices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sin facturas emitidas", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Genera una factura mensual o cargo de instalación.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(clientWithDetails.invoices, key = { it.id }) { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            onPay = { onPayInvoice(invoice) },
                            onDelete = { onDeleteInvoice(invoice) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Registro") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${client.fullName}'? Se borrarán sus servicios instalados y facturas asociadas permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClient()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSuspendido)
                ) {
                    Text("Eliminar Definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ServiceItemCard(
    service: InstalledServiceEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = service.planName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                ServiceStatusBadge(status = service.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Velocidad: ${service.downloadSpeedMbps} Mbps Bajada / ${service.uploadSpeedMbps} Mbps Subida",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$${String.format("%,.2f", service.monthlyCost)}/mes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // ONT & WiFi info
            Text(
                text = "Terminal Óptica (ONT): ${service.ontModel} (S/N: ${service.ontSerial})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (service.wifiSsid.isNotBlank()) {
                Text(
                    text = "WiFi SSID: ${service.wifiSsid} | Clave: ${service.wifiPassword.ifBlank { "••••••••" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instalado el ${service.installationDate} por ${service.technicianName.ifBlank { "Técnico" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar servicio", tint = ColorSuspendido, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun InvoiceItemCard(
    invoice: InvoiceItemEntity,
    onPay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = invoice.concept,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                InvoiceStatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vencimiento: ${invoice.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (invoice.status == InvoiceStatus.VENCIDO.key) ColorSuspendido else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (invoice.paymentDate != null) {
                        Text(
                            text = "Pagado el ${invoice.paymentDate} (${invoice.paymentMethod ?: ""})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorCliente
                        )
                    }
                }

                Text(
                    text = "$${String.format("%,.2f", invoice.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (invoice.status == InvoiceStatus.PAGADO.key) ColorCliente else MaterialTheme.colorScheme.onSurface
                )
            }

            if (invoice.receiptFolio.isNotBlank()) {
                Text(
                    text = "Folio Recibo: ${invoice.receiptFolio}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (invoice.status != InvoiceStatus.PAGADO.key) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.height(36.dp).testTag("btn_pay_invoice_${invoice.id}")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Pago", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Recibo Acreditado",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorCliente,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar factura", tint = ColorSuspendido, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

fun shareAccountStatement(context: Context, clientWithDetails: ClientWithDetails) {
    val client = clientWithDetails.client
    val sb = StringBuilder()
    sb.appendLine("========================================")
    sb.appendLine("ESTADO DE CUENTA - FIBRA ÓPTICA (FTTH)")
    sb.appendLine("========================================")
    sb.appendLine("Cliente: ${client.fullName}")
    if (client.businessOrDocument.isNotBlank()) {
        sb.appendLine("Identificación/RFC: ${client.businessOrDocument}")
    }
    sb.appendLine("Dirección: ${client.address}, ${client.colonyOrSector}, ${client.city}")
    sb.appendLine("Teléfono: ${client.phone}")
    sb.appendLine("Caja NAP: ${client.napBoxId} [Puerto: ${client.napPort}]")
    sb.appendLine("Potencia Óptica: ${client.opticalPowerDbm} dBm")
    sb.appendLine("----------------------------------------")
    sb.appendLine("HISTORIAL DE SERVICIOS:")
    if (clientWithDetails.services.isEmpty()) {
        sb.appendLine("- Sin servicios registrados.")
    } else {
        clientWithDetails.services.forEach {
            sb.appendLine("- ${it.planName} (${it.downloadSpeedMbps}M/simétrico)")
            sb.appendLine("  ONT: ${it.ontModel} | S/N: ${it.ontSerial}")
            sb.appendLine("  Costo Mensual: $${String.format("%,.2f", it.monthlyCost)} | Estado: ${it.status}")
        }
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine("ESTADO FINANCIERO / FACTURAS:")
    sb.appendLine("Total Facturado: $${String.format("%,.2f", clientWithDetails.totalInvoiced)}")
    sb.appendLine("Total Pagado:    $${String.format("%,.2f", clientWithDetails.totalPaid)}")
    sb.appendLine("SALDO PENDIENTE: $${String.format("%,.2f", clientWithDetails.balanceDue)}")
    sb.appendLine("----------------------------------------")
    clientWithDetails.invoices.forEach { inv ->
        val statusLabel = if (inv.status == "PAGADO") "[PAGADO - ${inv.paymentDate}]" else "[PENDIENTE - Vence: ${inv.dueDate}]"
        sb.appendLine("* ${inv.invoiceNumber}: ${inv.concept}")
        sb.appendLine("  Monto: $${String.format("%,.2f", inv.amount)} $statusLabel")
        if (inv.receiptFolio.isNotBlank()) {
            sb.appendLine("  Folio: ${inv.receiptFolio}")
        }
    }
    sb.appendLine("========================================")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_SUBJECT, "Estado de Cuenta - ${client.fullName}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir Estado de Cuenta vía")
    launchIntentSafely(context, shareIntent, "No se pudo compartir el estado de cuenta")
}
