package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorCliente
import com.example.ui.theme.ColorLead
import com.example.ui.theme.ColorPotencial
import com.example.ui.theme.ColorSuspendido
import com.example.ui.viewmodel.CrmMetrics

@Composable
fun KpiMetricsSection(
    metrics: CrmMetrics,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            title = "Clientes Activos",
            value = "${metrics.clienteCount}",
            subtitle = "FTTH en línea",
            icon = Icons.Default.CheckCircle,
            accentColor = ColorCliente,
            isSelected = selectedFilter == "CLIENTE",
            onClick = { onFilterSelected("CLIENTE") },
            testTag = "kpi_clientes"
        )

        KpiCard(
            title = "Leads Nuevos",
            value = "${metrics.leadCount}",
            subtitle = "Por contactar",
            icon = Icons.Default.People,
            accentColor = ColorLead,
            isSelected = selectedFilter == "LEAD",
            onClick = { onFilterSelected("LEAD") },
            testTag = "kpi_leads"
        )

        KpiCard(
            title = "Potenciales",
            value = "${metrics.potencialCount}",
            subtitle = "Factibilidad OK",
            icon = Icons.Default.HourglassTop,
            accentColor = ColorPotencial,
            isSelected = selectedFilter == "POTENCIAL",
            onClick = { onFilterSelected("POTENCIAL") },
            testTag = "kpi_potenciales"
        )

        KpiCard(
            title = "Ingresos MRR",
            value = "$${String.format("%,.0f", metrics.estimatedMonthlyRevenue)}",
            subtitle = "Mensual activo",
            icon = Icons.Default.AttachMoney,
            accentColor = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = { onFilterSelected("CLIENTE") },
            testTag = "kpi_mrr"
        )

        KpiCard(
            title = "Mora / Pendiente",
            value = "$${String.format("%,.0f", metrics.totalOutstandingDebt)}",
            subtitle = if (metrics.suspendidoCount > 0) "${metrics.suspendidoCount} suspendidos" else "Por cobrar",
            icon = Icons.Default.Warning,
            accentColor = ColorSuspendido,
            isSelected = selectedFilter == "MOROSOS",
            onClick = { onFilterSelected("MOROSOS") },
            testTag = "kpi_morosos"
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.width(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
