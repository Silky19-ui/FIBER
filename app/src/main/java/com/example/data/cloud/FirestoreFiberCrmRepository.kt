package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ClientEntity
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio en la nube con Firebase Firestore para sincronización y almacenamiento
 * de clientes, leads, parámetros FTTH, servicios instalados y estados de cuenta.
 */
class FirestoreFiberCrmRepository(
    val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    /**
     * Constructor secundario que inicializa Firestore con el databaseId
     * obligatorio provisto por AI Studio en firebase_applet_config.xml.
     */
    constructor(context: Context) : this(
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val tag = "FirestoreFiberCRM"

    /**
     * Retorna el UID del usuario autenticado actual o un identificador de sesión.
     */
    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión para interactuar con Firestore.")
    }

    /**
     * Referencia a la colección de clientes del usuario: /users/{userId}/clients
     */
    private fun clientsCollection(userId: String = requireUserId()) =
        firestore.collection("users").document(userId).collection("clients")

    /**
     * Observa todos los clientes y leads en tiempo real.
     */
    fun observeClients(userId: String = requireUserId()): Flow<List<FirestoreClient>> = callbackFlow {
        val listener = clientsCollection(userId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error observando clientes de Firestore", error)
                    close(error)
                    return@addSnapshotListener
                }
                val clients = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreClient::class.java)
                } ?: emptyList()
                trySend(clients)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Guarda o actualiza un cliente/lead en Firestore.
     */
    suspend fun saveClient(client: FirestoreClient, userId: String = requireUserId()): String {
        val collection = clientsCollection(userId)
        val docRef = if (client.id.isBlank()) collection.document() else collection.document(client.id)
        val payload = client.copy(
            id = docRef.id,
            userId = userId
        )
        val dataMap = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "userId" to userId,
            "fullName" to payload.fullName,
            "businessOrDocument" to payload.businessOrDocument,
            "phone" to payload.phone,
            "email" to payload.email,
            "category" to payload.category,
            "address" to payload.address,
            "colonyOrSector" to payload.colonyOrSector,
            "city" to payload.city,
            "postalCode" to payload.postalCode,
            "latitude" to payload.latitude,
            "longitude" to payload.longitude,
            "napBoxId" to payload.napBoxId,
            "napPort" to payload.napPort,
            "opticalPowerDbm" to payload.opticalPowerDbm,
            "dropMeters" to payload.dropMeters,
            "oltNode" to payload.oltNode,
            "notes" to payload.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (client.id.isBlank()) {
            dataMap["createdAt"] = FieldValue.serverTimestamp()
        }

        docRef.set(dataMap, SetOptions.merge()).await()
        Log.d(tag, "Cliente guardado en Firestore: ${docRef.id}")
        return docRef.id
    }

    /**
     * Elimina un cliente de Firestore.
     */
    suspend fun deleteClient(clientId: String, userId: String = requireUserId()) {
        clientsCollection(userId).document(clientId).delete().await()
        Log.d(tag, "Cliente eliminado de Firestore: $clientId")
    }

    /**
     * Observa el historial de servicios instalados de un cliente específico:
     * /users/{userId}/clients/{clientId}/services
     */
    fun observeServices(clientId: String, userId: String = requireUserId()): Flow<List<FirestoreService>> = callbackFlow {
        val listener = clientsCollection(userId).document(clientId).collection("services")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error observando servicios", error)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(FirestoreService::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Registra o actualiza una instalación de servicio de fibra en la nube.
     */
    suspend fun saveService(
        service: FirestoreService,
        clientId: String,
        userId: String = requireUserId()
    ): String {
        val coll = clientsCollection(userId).document(clientId).collection("services")
        val docRef = if (service.id.isBlank()) coll.document() else coll.document(service.id)
        val dataMap = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "userId" to userId,
            "clientId" to clientId,
            "planName" to service.planName,
            "downloadSpeedMbps" to service.downloadSpeedMbps,
            "uploadSpeedMbps" to service.uploadSpeedMbps,
            "monthlyCost" to service.monthlyCost,
            "installationDate" to service.installationDate,
            "ontModel" to service.ontModel,
            "ontSerial" to service.ontSerial,
            "ontMac" to service.ontMac,
            "wifiSsid" to service.wifiSsid,
            "wifiPassword" to service.wifiPassword,
            "status" to service.status,
            "technicianName" to service.technicianName,
            "notes" to service.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (service.id.isBlank()) {
            dataMap["createdAt"] = FieldValue.serverTimestamp()
        }

        docRef.set(dataMap, SetOptions.merge()).await()
        return docRef.id
    }

    /**
     * Observa el estado de cuenta y facturas de un cliente:
     * /users/{userId}/clients/{clientId}/invoices
     */
    fun observeInvoices(clientId: String, userId: String = requireUserId()): Flow<List<FirestoreInvoice>> = callbackFlow {
        val listener = clientsCollection(userId).document(clientId).collection("invoices")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error observando facturas", error)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(FirestoreInvoice::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Registra una nueva factura / cargo en el estado de cuenta en Firestore.
     */
    suspend fun saveInvoice(
        invoice: FirestoreInvoice,
        clientId: String,
        userId: String = requireUserId()
    ): String {
        val coll = clientsCollection(userId).document(clientId).collection("invoices")
        val docRef = if (invoice.id.isBlank()) coll.document() else coll.document(invoice.id)
        val dataMap = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "userId" to userId,
            "clientId" to clientId,
            "invoiceNumber" to invoice.invoiceNumber,
            "concept" to invoice.concept,
            "amount" to invoice.amount,
            "paidAmount" to invoice.paidAmount,
            "issueDate" to invoice.issueDate,
            "dueDate" to invoice.dueDate,
            "paymentDate" to invoice.paymentDate,
            "paymentMethod" to invoice.paymentMethod,
            "status" to invoice.status,
            "receiptFolio" to invoice.receiptFolio,
            "notes" to invoice.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (invoice.id.isBlank()) {
            dataMap["createdAt"] = FieldValue.serverTimestamp()
        }

        docRef.set(dataMap, SetOptions.merge()).await()
        return docRef.id
    }

    /**
     * Registra un pago en una factura existente en Firestore.
     */
    suspend fun registerPayment(
        clientId: String,
        invoiceId: String,
        paymentDate: String,
        paymentMethod: String,
        receiptFolio: String,
        amount: Double,
        userId: String = requireUserId()
    ) {
        val docRef = clientsCollection(userId).document(clientId).collection("invoices").document(invoiceId)
        val updates = mapOf(
            "status" to "PAGADO",
            "paidAmount" to amount,
            "paymentDate" to paymentDate,
            "paymentMethod" to paymentMethod,
            "receiptFolio" to receiptFolio,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updates).await()
    }

    /**
     * Función utilitaria para sincronizar un cliente local (Room) hacia Firestore en la nube.
     */
    suspend fun syncLocalClientToCloud(
        client: ClientEntity,
        services: List<InstalledServiceEntity>,
        invoices: List<InvoiceItemEntity>,
        userId: String = requireUserId()
    ): String {
        val cloudClient = FirestoreClient(
            fullName = client.fullName,
            businessOrDocument = client.businessOrDocument,
            phone = client.phone,
            email = client.email,
            category = client.category,
            address = client.address,
            colonyOrSector = client.colonyOrSector,
            city = client.city,
            postalCode = client.postalCode,
            latitude = client.latitude,
            longitude = client.longitude,
            napBoxId = client.napBoxId,
            napPort = client.napPort,
            opticalPowerDbm = client.opticalPowerDbm,
            dropMeters = client.dropMeters,
            oltNode = client.oltNode,
            notes = client.notes
        )
        val cloudClientId = saveClient(cloudClient, userId)

        services.forEach { s ->
            val cloudService = FirestoreService(
                clientId = cloudClientId,
                planName = s.planName,
                downloadSpeedMbps = s.downloadSpeedMbps,
                uploadSpeedMbps = s.uploadSpeedMbps,
                monthlyCost = s.monthlyCost,
                installationDate = s.installationDate,
                ontModel = s.ontModel,
                ontSerial = s.ontSerial,
                ontMac = s.ontMac,
                wifiSsid = s.wifiSsid,
                wifiPassword = s.wifiPassword,
                status = s.status,
                technicianName = s.technicianName,
                notes = s.notes
            )
            saveService(cloudService, cloudClientId, userId)
        }

        invoices.forEach { inv ->
            val cloudInvoice = FirestoreInvoice(
                clientId = cloudClientId,
                invoiceNumber = inv.invoiceNumber,
                concept = inv.concept,
                amount = inv.amount,
                paidAmount = inv.paidAmount,
                issueDate = inv.issueDate,
                dueDate = inv.dueDate,
                paymentDate = inv.paymentDate,
                paymentMethod = inv.paymentMethod,
                status = inv.status,
                receiptFolio = inv.receiptFolio,
                notes = inv.notes
            )
            saveInvoice(cloudInvoice, cloudClientId, userId)
        }

        return cloudClientId
    }

    private fun workersCollection(userId: String = requireUserId()) =
        firestore.collection("users").document(userId).collection("workers")

    fun observeWorkers(userId: String = requireUserId()): Flow<List<FirestoreWorker>> = callbackFlow {
        val listener = workersCollection(userId)
            .orderBy("fullName")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error observando trabajadores", error)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(FirestoreWorker::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveWorker(worker: FirestoreWorker, userId: String = requireUserId()): String {
        val collection = workersCollection(userId)
        val docRef = if (worker.id.isBlank()) collection.document() else collection.document(worker.id)
        val dataMap = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "userId" to userId,
            "fullName" to worker.fullName,
            "email" to worker.email,
            "phone" to worker.phone,
            "role" to worker.role,
            "departmentKey" to worker.departmentKey,
            "assignedZone" to worker.assignedZone,
            "status" to worker.status,
            "employeeNumber" to worker.employeeNumber,
            "functionsCsv" to worker.functionsCsv,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (worker.id.isBlank()) {
            dataMap["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(dataMap, SetOptions.merge()).await()
        return docRef.id
    }

    suspend fun deleteWorker(workerId: String, userId: String = requireUserId()) {
        workersCollection(userId).document(workerId).delete().await()
    }
}

