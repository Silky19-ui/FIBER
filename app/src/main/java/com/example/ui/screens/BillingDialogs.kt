package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddInvoiceDialog(
    clientId: Long,
    defaultAmount: Double = 549.00,
    onDismiss: () -> Unit,
    onConfirm: (InvoiceItemEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val today = remember { dateFormat.format(Date()) }
    val dueDateDefault = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 15)
        dateFormat.format(cal.time)
    }

    val generatedFolio = remember { "FAC-2026-${(1000..9999).random()}" }

    var invoiceNumber by remember { mutableStateOf(generatedFolio) }
    var concept by remember { mutableStateOf("Mensualidad Internet Fibra Óptica") }
    var amount by remember { mutableDoubleStateOf(defaultAmount) }
    var issueDate by remember { mutableStateOf(today) }
    var dueDate by remember { mutableStateOf(dueDateDefault) }
    var status by remember { mutableStateOf(InvoiceStatus.PENDIENTE.key) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Generar Cargo / Factura",
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
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Número de Factura / Folio") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_invoice_number")
                )

                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto de Cobro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_invoice_concept")
                )

                OutlinedTextField(
                    value = String.format("%.2f", amount),
                    onValueChange = { it.toDoubleOrNull()?.let { a -> amount = a } },
                    label = { Text("Monto a Facturar ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_invoice_amount")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text("Emisión") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Vencimiento") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Estado Inicial:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == InvoiceStatus.PENDIENTE.key,
                        onClick = { status = InvoiceStatus.PENDIENTE.key },
                        label = { Text("Pendiente") }
                    )
                    FilterChip(
                        selected = status == InvoiceStatus.PAGADO.key,
                        onClick = { status = InvoiceStatus.PAGADO.key },
                        label = { Text("Pagado Inmediato") }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas internas") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        InvoiceItemEntity(
                            clientId = clientId,
                            invoiceNumber = invoiceNumber.trim(),
                            concept = concept.trim(),
                            amount = amount,
                            paidAmount = if (status == InvoiceStatus.PAGADO.key) amount else 0.0,
                            issueDate = issueDate.trim(),
                            dueDate = dueDate.trim(),
                            paymentDate = if (status == InvoiceStatus.PAGADO.key) issueDate.trim() else null,
                            paymentMethod = if (status == InvoiceStatus.PAGADO.key) "Efectivo" else null,
                            status = status,
                            notes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("btn_save_invoice")
            ) {
                Text("Emitir Factura")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RegisterPaymentDialog(
    invoice: InvoiceItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (paymentDate: String, paymentMethod: String, receiptFolio: String, amount: Double) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val today = remember { dateFormat.format(Date()) }

    var paymentDate by remember { mutableStateOf(today) }
    var selectedMethod by remember { mutableStateOf("Transferencia SPEI") }
    var receiptFolio by remember { mutableStateOf("SPEI-${(100000..999999).random()}") }
    var amountToPay by remember { mutableDoubleStateOf(invoice.amount) }

    val methods = listOf(
        "Transferencia SPEI",
        "Tarjeta Débito/Crédito",
        "Efectivo Tienda/Oficina",
        "Depósito OXXO",
        "Domiciliación Bancaria"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Pago de Factura",
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
                    text = "Factura: ${invoice.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = invoice.concept,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Método de Pago:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = {
                                selectedMethod = method
                                receiptFolio = when (method) {
                                    "Transferencia SPEI" -> "SPEI-${(100000..999999).random()}"
                                    "Tarjeta Débito/Crédito" -> "AUTH-${(100000..999999).random()}"
                                    "Depósito OXXO" -> "OXXO-${(100000..999999).random()}"
                                    else -> "REC-${(1000..9999).random()}"
                                }
                            },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = String.format("%.2f", amountToPay),
                    onValueChange = { it.toDoubleOrNull()?.let { a -> amountToPay = a } },
                    label = { Text("Monto Pagado ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_amount")
                )

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Fecha de Pago") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = receiptFolio,
                    onValueChange = { receiptFolio = it },
                    label = { Text("Folio de Comprobante / Transacción") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_folio")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(paymentDate.trim(), selectedMethod.trim(), receiptFolio.trim(), amountToPay)
                },
                modifier = Modifier.testTag("btn_confirm_payment")
            ) {
                Text("Confirmar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
