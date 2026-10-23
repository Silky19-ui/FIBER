package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ClientCategory
import com.example.data.model.ClientEntity
import com.example.data.model.InstalledServiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import com.example.data.model.ServiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClientEntity::class,
        InstalledServiceEntity::class,
        InvoiceItemEntity::class,
        com.example.data.model.WorkerEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun serviceDao(): ServiceDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun workerDao(): WorkerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiber_crm_database"
                )
                    .addCallback(DatabaseSeedCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialFiberCrmData(database)
                }
            }
        }
    }
}

suspend fun populateInitialFiberCrmData(database: AppDatabase) {
    val clientDao = database.clientDao()
    val serviceDao = database.serviceDao()
    val invoiceDao = database.invoiceDao()

    if (clientDao.getCount() > 0) return

    // 1. LEAD: Mariana Soto Rivera
    val leadId = clientDao.insertClient(
        ClientEntity(
            fullName = "Mariana Soto Rivera",
            businessOrDocument = "INE 39482103",
            phone = "+52 55 1234 5678",
            email = "mariana.soto@gmail.com",
            category = ClientCategory.LEAD.key,
            address = "Av. De las Rosas 241",
            colonyOrSector = "Col. Jardines de la Paz",
            city = "Guadalajara, JAL",
            postalCode = "44860",
            latitude = 20.6597,
            longitude = -103.3496,
            napBoxId = "NAP-C08-SUR",
            napPort = 2,
            opticalPowerDbm = -18.2,
            dropMeters = 60,
            oltNode = "OLT-SUR-GPON",
            notes = "Interesada en plan de 300 Mbps para trabajo remoto. Requiere validar disponibilidad de splitter en poste."
        )
    )

    // 2. LEAD: Constructora Horizonte
    clientDao.insertClient(
        ClientEntity(
            fullName = "Constructora Horizonte S.A. de C.V.",
            businessOrDocument = "RFC: CHZ-180422-9T4",
            phone = "+52 33 4567 8901",
            email = "proyectos@constructorahorizonte.mx",
            category = ClientCategory.LEAD.key,
            address = "Av. Circunvalación 890",
            colonyOrSector = "Col. Chapultepec Country",
            city = "Guadalajara, JAL",
            postalCode = "44620",
            latitude = 20.6980,
            longitude = -103.3640,
            napBoxId = "NAP-C03-NORTE",
            napPort = 5,
            opticalPowerDbm = -19.0,
            dropMeters = 85,
            oltNode = "OLT-NORTE-GPON",
            notes = "Solicitan cotización para nueva oficina con 20 puntos de red y enlace simétrico."
        )
    )

    // 3. POTENCIAL: Taller Mecánico El Rayo
    val potId = clientDao.insertClient(
        ClientEntity(
            fullName = "Taller Mecánico El Rayo",
            businessOrDocument = "RFC: TMR-910412-K71",
            phone = "+52 55 8765 4321",
            email = "gerencia@tallerelrayo.com",
            category = ClientCategory.POTENCIAL.key,
            address = "Calzada Independencia Norte 1845",
            colonyOrSector = "Col. San Juan de Dios",
            city = "Guadalajara, JAL",
            postalCode = "44360",
            latitude = 20.6775,
            longitude = -103.3421,
            napBoxId = "NAP-C14-ORIENTE",
            napPort = 6,
            opticalPowerDbm = -20.1,
            dropMeters = 40,
            oltNode = "OLT-CENTRO-HUAWEI",
            notes = "Factibilidad técnica aprobada (caja NAP-C14 con puerto libre y -20.1 dBm). Cotización de 500 Mbps enviada."
        )
    )

    // 4. CLIENTE ACTIVO: Supermercado La Canasta
    val client1Id = clientDao.insertClient(
        ClientEntity(
            fullName = "Supermercado La Canasta S.A.",
            businessOrDocument = "RFC: SCA-160803-9A2",
            phone = "+52 33 9988 7766",
            email = "contacto@lacanasta.com.mx",
            category = ClientCategory.CLIENTE.key,
            address = "Av. Hidalgo 1020 Int. 4",
            colonyOrSector = "Zona Comercial Moderna",
            city = "Zapopan, JAL",
            postalCode = "45130",
            latitude = 20.7167,
            longitude = -103.3833,
            napBoxId = "NAP-D02-NORTE",
            napPort = 4,
            opticalPowerDbm = -17.8,
            dropMeters = 75,
            oltNode = "OLT-NORTE-ZTE",
            notes = "Cliente empresarial crítico. Enlace simétrico de 1 Gbps con ONT Huawei y SLA 99.8%."
        )
    )
    serviceDao.insertService(
        InstalledServiceEntity(
            clientId = client1Id,
            planName = "Fibra Dedicada 1 Gbps Pro",
            downloadSpeedMbps = 1000,
            uploadSpeedMbps = 1000,
            monthlyCost = 1499.00,
            installationDate = "10/01/2026",
            ontModel = "Huawei EchoLife EG8145V5",
            ontSerial = "HWTC92841029",
            ontMac = "E4:68:A3:89:12:01",
            wifiSsid = "LaCanasta_POS_Segura",
            wifiPassword = "CanastaFTTH2026*",
            status = ServiceStatus.ACTIVO.key,
            technicianName = "Ing. Roberto Alarcón",
            notes = "Instalación en rack principal de servidores. Potencia óptica recibida: -17.8 dBm (Óptima)."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client1Id,
            invoiceNumber = "FAC-2026-0042",
            concept = "Mensualidad Fibra Dedicada 1 Gbps - Octubre 2026",
            amount = 1499.00,
            paidAmount = 1499.00,
            issueDate = "01/10/2026",
            dueDate = "15/10/2026",
            paymentDate = "05/10/2026",
            paymentMethod = "Transferencia SPEI",
            status = InvoiceStatus.PAGADO.key,
            receiptFolio = "SPEI-738910283",
            notes = "Pago acreditado en cuenta bancaria Banorte."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client1Id,
            invoiceNumber = "FAC-2026-0019",
            concept = "Mensualidad Fibra Dedicada 1 Gbps - Septiembre 2026",
            amount = 1499.00,
            paidAmount = 1499.00,
            issueDate = "01/09/2026",
            dueDate = "15/09/2026",
            paymentDate = "03/09/2026",
            paymentMethod = "Transferencia SPEI",
            status = InvoiceStatus.PAGADO.key,
            receiptFolio = "SPEI-628190331",
            notes = "Pago puntual."
        )
    )

    // 5. CLIENTE ACTIVO: Familia González Mendoza
    val client2Id = clientDao.insertClient(
        ClientEntity(
            fullName = "Carlos & Sofía Mendoza",
            businessOrDocument = "CURP: MEMC880521",
            phone = "+52 33 2244 6688",
            email = "carlos.mendoza.tech@outlook.com",
            category = ClientCategory.CLIENTE.key,
            address = "Paseo de las Lomas 312",
            colonyOrSector = "Fracc. Lomas Altas",
            city = "Zapopan, JAL",
            postalCode = "45128",
            latitude = 20.6942,
            longitude = -103.4172,
            napBoxId = "NAP-A03-PONIENTE",
            napPort = 1,
            opticalPowerDbm = -19.4,
            dropMeters = 35,
            oltNode = "OLT-PONIENTE-GPON",
            notes = "Servicio residencial simétrico de 300 Mbps con router WiFi 6."
        )
    )
    serviceDao.insertService(
        InstalledServiceEntity(
            clientId = client2Id,
            planName = "Fibra Simétrica 300 Mbps Residencial",
            downloadSpeedMbps = 300,
            uploadSpeedMbps = 300,
            monthlyCost = 549.00,
            installationDate = "20/03/2026",
            ontModel = "ZTE ZXHN F670L Dual Band",
            ontSerial = "ZTEG64810293",
            ontMac = "C8:3A:35:B9:44:E2",
            wifiSsid = "FibraMendoza_5G",
            wifiPassword = "LomasWifi2026!",
            status = ServiceStatus.ACTIVO.key,
            technicianName = "Tec. Andrés Gómez",
            notes = "Cable drop fijado en fachada con tensores y herrajes tipo J."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client2Id,
            invoiceNumber = "FAC-2026-0098",
            concept = "Mensualidad Fibra 300 Mbps - Octubre 2026",
            amount = 549.00,
            paidAmount = 549.00,
            issueDate = "01/10/2026",
            dueDate = "15/10/2026",
            paymentDate = "12/10/2026",
            paymentMethod = "Tarjeta de Débito (App)",
            status = InvoiceStatus.PAGADO.key,
            receiptFolio = "STRIPE-CHG-9842",
            notes = "Pago con tarjeta en pasarela digital."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client2Id,
            invoiceNumber = "FAC-2026-0071",
            concept = "Mensualidad Fibra 300 Mbps - Septiembre 2026",
            amount = 549.00,
            paidAmount = 549.00,
            issueDate = "01/09/2026",
            dueDate = "15/09/2026",
            paymentDate = "11/09/2026",
            paymentMethod = "Tarjeta de Débito",
            status = InvoiceStatus.PAGADO.key,
            receiptFolio = "STRIPE-CHG-8411",
            notes = "Pago procesado exitosamente."
        )
    )

    // 6. CLIENTE SUSPENDIDO (Por mora / Factura vencida): Cafetería El Grano de Oro
    val client3Id = clientDao.insertClient(
        ClientEntity(
            fullName = "Cafetería El Grano de Oro",
            businessOrDocument = "RFC: CGO-190401-23A",
            phone = "+52 33 1122 3344",
            email = "contacto@granodeoro.cafe",
            category = ClientCategory.SUSPENDIDO.key,
            address = "Calle Juárez 480",
            colonyOrSector = "Centro Histórico",
            city = "Guadalajara, JAL",
            postalCode = "44100",
            latitude = 20.6750,
            longitude = -103.3475,
            napBoxId = "NAP-C01-CENTRO",
            napPort = 8,
            opticalPowerDbm = -21.3,
            dropMeters = 80,
            oltNode = "OLT-CENTRO-HUAWEI",
            notes = "Corte lógico en OLT ejecutado por mora. Desbloqueo automático al saldar adeudo en estado de cuenta."
        )
    )
    serviceDao.insertService(
        InstalledServiceEntity(
            clientId = client3Id,
            planName = "Fibra Pyme 200 Mbps Comercial",
            downloadSpeedMbps = 200,
            uploadSpeedMbps = 200,
            monthlyCost = 699.00,
            installationDate = "05/11/2025",
            ontModel = "FiberHome HG6145F",
            ontSerial = "FHTT58190234",
            ontMac = "70:8B:CD:45:90:1B",
            wifiSsid = "CafeGranoOro_Clientes",
            wifiPassword = "CafeInternet2026",
            status = ServiceStatus.MANTENIMIENTO.key,
            technicianName = "Tec. Miguel Peña",
            notes = "Servicio pausado administrativamente en OLT."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client3Id,
            invoiceNumber = "FAC-2026-0105",
            concept = "Mensualidad Fibra Pyme 200 Mbps - Octubre 2026",
            amount = 699.00,
            paidAmount = 0.0,
            issueDate = "01/10/2026",
            dueDate = "10/10/2026",
            paymentDate = null,
            paymentMethod = null,
            status = InvoiceStatus.VENCIDO.key,
            receiptFolio = "",
            notes = "Factura vencida por 12 días."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client3Id,
            invoiceNumber = "FAC-2026-0080",
            concept = "Mensualidad Fibra Pyme 200 Mbps - Septiembre 2026",
            amount = 699.00,
            paidAmount = 0.0,
            issueDate = "01/09/2026",
            dueDate = "10/09/2026",
            paymentDate = null,
            paymentMethod = null,
            status = InvoiceStatus.VENCIDO.key,
            receiptFolio = "",
            notes = "Mora acumulada pendiente de pago."
        )
    )
    invoiceDao.insertInvoice(
        InvoiceItemEntity(
            clientId = client3Id,
            invoiceNumber = "FAC-2026-0055",
            concept = "Mensualidad Fibra Pyme 200 Mbps - Agosto 2026",
            amount = 699.00,
            paidAmount = 699.00,
            issueDate = "01/08/2026",
            dueDate = "10/08/2026",
            paymentDate = "04/08/2026",
            paymentMethod = "Efectivo OXXO",
            status = InvoiceStatus.PAGADO.key,
            receiptFolio = "OXXO-981042",
            notes = "Acreditado correctamente."
        )
    )

    val workerDao = database.workerDao()
    if (workerDao.getCount() == 0) {
        workerDao.insertWorker(
            com.example.data.model.WorkerEntity(
                fullName = "Ing. Carlos Mendoza",
                email = "carlos.mendoza@fibercrm.com",
                phone = "+52 33 2244 5566",
                role = "Técnico Instalador FTTH",
                departmentKey = com.example.data.model.RoleDepartment.OPERACIONES_TECNICAS.key,
                assignedZone = "Zona Sur (OLT-SUR-GPON)",
                status = "ACTIVO",
                employeeNumber = "TEC-014",
                functionsCsv = "FEASIBILITY_CHECK,FTTH_INSTALL"
            )
        )
        workerDao.insertWorker(
            com.example.data.model.WorkerEntity(
                fullName = "Lic. Mariana Valdés",
                email = "mariana.valdes@fibercrm.com",
                phone = "+52 33 1122 8899",
                role = "Asesora Comercial / Leads",
                departmentKey = com.example.data.model.RoleDepartment.COMERCIAL_VENTAS.key,
                assignedZone = "Zona Centro (OLT-CENTRO-HUAWEI)",
                status = "ACTIVO",
                employeeNumber = "COM-022",
                functionsCsv = "LEADS_MANAGEMENT,FEASIBILITY_CHECK"
            )
        )
        workerDao.insertWorker(
            com.example.data.model.WorkerEntity(
                fullName = "Tec. Roberto Alarcón",
                email = "roberto.alarcon@fibercrm.com",
                phone = "+52 33 7788 9900",
                role = "Soporte de Fibra y OLT",
                departmentKey = com.example.data.model.RoleDepartment.OPERACIONES_TECNICAS.key,
                assignedZone = "Zona Norte (OLT-NORTE-ZTE)",
                status = "EN_RUTA",
                employeeNumber = "SUP-009",
                functionsCsv = "FEASIBILITY_CHECK,FTTH_INSTALL,NETWORK_PROVISION"
            )
        )
        workerDao.insertWorker(
            com.example.data.model.WorkerEntity(
                fullName = "C.P. Beatriz Solís",
                email = "beatriz.solis@fibercrm.com",
                phone = "+52 33 5566 7788",
                role = "Especialista de Cobranza y Cuentas",
                departmentKey = com.example.data.model.RoleDepartment.FINANZAS_COBRANZA.key,
                assignedZone = "Oficina Matriz / Todos",
                status = "ACTIVO",
                employeeNumber = "FIN-005",
                functionsCsv = "BILLING_INVOICING,PAYMENT_COLLECTION,SERVICE_SUSPENSION"
            )
        )
        workerDao.insertWorker(
            com.example.data.model.WorkerEntity(
                fullName = "Ing. Alejandro Torres",
                email = "alejandro.torres@fibercrm.com",
                phone = "+52 33 9900 1122",
                role = "Supervisor General / Administrador",
                departmentKey = com.example.data.model.RoleDepartment.ADMINISTRACION.key,
                assignedZone = "Red Global Telecom",
                status = "ACTIVO",
                employeeNumber = "ADM-001",
                functionsCsv = "FEASIBILITY_CHECK,FTTH_INSTALL,NETWORK_PROVISION,LEADS_MANAGEMENT,BILLING_INVOICING,PAYMENT_COLLECTION,SERVICE_SUSPENSION,MANAGE_WORKERS"
            )
        )
    }
}

