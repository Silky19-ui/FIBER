package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.ui.graphics.Color
import com.example.data.model.ClientCategory
import com.example.data.model.ClientWithDetails
import com.example.ui.components.ClientCard
import com.example.ui.components.KpiMetricsSection
import com.example.ui.viewmodel.FiberCrmUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardListScreen(
    uiState: FiberCrmUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onClientSelected: (Long) -> Unit,
    onAddNewClient: () -> Unit,
    onReseedDemoData: () -> Unit,
    onOpenWorkers: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onOpenCommercialAdvisor: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FiberCRM",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gestión FTTH y Telecom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Portal Asesor Comercial (Leads y Prospectos)
                    IconButton(
                        onClick = onOpenCommercialAdvisor,
                        modifier = Modifier.testTag("btn_open_commercial_portal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "Portal Asesor Comercial (Leads y Prospectos)",
                            tint = Color(0xFFFB8C00)
                        )
                    }

                    // Admin Panel: Roles and Functions Management
                    IconButton(
                        onClick = onOpenAdminPanel,
                        modifier = Modifier.testTag("btn_open_admin_panel")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Panel de Administrador (Roles y Funciones)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Button to open Workers and Technicians management
                    IconButton(
                        onClick = onOpenWorkers,
                        modifier = Modifier.testTag("btn_open_workers")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Gestionar Equipo de Trabajadores",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = onReseedDemoData,
                        modifier = Modifier.testTag("btn_reseed_demo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restaurar datos demo de prueba"
                        )
                    }

                    // Sign Out Button
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("btn_sign_out")
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
                onClick = onAddNewClient,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Lead / Cliente", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_client")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_client"),
                    placeholder = { Text("Buscar cliente, dirección, caja NAP o OLT...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Top KPI Carousel Section
            KpiMetricsSection(
                metrics = uiState.metrics,
                selectedFilter = uiState.selectedCategoryFilter,
                onFilterSelected = onCategoryFilterChange
            )

            // Category Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "TODOS" to "Todos (${uiState.metrics.totalCount})",
                    ClientCategory.LEAD.key to "Leads (${uiState.metrics.leadCount})",
                    ClientCategory.POTENCIAL.key to "Potenciales (${uiState.metrics.potencialCount})",
                    ClientCategory.CLIENTE.key to "Clientes (${uiState.metrics.clienteCount})",
                    "MOROSOS" to "Morosos (${if (uiState.metrics.suspendidoCount > 0) uiState.metrics.suspendidoCount else "0"})"
                )

                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedCategoryFilter.equals(key, ignoreCase = true),
                        onClick = { onCategoryFilterChange(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            // Client Records List or Empty State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredClients.isEmpty()) {
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
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = "No se encontraron registros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "Ningún cliente o lead coincide con '${uiState.searchQuery}'"
                            } else {
                                "No hay registros en la categoría seleccionada."
                            },
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
                        items = uiState.filteredClients,
                        key = { it.client.id }
                    ) { clientItem ->
                        ClientCard(
                            clientWithDetails = clientItem,
                            onClick = { onClientSelected(clientItem.client.id) }
                        )
                    }
                }
            }
        }
    }
}
