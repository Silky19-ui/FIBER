package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ClientDao
import com.example.data.local.InvoiceDao
import com.example.data.local.ServiceDao
import com.example.data.local.populateInitialFiberCrmData
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithDetails
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FiberCrmRepository(
    private val database: AppDatabase,
    private val clientDao: ClientDao = database.clientDao(),
    private val serviceDao: ServiceDao = database.serviceDao(),
    private val invoiceDao: InvoiceDao = database.invoiceDao(),
    private val workerDao: com.example.data.local.WorkerDao = database.workerDao()
) {
    val allWorkers: Flow<List<com.example.data.model.WorkerEntity>> =
        workerDao.getAllWorkers()
    val allClientsWithDetails: Flow<List<ClientWithDetails>> =
        clientDao.getAllClientsWithDetails()

    fun getClientWithDetails(clientId: Long): Flow<ClientWithDetails?> =
        clientDao.getClientWithDetails(clientId)

    suspend fun saveClient(client: ClientEntity): Long = withContext(Dispatchers.IO) {
        if (client.id == 0L) {
            clientDao.insertClient(client)
        } else {
            clientDao.updateClient(client.copy(updatedAt = System.currentTimeMillis()))
            client.id
        }
    }

    suspend fun deleteClient(client: ClientEntity) = withContext(Dispatchers.IO) {
        clientDao.deleteClient(client)
    }

    suspend fun updateClientCategory(clientId: Long, category: ClientCategory) = withContext(Dispatchers.IO) {
        clientDao.updateCategory(clientId, category.key, System.currentTimeMillis())
    }

    suspend fun saveInstalledService(service: InstalledServiceEntity): Long = withContext(Dispatchers.IO) {
        if (service.id == 0L) {
            serviceDao.insertService(service)
        } else {
            serviceDao.updateService(service)
            service.id
        }
    }

    suspend fun deleteInstalledService(service: InstalledServiceEntity) = withContext(Dispatchers.IO) {
        serviceDao.deleteService(service)
    }

    suspend fun saveInvoice(invoice: InvoiceItemEntity): Long = withContext(Dispatchers.IO) {
        if (invoice.id == 0L) {
            invoiceDao.insertInvoice(invoice)
        } else {
            invoiceDao.updateInvoice(invoice)
            invoice.id
        }
    }

    suspend fun deleteInvoice(invoice: InvoiceItemEntity) = withContext(Dispatchers.IO) {
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun registerPayment(
        invoiceId: Long,
        paymentDate: String,
        paymentMethod: String,
        receiptFolio: String,
        paidAmount: Double
    ) = withContext(Dispatchers.IO) {
        invoiceDao.markInvoiceAsPaid(
            invoiceId = invoiceId,
            paymentDate = paymentDate,
            paymentMethod = paymentMethod,
            receiptFolio = receiptFolio,
            paidAmount = paidAmount
        )
    }

    suspend fun reseedDemoData() = withContext(Dispatchers.IO) {
        populateInitialFiberCrmData(database)
    }

    suspend fun saveWorker(worker: com.example.data.model.WorkerEntity): Long = withContext(Dispatchers.IO) {
        if (worker.id == 0L) {
            workerDao.insertWorker(worker)
        } else {
            workerDao.updateWorker(worker)
            worker.id
        }
    }

    suspend fun deleteWorker(worker: com.example.data.model.WorkerEntity) = withContext(Dispatchers.IO) {
        workerDao.deleteWorker(worker)
    }
}

