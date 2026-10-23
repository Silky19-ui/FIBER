package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithDetails
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Transaction
    @Query("SELECT * FROM clients ORDER BY updatedAt DESC")
    fun getAllClientsWithDetails(): Flow<List<ClientWithDetails>>

    @Transaction
    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    fun getClientWithDetails(clientId: Long): Flow<ClientWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("UPDATE clients SET category = :category, updatedAt = :updatedAt WHERE id = :clientId")
    suspend fun updateCategory(clientId: Long, category: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM clients WHERE category = :category")
    fun countByCategory(category: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getCount(): Int
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM installed_services WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getServicesForClient(clientId: Long): Flow<List<InstalledServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: InstalledServiceEntity): Long

    @Update
    suspend fun updateService(service: InstalledServiceEntity)

    @Delete
    suspend fun deleteService(service: InstalledServiceEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getInvoicesForClient(clientId: Long): Flow<List<InvoiceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceItemEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceItemEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceItemEntity)

    @Query("""
        UPDATE invoices 
        SET status = 'PAGADO', 
            paidAmount = :paidAmount, 
            paymentDate = :paymentDate, 
            paymentMethod = :paymentMethod, 
            receiptFolio = :receiptFolio 
        WHERE id = :invoiceId
    """)
    suspend fun markInvoiceAsPaid(
        invoiceId: Long,
        paymentDate: String,
        paymentMethod: String,
        receiptFolio: String,
        paidAmount: Double
    )
}

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY fullName ASC")
    fun getAllWorkers(): Flow<List<com.example.data.model.WorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: com.example.data.model.WorkerEntity): Long

    @Update
    suspend fun updateWorker(worker: com.example.data.model.WorkerEntity)

    @Delete
    suspend fun deleteWorker(worker: com.example.data.model.WorkerEntity)

    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getCount(): Int
}

