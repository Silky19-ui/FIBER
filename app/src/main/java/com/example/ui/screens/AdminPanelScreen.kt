package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.RoleDepartment
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerFunctionItem
import com.example.data.model.WorkerFunctions
import com.example.ui.theme.ColorCliente
import com.example.ui.theme.ColorLead
import com.example.ui.theme.ColorPotencial
import com.example.ui.theme.ColorSuspendido

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    workers: List<WorkerEntity>,
    onBack: () -> Unit,
    onSaveWorker: (WorkerEntity) -> Unit,
    onDeleteWorker: (WorkerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedDepartmentFilter by remember { mutableStateOf("TODOS") }
    var searchQuery by remember { mutableStateOf("") }
    var workerToEditFunctions by remember { mutableStateOf<WorkerEntity?>(null) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }

    // Filter workers by department and search
    val filteredWorkers = remember(workers, selectedDepartmentFilter, searchQuery) {
        workers.filter { w ->
            val matchesDept = if (selectedDepartmentFilter == "TODOS") true
            else w.departmentKey.equals(selectedDepartmentFilter, ignoreCase = true)

            val matchesQuery = if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.trim().lowercase()
                w.fullName.lowercase().contains(q) ||
                        w.role.lowercase().contains(q) ||
                        w.assignedZone.lowercase().contains(q) ||
                        w.employeeNumber.lowercase().contains(q)
            }
            matchesDept && matchesQuery
        }
    }

    // Counts per department
    val technicalCount = workers.count { it.departmentKey == RoleDepartment.OPERACIONES_TECNICAS.key }
    val salesCount = workers.count { it.departmentKey == RoleDepartment.COMERCIAL_VENTAS.key }
    val financeCount = workers.count { it.departmentKey == RoleDepartment.FINANZAS_COBRANZA.key }
    val adminCount = workers.count { it.departmentKey == RoleDepartment.ADMINISTRACION.key }

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
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Panel de Administrador",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Roles, Funciones y Permisos de Personal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_from_admin")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al CRM"
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
                onClick = { showAddWorkerDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Empleado / Rol", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_admin_add_worker")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Department Summary KPI Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatChip(
                    title = "Técnicos FTTH",
                    count = technicalCount,
                    color = Color(RoleDepartment.OPERACIONES_TECNICAS.colorHex),
                    isSelected = selectedDepartmentFilter == RoleDepartment.OPERACIONES_TECNICAS.key,
                    onClick = {
                        selectedDepartmentFilter = if (selectedDepartmentFilter == RoleDepartment.OPERACIONES_TECNICAS.key) "TODOS"
                        else RoleDepartment.OPERACIONES_TECNICAS.key
                    }
                )

                AdminStatChip(
                    title = "Ventas & Leads",
                    count = salesCount,
                    color = Color(RoleDepartment.COMERCIAL_VENTAS.colorHex),
                    isSelected = selectedDepartmentFilter == RoleDepartment.COMERCIAL_VENTAS.key,
                    onClick = {
                        selectedDepartmentFilter = if (selectedDepartmentFilter == RoleDepartment.COMERCIAL_VENTAS.key) "TODOS"
                        else RoleDepartment.COMERCIAL_VENTAS.key
                    }
                )

                AdminStatChip(
                    title = "Finanzas / Cobranza",
                    count = financeCount,
                    color = Color(RoleDepartment.FINANZAS_COBRANZA.colorHex),
                    isSelected = selectedDepartmentFilter == RoleDepartment.FINANZAS_COBRANZA.key,
                    onClick = {
                        selectedDepartmentFilter = if (selectedDepartmentFilter == RoleDepartment.FINANZAS_COBRANZA.key) "TODOS"
                        else RoleDepartment.FINANZAS_COBRANZA.key
                    }
                )

                AdminStatChip(
                    title = "Administración",
                    count = adminCount,
                    color = Color(RoleDepartment.ADMINISTRACION.colorHex),
                    isSelected = selectedDepartmentFilter == RoleDepartment.ADMINISTRACION.key,
                    onClick = {
                        selectedDepartmentFilter = if (selectedDepartmentFilter == RoleDepartment.ADMINISTRACION.key) "TODOS"
                        else RoleDepartment.ADMINISTRACION.key
                    }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar empleado por nombre, rol o zona...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("admin_search_worker")
            )

            // Subdivisión de Roles Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDepartmentFilter == "TODOS",
                    onClick = { selectedDepartmentFilter = "TODOS" },
                    label = { Text("Todos (${workers.size})", fontSize = 12.sp) }
                )

                RoleDepartment.entries.forEach { dept ->
                    FilterChip(
                        selected = selectedDepartmentFilter == dept.key,
                        onClick = { selectedDepartmentFilter = dept.key },
                        label = { Text(dept.title, fontSize = 12.sp) }
                    )
                }
            }

            // Employee List with Functions
            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No se encontraron colaboradores",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Añade nuevos empleados o cambia el filtro de departamento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWorkers, key = { it.id }) { worker ->
                        AdminWorkerCard(
                            worker = worker,
                            onEditFunctions = { workerToEditFunctions = worker },
                            onDelete = { onDeleteWorker(worker) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(84.dp)) }
                }
            }
        }
    }

    // Modal: Edit Worker Roles & Functions
    workerToEditFunctions?.let { worker ->
        EditWorkerFunctionsDialog(
            worker = worker,
            onDismiss = { workerToEditFunctions = null },
            onConfirm = { updatedWorker ->
                onSaveWorker(updatedWorker)
                workerToEditFunctions = null
            }
        )
    }

    // Modal: Add Worker Dialog
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = { showAddWorkerDialog = false },
            onConfirm = { newWorker ->
                onSaveWorker(newWorker)
                showAddWorkerDialog = false
            }
        )
    }
}

