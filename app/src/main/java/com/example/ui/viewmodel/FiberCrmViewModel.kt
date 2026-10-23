package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithDetails
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import com.example.data.model.ServiceStatus
import com.example.data.model.WorkerEntity
import com.example.data.repository.FiberCrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CrmMetrics(
    val totalCount: Int = 0,
    val leadCount: Int = 0,
    val potencialCount: Int = 0,
    val clienteCount: Int = 0,
    val suspendidoCount: Int = 0,
    val estimatedMonthlyRevenue: Double = 0.0,
    val totalOutstandingDebt: Double = 0.0
)

data class FiberCrmUiState(
    val allClients: List<ClientWithDetails> = emptyList(),
    val filteredClients: List<ClientWithDetails> = emptyList(),
    val commercialLeads: List<ClientWithDetails> = emptyList(),
    val workers: List<WorkerEntity> = emptyList(),
    val metrics: CrmMetrics = CrmMetrics(),
    val selectedCategoryFilter: String = "TODOS",
    val searchQuery: String = "",
    val selectedClientId: Long? = null,
    val showWorkersScreen: Boolean = false,
    val showAdminPanel: Boolean = false,
    val showCommercialAdvisorPortal: Boolean = false,
    val currentUserEmail: String? = null,
    val currentWorkerProfile: WorkerEntity? = null,
    val isCommercialAdvisorRole: Boolean = false,
    val isStrictCommercialAdvisor: Boolean = false,
    val isLoading: Boolean = false
)

private data class NavigationState(
    val showWorkers: Boolean,
    val showAdmin: Boolean,
    val showCommercialPortal: Boolean,
    val userEmail: String?
)

class FiberCrmViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FiberCrmRepository(database)

    private val _selectedCategoryFilter = MutableStateFlow("TODOS")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedClientId = MutableStateFlow<Long?>(null)
    val selectedClientId: StateFlow<Long?> = _selectedClientId

    private val _showWorkersScreen = MutableStateFlow(false)
    val showWorkersScreen: StateFlow<Boolean> = _showWorkersScreen

    private val _showAdminPanel = MutableStateFlow(false)
    val showAdminPanel: StateFlow<Boolean> = _showAdminPanel

    private val _showCommercialAdvisorPortal = MutableStateFlow(false)
    val showCommercialAdvisorPortal: StateFlow<Boolean> = _showCommercialAdvisorPortal

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    val uiState: StateFlow<FiberCrmUiState> = combine(
        combine(repository.allClientsWithDetails, repository.allWorkers) { clients, workers ->
            Pair(clients, workers)
        },
        _selectedCategoryFilter,
        _searchQuery,
        _selectedClientId,
        combine(_showWorkersScreen, _showAdminPanel, _showCommercialAdvisorPortal, _currentUserEmail) { w, a, c, email ->
            NavigationState(w, a, c, email)
        }
    ) { clientsAndWorkers, categoryFilter, query, selectedId, navState ->
        val clients = clientsAndWorkers.first
        val workers = clientsAndWorkers.second
        val showWorkers = navState.showWorkers
        val showAdmin = navState.showAdmin
        val showCommercialPortal = navState.showCommercialPortal
        val userEmail = navState.userEmail

        val matchedWorker = userEmail?.let { email ->
            workers.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        }

        val isAdvisorRole = matchedWorker?.let { w ->
            w.departmentKey.equals(com.example.data.model.RoleDepartment.COMERCIAL_VENTAS.key, ignoreCase = true) ||
                    w.role.contains("Comercial", ignoreCase = true) ||
                    w.role.contains("Ventas", ignoreCase = true) ||
                    w.role.contains("Leads", ignoreCase = true)
        } ?: false

        val isStrictAdvisor = isAdvisorRole && matchedWorker?.hasFunction(com.example.data.model.WorkerFunctions.MANAGE_WORKERS) != true

        // Leads exclusivos para el portal comercial (solo LEAD y POTENCIAL)
        val commercialLeads = clients.filter { item ->
            item.client.category.equals(ClientCategory.LEAD.key, ignoreCase = true) ||
                    item.client.category.equals(ClientCategory.POTENCIAL.key, ignoreCase = true)
        }

        val filtered = clients.filter { item ->
            val matchesCategory = when (categoryFilter) {
                "TODOS" -> true
                "MOROSOS" -> item.balanceDue > 0
                else -> item.client.category.equals(categoryFilter, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                item.client.fullName.lowercase().contains(q) ||
                        item.client.businessOrDocument.lowercase().contains(q) ||
                        item.client.phone.lowercase().contains(q) ||
                        item.client.address.lowercase().contains(q) ||
                        item.client.colonyOrSector.lowercase().contains(q) ||
                        item.client.napBoxId.lowercase().contains(q) ||
                        item.client.oltNode.lowercase().contains(q)
            }
            matchesCategory && matchesQuery
        }

        val leads = clients.count { it.client.category.equals(ClientCategory.LEAD.key, ignoreCase = true) }
        val potenciales = clients.count { it.client.category.equals(ClientCategory.POTENCIAL.key, ignoreCase = true) }
        val clientes = clients.count { it.client.category.equals(ClientCategory.CLIENTE.key, ignoreCase = true) }
        val suspendidos = clients.count { it.client.category.equals(ClientCategory.SUSPENDIDO.key, ignoreCase = true) }

        val mrr = clients.filter { it.client.category.equals(ClientCategory.CLIENTE.key, ignoreCase = true) }
            .sumOf { clientItem ->
                clientItem.services
                    .filter { it.status == ServiceStatus.ACTIVO.key }
                    .sumOf { it.monthlyCost }
            }

        val outstanding = clients.sumOf { it.balanceDue }

        val metrics = CrmMetrics(
            totalCount = clients.size,
            leadCount = leads,
            potencialCount = potenciales,
            clienteCount = clientes,
            suspendidoCount = suspendidos,
            estimatedMonthlyRevenue = mrr,
            totalOutstandingDebt = outstanding
        )

        FiberCrmUiState(
            allClients = clients,
            filteredClients = filtered,
            commercialLeads = commercialLeads,
            workers = workers,
            metrics = metrics,
            selectedCategoryFilter = categoryFilter,
            searchQuery = query,
            selectedClientId = selectedId,
            showWorkersScreen = showWorkers,
            showAdminPanel = showAdmin,
            showCommercialAdvisorPortal = showCommercialPortal,
            currentUserEmail = userEmail,
            currentWorkerProfile = matchedWorker,
            isCommercialAdvisorRole = isAdvisorRole,
            isStrictCommercialAdvisor = isStrictAdvisor,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FiberCrmUiState(isLoading = true)
    )

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectClient(clientId: Long?) {
        _selectedClientId.value = clientId
    }

    fun setWorkersScreenVisible(visible: Boolean) {
        _showWorkersScreen.value = visible
    }

    fun setAdminPanelVisible(visible: Boolean) {
        _showAdminPanel.value = visible
    }

    fun setCommercialAdvisorPortalVisible(visible: Boolean) {
        _showCommercialAdvisorPortal.value = visible
    }

    fun setUserEmail(email: String?) {
        _currentUserEmail.value = email
    }

    fun saveClient(client: ClientEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveClient(client)
            onSaved(id)
        }
    }

    fun updateClientCategory(clientId: Long, newCategory: ClientCategory) {
        viewModelScope.launch {
            repository.updateClientCategory(clientId, newCategory)
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            if (_selectedClientId.value == client.id) {
                _selectedClientId.value = null
            }
            repository.deleteClient(client)
        }
    }

    fun saveService(service: InstalledServiceEntity) {
        viewModelScope.launch {
            repository.saveInstalledService(service)
        }
    }

    fun deleteService(service: InstalledServiceEntity) {
        viewModelScope.launch {
            repository.deleteInstalledService(service)
        }
    }

    fun saveInvoice(invoice: InvoiceItemEntity) {
        viewModelScope.launch {
            repository.saveInvoice(invoice)
        }
    }

    fun deleteInvoice(invoice: InvoiceItemEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    fun registerPayment(
        invoiceId: Long,
        paymentDate: String,
        paymentMethod: String,
        receiptFolio: String,
        amount: Double
    ) {
        viewModelScope.launch {
            repository.registerPayment(
                invoiceId = invoiceId,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                receiptFolio = receiptFolio,
                paidAmount = amount
            )
        }
    }

    fun saveWorker(worker: WorkerEntity) {
        viewModelScope.launch {
            repository.saveWorker(worker)
        }
    }

    fun deleteWorker(worker: WorkerEntity) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
        }
    }

    fun reseedData() {
        viewModelScope.launch {
            repository.reseedDemoData()
        }
    }
}
