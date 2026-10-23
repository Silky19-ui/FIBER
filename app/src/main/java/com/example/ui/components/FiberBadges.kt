package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientCategory
import com.example.data.model.InvoiceStatus
import com.example.data.model.ServiceStatus
import com.example.ui.theme.ColorCliente
import com.example.ui.theme.ColorLead
import com.example.ui.theme.ColorOpticalPowerBad
import com.example.ui.theme.ColorOpticalPowerGood
import com.example.ui.theme.ColorOpticalPowerWarning
import com.example.ui.theme.ColorPotencial
import com.example.ui.theme.ColorSuspendido

@Composable
fun CategoryBadge(
    category: ClientCategory,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (bgColor, textColor) = when (category) {
        ClientCategory.LEAD -> ColorLead.copy(alpha = 0.15f) to ColorLead
        ClientCategory.POTENCIAL -> ColorPotencial.copy(alpha = 0.15f) to ColorPotencial
        ClientCategory.CLIENTE -> ColorCliente.copy(alpha = 0.15f) to ColorCliente
        ClientCategory.SUSPENDIDO -> ColorSuspendido.copy(alpha = 0.15f) to ColorSuspendido
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = if (isCompact) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = if (isCompact) category.shortLabel else category.title,
                color = textColor,
                fontSize = if (isCompact) 11.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

@Composable
fun OpticalPowerIndicator(
    powerDbm: Double,
    modifier: Modifier = Modifier
) {
    val (statusColor, qualityLabel) = when {
        powerDbm in -23.0..-15.0 -> ColorOpticalPowerGood to "Óptima"
        powerDbm in -26.0..-23.0 -> ColorOpticalPowerWarning to "Aceptable"
        else -> ColorOpticalPowerBad to "Atenuada"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = statusColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NetworkCheck,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = " ${String.format("%.1f", powerDbm)} dBm ($qualityLabel)",
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InvoiceStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val invStatus = InvoiceStatus.fromKey(status)
    val (bgColor, textColor, icon) = when (invStatus) {
        InvoiceStatus.PAGADO -> Triple(ColorCliente.copy(alpha = 0.15f), ColorCliente, Icons.Default.CheckCircle)
        InvoiceStatus.PENDIENTE -> Triple(ColorLead.copy(alpha = 0.15f), ColorLead, Icons.Default.Info)
        InvoiceStatus.VENCIDO -> Triple(ColorSuspendido.copy(alpha = 0.15f), ColorSuspendido, Icons.Default.Warning)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " ${invStatus.label}",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ServiceStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val servStatus = ServiceStatus.fromKey(status)
    val (bgColor, textColor) = when (servStatus) {
        ServiceStatus.ACTIVO -> ColorCliente.copy(alpha = 0.15f) to ColorCliente
        ServiceStatus.EN_PROCESO -> ColorPotencial.copy(alpha = 0.15f) to ColorPotencial
        ServiceStatus.MANTENIMIENTO -> ColorLead.copy(alpha = 0.15f) to ColorLead
        ServiceStatus.RETIRADO -> Color.Gray.copy(alpha = 0.15f) to Color.Gray
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = servStatus.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
