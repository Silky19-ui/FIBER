package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ClientCategory(
    val key: String,
    val title: String,
    val shortLabel: String,
    val colorHex: Long,
    val description: String
) {
    LEAD(
        key = "LEAD",
        title = "Lead / Consulta",
        shortLabel = "Lead",
        colorHex = 0xFFFB8C00, // Vibrant Amber/Orange
        description = "Contacto nuevo con interés, requiere validación de cobertura y caja NAP"
    ),
    POTENCIAL(
        key = "POTENCIAL",
        title = "Cliente Potencial",
        shortLabel = "Potencial",
        colorHex = 0xFF0288D1, // Telecom Cyan/Blue
        description = "Factibilidad aprobada, cotización enviada y pendiente de confirmación/firma"
    ),
    CLIENTE(
        key = "CLIENTE",
        title = "Cliente Activo",
        shortLabel = "Cliente",
        colorHex = 0xFF2E7D32, // Fiber Green
        description = "Servicio de fibra óptica instalado y funcionando con contrato activo"
    ),
    SUSPENDIDO(
        key = "SUSPENDIDO",
        title = "Servicio Suspendido",
        shortLabel = "Suspendido",
        colorHex = 0xFFC62828, // Crimson Red
        description = "Servicio cortado temporalmente por mora en estado de cuenta"
    );

    companion object {
        fun fromKey(key: String): ClientCategory {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: LEAD
        }
    }
}

enum class ServiceStatus(val key: String, val label: String, val colorHex: Long) {
    ACTIVO("ACTIVO", "Activo", 0xFF2E7D32),
    EN_PROCESO("EN_PROCESO", "En Instalación", 0xFF0288D1),
    MANTENIMIENTO("MANTENIMIENTO", "Mantenimiento", 0xFFF57C00),
    RETIRADO("RETIRADO", "Retirado", 0xFF757575);

    companion object {
        fun fromKey(key: String): ServiceStatus {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ACTIVO
        }
    }
}

enum class InvoiceStatus(val key: String, val label: String, val colorHex: Long) {
    PAGADO("PAGADO", "Pagado", 0xFF2E7D32),
    PENDIENTE("PENDIENTE", "Pendiente", 0xFFF57C00),
    VENCIDO("VENCIDO", "Vencido", 0xFFC62828);

    companion object {
        fun fromKey(key: String): InvoiceStatus {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: PENDIENTE
        }
    }
}

enum class RoleDepartment(
    val key: String,
    val title: String,
    val colorHex: Long,
    val description: String
) {
    OPERACIONES_TECNICAS(
        key = "OPERACIONES_TECNICAS",
        title = "Operaciones Técnicas FTTH",
        colorHex = 0xFF00838F, // Deep Cyan
        description = "Instalaciones de fibra, verificación de potencia óptica, cajas NAP y ONTs"
    ),
    COMERCIAL_VENTAS(
        key = "COMERCIAL_VENTAS",
        title = "Comercial y Ventas",
        colorHex = 0xFFFB8C00, // Vibrant Amber
        description = "Prospección de Leads, validación de factibilidad y cotización de planes"
    ),
    FINANZAS_COBRANZA(
        key = "FINANZAS_COBRANZA",
        title = "Finanzas y Cobranza",
        colorHex = 0xFF2E7D32, // Fiber Green
        description = "Estados de cuenta, emisión de facturas y recaudación de pagos"
    ),
    ADMINISTRACION(
        key = "ADMINISTRACION",
        title = "Administración y Dirección",
        colorHex = 0xFF6A1B9A, // Purple
        description = "Supervisión global, gestión de permisos y configuración de la empresa"
    );

    companion object {
        fun fromKey(key: String): RoleDepartment {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OPERACIONES_TECNICAS
        }
    }
}

data class WorkerFunctionItem(
    val id: String,
    val title: String,
    val description: String,
    val department: RoleDepartment
)

object WorkerFunctions {
    const val FEASIBILITY_CHECK = "FEASIBILITY_CHECK" // Factibilidad técnica en postes y splitters
    const val FTTH_INSTALL = "FTTH_INSTALL" // Instalación de fibra y equipos ONT
    const val NETWORK_PROVISION = "NETWORK_PROVISION" // Alta de cajas NAP y puertos OLT
    const val LEADS_MANAGEMENT = "LEADS_MANAGEMENT" // Captura y seguimiento de Leads
    const val BILLING_INVOICING = "BILLING_INVOICING" // Emisión de facturas y cargos
    const val PAYMENT_COLLECTION = "PAYMENT_COLLECTION" // Cobro y acreditación de pagos
    const val SERVICE_SUSPENSION = "SERVICE_SUSPENSION" // Gestión de cortes y reactivaciones
    const val MANAGE_WORKERS = "MANAGE_WORKERS" // Gestión de roles y funciones de empleados

