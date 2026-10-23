package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ClientEntity
import com.example.data.model.InvoiceItemEntity
import com.example.ui.screens.AddEditClientDialog
import com.example.ui.screens.AddInvoiceDialog
import com.example.ui.screens.AddServiceDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.CommercialAdvisorScreen
import com.example.ui.screens.DashboardListScreen
import com.example.ui.screens.RegisterPaymentDialog
import com.example.ui.screens.WorkersScreen
import com.example.ui.screens.signOut
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FiberCrmViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { user ->
                currentUser = user
            }
        )
    } else {
        FiberCrmApp(
            currentUser = currentUser!!,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun FiberCrmApp(
    currentUser: FirebaseUser,
    onSignOut: () -> Unit,
    viewModel: FiberCrmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser.email) {
        viewModel.setUserEmail(currentUser.email)
    }

    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    var serviceClientId by remember { mutableStateOf<Long?>(null) }
    var invoiceClientId by remember { mutableStateOf<Long?>(null) }
    var invoiceToPay by remember { mutableStateOf<InvoiceItemEntity?>(null) }

    val selectedClientWithDetails = uiState.selectedClientId?.let { id ->
        uiState.allClients.firstOrNull { it.client.id == id }
    }

    when {
        // Modo Asesor Comercial: si el usuario tiene rol comercial estricto o si un admin activa la vista
        uiState.isStrictCommercialAdvisor || uiState.showCommercialAdvisorPortal -> {
            CommercialAdvisorScreen(
                commercialLeads = uiState.commercialLeads,
                workerProfile = uiState.currentWorkerProfile,
                userEmail = currentUser.email,
                canReturnToAdmin = !uiState.isStrictCommercialAdvisor,
                onBackToAdmin = { viewModel.setCommercialAdvisorPortalVisible(false) },
                onSaveLead = { lead -> viewModel.saveClient(lead) },
                onUpdateCategory = { id, cat -> viewModel.updateClientCategory(id, cat) },
                onDeleteLead = { lead -> viewModel.deleteClient(lead) },
                onSignOut = onSignOut
            )
        }
        uiState.showAdminPanel -> {
            AdminPanelScreen(
                workers = uiState.workers,
                onBack = { viewModel.setAdminPanelVisible(false) },
                onSaveWorker = { worker -> viewModel.saveWorker(worker) },
                onDeleteWorker = { worker -> viewModel.deleteWorker(worker) }
            )
        }
        uiState.showWorkersScreen -> {
            WorkersScreen(
                workers = uiState.workers,
                onBack = { viewModel.setWorkersScreenVisible(false) },
                onSaveWorker = { worker -> viewModel.saveWorker(worker) },
                onDeleteWorker = { worker -> viewModel.deleteWorker(worker) }
            )
        }
        selectedClientWithDetails != null -> {
            ClientDetailScreen(
                clientWithDetails = selectedClientWithDetails,
                onBack = { viewModel.selectClient(null) },
                onEditClient = {
                    clientToEdit = selectedClientWithDetails.client
                    showAddEditDialog = true
                },
                onDeleteClient = {
                    viewModel.deleteClient(selectedClientWithDetails.client)
                },
                onCategoryChange = { newCat ->
                    viewModel.updateClientCategory(selectedClientWithDetails.client.id, newCat)
                },
                onAddService = {
                    serviceClientId = selectedClientWithDetails.client.id
                },
                onDeleteService = { service ->
                    viewModel.deleteService(service)
                },
                onAddInvoice = {
                    invoiceClientId = selectedClientWithDetails.client.id
                },
                onDeleteInvoice = { invoice ->
                    viewModel.deleteInvoice(invoice)
                },
                onPayInvoice = { invoice ->
                    invoiceToPay = invoice
                }
            )
        }
        else -> {
            DashboardListScreen(
                uiState = uiState,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                onClientSelected = { viewModel.selectClient(it) },
                onAddNewClient = {
                    clientToEdit = null
                    showAddEditDialog = true
                },
                onReseedDemoData = { viewModel.reseedData() },
                onOpenWorkers = { viewModel.setWorkersScreenVisible(true) },
                onOpenAdminPanel = { viewModel.setAdminPanelVisible(true) },
                onOpenCommercialAdvisor = { viewModel.setCommercialAdvisorPortalVisible(true) },
                onSignOut = onSignOut
            )
        }
    }

    // Modal: Add or Edit Client
    if (showAddEditDialog) {
        AddEditClientDialog(
            initialClient = clientToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { client ->
                viewModel.saveClient(client) { savedId ->
                    if (uiState.selectedClientId != null) {
                        viewModel.selectClient(savedId)
                    }
                }
                showAddEditDialog = false
            }
        )
    }

    // Modal: Add Installed Service
    serviceClientId?.let { cId ->
        AddServiceDialog(
            clientId = cId,
            onDismiss = { serviceClientId = null },
            onConfirm = { newService ->
                viewModel.saveService(newService)
                serviceClientId = null
            }
        )
    }

    // Modal: Add Invoice / Charge
    invoiceClientId?.let { cId ->
        val clientItem = uiState.allClients.firstOrNull { it.client.id == cId }
        val defaultAmount = clientItem?.activeService?.monthlyCost ?: 549.00
        AddInvoiceDialog(
            clientId = cId,
            defaultAmount = defaultAmount,
            onDismiss = { invoiceClientId = null },
            onConfirm = { newInvoice ->
                viewModel.saveInvoice(newInvoice)
                invoiceClientId = null
            }
        )
    }

    // Modal: Register Payment
    invoiceToPay?.let { inv ->
        RegisterPaymentDialog(
            invoice = inv,
            onDismiss = { invoiceToPay = null },
            onConfirm = { pDate, pMethod, pFolio, amount ->
                viewModel.registerPayment(
                    invoiceId = inv.id,
                    paymentDate = pDate,
                    paymentMethod = pMethod,
                    receiptFolio = pFolio,
                    amount = amount
                )
                invoiceToPay = null
            }
        )
    }
}
