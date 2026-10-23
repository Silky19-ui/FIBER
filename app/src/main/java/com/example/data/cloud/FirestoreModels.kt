package com.example.data.cloud

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreClient(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val businessOrDocument: String = "",
    val phone: String = "",
    val email: String = "",
    val category: String = "LEAD", // LEAD, POTENCIAL, CLIENTE, SUSPENDIDO
    val address: String = "",
    val colonyOrSector: String = "",
    val city: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val napBoxId: String = "",
    val napPort: Int = 1,
    val opticalPowerDbm: Double = -19.5,
    val dropMeters: Int = 50,
    val oltNode: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreService(
    @DocumentId val id: String = "",
    val userId: String = "",
    val clientId: String = "",
    val planName: String = "",
    val downloadSpeedMbps: Int = 300,
    val uploadSpeedMbps: Int = 300,
    val monthlyCost: Double = 549.0,
    val installationDate: String = "",
    val ontModel: String = "",
    val ontSerial: String = "",
    val ontMac: String = "",
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val status: String = "ACTIVO",
    val technicianName: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreInvoice(
    @DocumentId val id: String = "",
    val userId: String = "",
    val clientId: String = "",
    val invoiceNumber: String = "",
    val concept: String = "",
    val amount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val issueDate: String = "",
    val dueDate: String = "",
    val paymentDate: String? = null,
    val paymentMethod: String? = null,
    val status: String = "PENDIENTE", // PAGADO, PENDIENTE, VENCIDO
    val receiptFolio: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreWorker(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Técnico Instalador FTTH",
    val departmentKey: String = "OPERACIONES_TECNICAS",
    val assignedZone: String = "Zona Centro (OLT-CENTRO)",
    val status: String = "ACTIVO",
    val employeeNumber: String = "",
    val functionsCsv: String = "FEASIBILITY_CHECK,FTTH_INSTALL",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

