package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithDetails
import com.example.data.model.WorkerEntity

/**
 * Pantalla Exclusiva para Asesores Comerciales.
 *
 * Los asesores únicamente tienen acceso a la captura y consulta de Leads y Clientes Potenciales,
 * garantizando la confidencialidad de la infraestructura técnica (ONTs, puertos OLT, potencias dBm)
 * y de las finanzas (facturación, saldos morosos, cobros).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommercialAdvisorScreen(
    commercialLeads: List<ClientWithDetails>,
    workerProfile: WorkerEntity?,
    userEmail: String?,
    canReturnToAdmin: Boolean,
    onBackToAdmin: () -> Unit,
    onSaveLead: (ClientEntity) -> Unit,
    onUpdateCategory: (Long, ClientCategory) -> Unit,
    onDeleteLead: (ClientEntity) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // "TODOS", "LEAD", "POTENCIAL"

    var leadToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var leadToViewDetail by remember { mutableStateOf<ClientEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Interceptar BackHandler si se puede regresar a la vista administrativa
    BackHandler(enabled = canReturnToAdmin) {
        onBackToAdmin()
    }

    // Filtrar estrictamente solo Leads y Potenciales
    val filteredLeads = commercialLeads.filter { item ->
        val client = item.client
        val isCommercial = client.category == ClientCategory.LEAD.key || client.category == ClientCategory.POTENCIAL.key

        val matchesFilter = when (selectedFilter) {
            "TODOS" -> true
            else -> client.category.equals(selectedFilter, ignoreCase = true)
        }

        val matchesQuery = if (searchQuery.isBlank()) {
            true
        } else {
            val q = searchQuery.trim().lowercase()
            client.fullName.lowercase().contains(q) ||
                    client.phone.lowercase().contains(q) ||
                    client.address.lowercase().contains(q) ||
                    client.colonyOrSector.lowercase().contains(q) ||
                    client.city.lowercase().contains(q) ||
                    client.notes.lowercase().contains(q)
        }

        isCommercial && matchesFilter && matchesQuery
    }

    val totalLeadsCount = commercialLeads.count { it.client.category == ClientCategory.LEAD.key }
    val totalProspectsCount = commercialLeads.count { it.client.category == ClientCategory.POTENCIAL.key }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFB8C00),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Portal Comercial",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = "Ventas",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = workerProfile?.fullName ?: userEmail ?: "Asesor de Ventas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Botón para alternar a Vista General si tiene privilegios de Administrador
                    if (canReturnToAdmin) {
                        IconButton(
                            onClick = onBackToAdmin,
                            modifier = Modifier.testTag("btn_back_to_admin")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Regresar al CRM General",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botón de Cerrar Sesión
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("btn_advisor_sign_out")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    leadToEdit = null
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Lead / Prospecto", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFFFB8C00),
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_lead")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Banner de Rol Restringido
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Acceso Restringido",
                        tint = Color(0xFFFB8C00),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acceso exclusivo a registro de Leads y Prospectos. Otras áreas (Facturación, Red FTTH, etc.) restringidas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Métricas comerciales rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total
                MetricCard(
                    title = "Total Registros",
                    value = "${commercialLeads.size}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Nuevos Leads
                MetricCard(
                    title = "Leads Nuevos",
                    value = "$totalLeadsCount",
                    color = Color(0xFFFB8C00),
                    modifier = Modifier.weight(1f)
                )

                // Prospectos en Cotización
                MetricCard(
                    title = "Prospectos",
                    value = "$totalProspectsCount",
                    color = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )
            }

            // Barra de Búsqueda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, teléfono, colonia...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commercial_search_input")
                )
            }

            // Chips de Filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "TODOS",
                    onClick = { selectedFilter = "TODOS" },
                    label = { Text("Todos (${commercialLeads.size})", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_all_leads")
                )

                FilterChip(
                    selected = selectedFilter == ClientCategory.LEAD.key,
                    onClick = { selectedFilter = ClientCategory.LEAD.key },
                    label = { Text("Leads / Consultas ($totalLeadsCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFF3E0),
                        selectedLabelColor = Color(0xFFE65100)
                    ),
                    modifier = Modifier.testTag("filter_only_leads")
                )

                FilterChip(
                    selected = selectedFilter == ClientCategory.POTENCIAL.key,
                    onClick = { selectedFilter = ClientCategory.POTENCIAL.key },
                    label = { Text("Prospectos / Cotizados ($totalProspectsCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE1F5FE),
                        selectedLabelColor = Color(0xFF01579B)
                    ),
                    modifier = Modifier.testTag("filter_only_prospects")
                )
            }

            // Lista de Prospectos
            if (filteredLeads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFFB8C00),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron prospectos con '$searchQuery'" else "Sin registros en esta categoría",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pulsa '+ Nuevo Lead / Prospecto' para registrar contacto y dirección.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredLeads,
                        key = { it.client.id }
                    ) { leadItem ->
                        CommercialLeadCard(
                            client = leadItem.client,
                            onClick = { leadToViewDetail = leadItem.client },
                            onPromote = {
                                val nextCategory = if (leadItem.client.category == ClientCategory.LEAD.key) {
                                    ClientCategory.POTENCIAL
                                } else {
                                    ClientCategory.LEAD
                                }
                                onUpdateCategory(leadItem.client.id, nextCategory)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal de Creación o Edición Simple
    if (showCreateDialog) {
        SimpleLeadRegistrationDialog(
            initialLead = leadToEdit,
            onDismiss = { showCreateDialog = false },
            onConfirm = { client ->
                onSaveLead(client)
                showCreateDialog = false
            }
        )
    }

    // Modal de Detalle Comercial
    leadToViewDetail?.let { lead ->
        SimpleLeadDetailDialog(
            lead = lead,
            onDismiss = { leadToViewDetail = null },
            onEditLead = {
                leadToEdit = it
                leadToViewDetail = null
                showCreateDialog = true
            },
            onToggleCategory = { cl, newCat ->
                onUpdateCategory(cl.id, newCat)
            },
            onDeleteLead = { cl ->
                onDeleteLead(cl)
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CommercialLeadCard(
    client: ClientEntity,
    onClick: () -> Unit,
    onPromote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLead = client.category == ClientCategory.LEAD.key
    val categoryColor = if (isLead) Color(0xFFFB8C00) else Color(0xFF0288D1)
    val categoryLabel = if (isLead) "Lead Nuevo" else "Prospecto Cotizado"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("lead_card_${client.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Encabezado: Nombre y Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = categoryColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = client.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = categoryColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = client.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dirección
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${client.address}, ${client.colonyOrSector}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Plan o notas de interés si existen
            if (client.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = client.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción rápida en la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Llamar
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .clickable { dialPhone(context, client.phone) }
                            .padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Llamar", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // WhatsApp
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { openWhatsApp(context, client.phone, client.fullName) }
                            .padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color(0xFF1E7E34), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF1E7E34), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Botón rápido de avanzar estado
                Button(
                    onClick = onPromote,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLead) Color(0xFF0288D1) else Color(0xFFFB8C00)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isLead) "➔ A Cotización" else "➔ A Lead",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun dialPhone(context: Context, rawPhone: String) {
    try {
        val cleanNumber = rawPhone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, rawPhone: String, clientName: String) {
    try {
        var cleanNumber = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanNumber.length == 10) {
            cleanNumber = "52$cleanNumber"
        }
        val text = "Hola $clientName, te saludo de FiberCRM Telecom para dar seguimiento a tu solicitud de servicio de fibra óptica."
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(text)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