    val allFunctions = listOf(
        WorkerFunctionItem(FEASIBILITY_CHECK, "Validación de Factibilidad", "Verificar cajas NAP, puertos y potencia dBm en campo", RoleDepartment.OPERACIONES_TECNICAS),
        WorkerFunctionItem(FTTH_INSTALL, "Instalación de Fibra y ONT", "Tendido de cable drop, roseta óptica y configuración de router WiFi", RoleDepartment.OPERACIONES_TECNICAS),
        WorkerFunctionItem(NETWORK_PROVISION, "Aprovisionamiento de Red", "Configuración de nodos OLT y splitters ópticos", RoleDepartment.OPERACIONES_TECNICAS),
        WorkerFunctionItem(LEADS_MANAGEMENT, "Gestión Comercial y Leads", "Captar prospectos, enviar cotizaciones y cerrar contratos", RoleDepartment.COMERCIAL_VENTAS),
        WorkerFunctionItem(BILLING_INVOICING, "Emisión de Facturación", "Generar folios mensuales y cargos por instalación", RoleDepartment.FINANZAS_COBRANZA),
        WorkerFunctionItem(PAYMENT_COLLECTION, "Cobranza y Pagos", "Registrar pagos SPEI, tarjeta y efectivo en estado de cuenta", RoleDepartment.FINANZAS_COBRANZA),
        WorkerFunctionItem(SERVICE_SUSPENSION, "Cortes y Reactivaciones", "Suspender o restablecer servicio por mora", RoleDepartment.FINANZAS_COBRANZA),
        WorkerFunctionItem(MANAGE_WORKERS, "Gestión de Personal", "Añadir trabajadores y asignar funciones y roles", RoleDepartment.ADMINISTRACION)
    )

    fun getDefaultFunctionsForRole(role: String): List<String> {
        return when {
            role.contains("Técnico", ignoreCase = true) || role.contains("Instalador", ignoreCase = true) ->
                listOf(FEASIBILITY_CHECK, FTTH_INSTALL)
            role.contains("Soporte", ignoreCase = true) ->
                listOf(FEASIBILITY_CHECK, FTTH_INSTALL, NETWORK_PROVISION)
            role.contains("Comercial", ignoreCase = true) || role.contains("Ventas", ignoreCase = true) || role.contains("Leads", ignoreCase = true) ->
                listOf(LEADS_MANAGEMENT, FEASIBILITY_CHECK)
            role.contains("Cobranza", ignoreCase = true) || role.contains("Facturación", ignoreCase = true) || role.contains("Finanzas", ignoreCase = true) ->
                listOf(BILLING_INVOICING, PAYMENT_COLLECTION, SERVICE_SUSPENSION)
            role.contains("Admin", ignoreCase = true) || role.contains("Supervisor", ignoreCase = true) ->
                allFunctions.map { it.id }
            else -> listOf(FEASIBILITY_CHECK, LEADS_MANAGEMENT)
        }
    }
}

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val businessOrDocument: String = "", // DNI / RFC / Razón Social
    val phone: String,
    val email: String = "",
    val category: String = ClientCategory.LEAD.key,
    val address: String, // Calle, Número exterior/interior
    val colonyOrSector: String, // Colonia / Fraccionamiento / Sector
    val city: String,
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    // Network & FTTH infrastructure parameters
    val napBoxId: String = "", // e.g. "NAP-C04"
    val napPort: Int = 1, // Port 1 to 16
    val opticalPowerDbm: Double = -19.5, // Standard GPON received optical power (dBm)
    val dropMeters: Int = 45, // Fiber drop length in meters
    val oltNode: String = "", // e.g. "OLT-ZTE-CENTRAL"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "installed_services",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class InstalledServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val planName: String, // "Fibra Simétrica 300 Mbps"
    val downloadSpeedMbps: Int, // 300
    val uploadSpeedMbps: Int, // 300
    val monthlyCost: Double, // 549.00
    val installationDate: String, // "14/10/2025"
    val ontModel: String, // "Huawei HG8245H5 Dual-Band"
    val ontSerial: String, // "HWTC4857A19"
    val ontMac: String = "",
    val wifiSsid: String = "", // "FibraOptica_Cliente"
    val wifiPassword: String = "",
    val status: String = ServiceStatus.ACTIVO.key,
    val technicianName: String = "", // "Ing. Carlos Mendoza"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val invoiceNumber: String, // "FAC-2026-0082"
    val concept: String, // "Mensualidad Fibra 300 Mbps - Octubre 2026"
    val amount: Double,
    val paidAmount: Double = 0.0,
    val issueDate: String, // "01/10/2026"
    val dueDate: String, // "15/10/2026"
    val paymentDate: String? = null,
    val paymentMethod: String? = null, // "Transferencia SPEI", "Efectivo", "Tarjeta"
    val status: String = InvoiceStatus.PENDIENTE.key,
    val receiptFolio: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ClientWithDetails(
    @Embedded val client: ClientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "clientId"
    )
    val services: List<InstalledServiceEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "clientId"
    )
    val invoices: List<InvoiceItemEntity>
) {
    val totalInvoiced: Double get() = invoices.sumOf { it.amount }
    val totalPaid: Double get() = invoices.filter { it.status == InvoiceStatus.PAGADO.key }.sumOf { it.amount }
    val balanceDue: Double get() = invoices.filter { it.status != InvoiceStatus.PAGADO.key }.sumOf { it.amount }
    val activeService: InstalledServiceEntity? get() = services.firstOrNull { it.status == ServiceStatus.ACTIVO.key } ?: services.firstOrNull()
    val hasOverdueInvoices: Boolean get() = invoices.any { it.status == InvoiceStatus.VENCIDO.key }
}

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val role: String = "Técnico Instalador FTTH",
    val departmentKey: String = RoleDepartment.OPERACIONES_TECNICAS.key,
    val assignedZone: String = "Zona Centro (OLT-CENTRO)",
    val status: String = "ACTIVO",
    val employeeNumber: String = "",
    val functionsCsv: String = "FEASIBILITY_CHECK,FTTH_INSTALL",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun hasFunction(functionId: String): Boolean {
        return functionsCsv.split(",").map { it.trim() }.contains(functionId)
    }

    fun getFunctionsList(): List<String> {
        return if (functionsCsv.isBlank()) emptyList() else functionsCsv.split(",").map { it.trim() }
    }
}