@Composable
fun AdminStatChip(
    title: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$title: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminWorkerCard(
    worker: WorkerEntity,
    onEditFunctions: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val department = RoleDepartment.fromKey(worker.departmentKey)
    val deptColor = Color(department.colorHex)
    val activeFunctions = worker.getFunctionsList()

    Card(
        modifier = modifier.fillMaxWidth().testTag("admin_worker_card_${worker.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Department badge, status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = deptColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = worker.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = deptColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = worker.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${worker.role} (${worker.employeeNumber.ifBlank { "ID" }})",
                            style = MaterialTheme.typography.bodySmall,
                            color = deptColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = deptColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = department.title,
                        color = deptColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subdivisión de Funciones y Permisos asignados
            Text(
                text = "Funciones y Permisos Asignados (${activeFunctions.size}):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (activeFunctions.isEmpty()) {
                    Text(
                        text = "Sin funciones asignadas (Acceso básico)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    activeFunctions.forEach { funcId ->
                        val item = WorkerFunctions.allFunctions.firstOrNull { it.id == funcId }
                        val label = item?.title ?: funcId
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zona: ${worker.assignedZone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEditFunctions,
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier.height(34.dp).testTag("btn_edit_functions_${worker.id}")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asignar Funciones", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar colaborador",
                            tint = ColorSuspendido,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditWorkerFunctionsDialog(
    worker: WorkerEntity,
    onDismiss: () -> Unit,
    onConfirm: (WorkerEntity) -> Unit
) {
    var role by remember { mutableStateOf(worker.role) }
    var selectedDepartment by remember { mutableStateOf(worker.departmentKey) }
    var assignedZone by remember { mutableStateOf(worker.assignedZone) }
    var status by remember { mutableStateOf(worker.status) }

    // Set of selected functions
    var selectedFunctions by remember {
        mutableStateOf(worker.getFunctionsList().toSet())
    }

    val zones = listOf(
        "Zona Centro (OLT-CENTRO)",
        "Zona Norte (OLT-NORTE)",
        "Zona Sur (OLT-SUR)",
        "Zona Poniente (OLT-PONIENTE)",
        "Red Global Telecom"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Funciones y Rol: ${worker.fullName}",
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
                    text = "Subdivisión Departamental:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Department selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RoleDepartment.entries.forEach { dept ->
                        FilterChip(
                            selected = selectedDepartment == dept.key,
                            onClick = {
                                selectedDepartment = dept.key
                                // Auto-suggest role title if default
                                role = when (dept) {
                                    RoleDepartment.OPERACIONES_TECNICAS -> "Técnico Instalador FTTH"
                                    RoleDepartment.COMERCIAL_VENTAS -> "Asesor Comercial / Leads"
                                    RoleDepartment.FINANZAS_COBRANZA -> "Especialista de Cobranza"
                                    RoleDepartment.ADMINISTRACION -> "Supervisor General / Admin"
                                }
                            },
                            label = { Text(dept.title, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Puesto / Título del Rol") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Zona OLT Asignada:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    zones.forEach { z ->
                        FilterChip(
                            selected = assignedZone == z,
                            onClick = { assignedZone = z },
                            label = { Text(z, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Quick Function Presets
                Text(
                    text = "Cargar Paquete de Funciones Predefinido:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedFunctions = setOf(
                                WorkerFunctions.FEASIBILITY_CHECK,
                                WorkerFunctions.FTTH_INSTALL,
                                WorkerFunctions.NETWORK_PROVISION
                            )
                        }
                    ) {
                        Text("Técnico FTTH", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedFunctions = setOf(
                                WorkerFunctions.LEADS_MANAGEMENT,
                                WorkerFunctions.FEASIBILITY_CHECK
                            )
                        }
                    ) {
                        Text("Ventas & Leads", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedFunctions = setOf(
                                WorkerFunctions.BILLING_INVOICING,
                                WorkerFunctions.PAYMENT_COLLECTION,
                                WorkerFunctions.SERVICE_SUSPENSION
                            )
                        }
                    ) {
                        Text("Cobranza", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            selectedFunctions = WorkerFunctions.allFunctions.map { it.id }.toSet()
                        }
                    ) {
                        Text("Acceso Total", fontSize = 11.sp)
                    }
                }

                // Individual Checkbox Switches for each function
                Text(
                    text = "Permisos Individuales por Función:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                WorkerFunctions.allFunctions.forEach { func ->
                    val isChecked = selectedFunctions.contains(func.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedFunctions = if (isChecked) selectedFunctions - func.id
                            else selectedFunctions + func.id
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedFunctions = if (checked) selectedFunctions + func.id
                                    else selectedFunctions - func.id
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = func.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = func.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = worker.copy(
                        role = role.trim(),
                        departmentKey = selectedDepartment,
                        assignedZone = assignedZone,
                        status = status,
                        functionsCsv = selectedFunctions.joinToString(",")
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.testTag("btn_save_functions_confirm")
            ) {
                Text("Guardar Funciones")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
