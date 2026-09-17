package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.CrmUiState
import com.example.Order
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: CrmUiState,
    totalSales: Double,
    onSignOutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .size(34.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nova),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dashboard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Plano: " + state.merchantProfile.subscription.plan.uppercase(),
                                fontSize = 11.sp,
                                color = CosmicPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sair",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = CosmicBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Total Sales Hero Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("total_sales_card")
                        .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        CosmicPrimary.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    center = Offset(200f, 0f),
                                    radius = 450f
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vendas Totais Concluídas",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            CosmicPrimary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Vendas",
                                        tint = CosmicPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "R$ " + String.format(Locale.getDefault(), "%,.2f", totalSales),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pedidos Concluídos",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                val completedCount = state.orders.count { it.status.lowercase() == "concluído" || it.status.lowercase() == "completed" }
                                Text(
                                    text = "$completedCount pedidos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Quick Metrict Grid (3 cards in Row)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Estoque",
                        count = state.products.size.toString(),
                        icon = Icons.Default.Inventory,
                        tint = CosmicTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Clientes",
                        count = state.clients.size.toString(),
                        icon = Icons.Default.Group,
                        tint = CosmicSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Pedidos",
                        count = state.orders.size.toString(),
                        icon = Icons.Default.ReceiptLong,
                        tint = CosmicPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Canvas Sales Status Distribution Chart (Elegant Radial Ring Chart)
            item {
                val completed = state.orders.count { it.status.lowercase() == "concluído" || it.status.lowercase() == "completed" }
                val pending = state.orders.count { it.status.lowercase() == "pendente" || it.status.lowercase() == "pending" }
                val cancelled = state.orders.count { it.status.lowercase() == "cancelado" || it.status.lowercase() == "cancelled" }
                val totalOrders = state.orders.size

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sales_radial_chart_card")
                        .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Distribuição de Pedidos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Radial Ring Draw
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .testTag("sales_radial_chart"),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 32f
                                    val size = Size(size.width - strokeWidth, size.height - strokeWidth)
                                    val offset = Offset(strokeWidth / 2, strokeWidth / 2)

                                    if (totalOrders == 0) {
                                        // Empty state circle
                                        drawArc(
                                            color = Color.DarkGray,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                            topLeft = offset,
                                            size = size
                                        )
                                    } else {
                                        val sweepComp = (completed.toFloat() / totalOrders) * 360f
                                        val sweepPend = (pending.toFloat() / totalOrders) * 360f
                                        val sweepCanc = (cancelled.toFloat() / totalOrders) * 360f

                                        var startAngle = -90f

                                        if (sweepComp > 0) {
                                            drawArc(
                                                color = CosmicPrimary,
                                                startAngle = startAngle,
                                                sweepAngle = sweepComp,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                                topLeft = offset,
                                                size = size
                                            )
                                            startAngle += sweepComp
                                        }

                                        if (sweepPend > 0) {
                                            drawArc(
                                                color = Color(0xFFF7B500),
                                                startAngle = startAngle,
                                                sweepAngle = sweepPend,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                                topLeft = offset,
                                                size = size
                                            )
                                            startAngle += sweepPend
                                        }

                                        if (sweepCanc > 0) {
                                            drawArc(
                                                color = Color(0xFFE02020),
                                                startAngle = startAngle,
                                                sweepAngle = sweepCanc,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                                topLeft = offset,
                                                size = size
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalOrders",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Total",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Chart Legend Column
                            Column(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LegendItem(color = CosmicPrimary, label = "Concluído", count = completed, total = totalOrders)
                                LegendItem(color = Color(0xFFF7B500), label = "Pendente", count = pending, total = totalOrders)
                                LegendItem(color = Color(0xFFE02020), label = "Cancelado", count = cancelled, total = totalOrders)
                            }
                        }
                    }
                }
            }

            // 4. Recent Orders list
            item {
                Text(
                    text = "Últimos Pedidos Fechados",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            if (state.orders.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nenhum pedido cadastrado no momento.",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(state.orders.takeLast(8).reversed()) { order ->
                    OrderRowItem(order = order)
                }
            }

            // Spacer bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int, total: Int) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = "$count ($percentage%)",
            fontSize = 12.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderRowItem(order: Order) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.customerEmail,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt)),
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "R$ " + String.format(Locale.getDefault(), "%.2f", order.total),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CosmicPrimary
                )
                
                OrderStatusPill(status = order.status)
            }
        }
    }
}

@Composable
fun OrderStatusPill(status: String) {
    val cleanStatus = status.lowercase()
    val (bgColor, textColor) = when (cleanStatus) {
        "concluído", "completed" -> Pair(CosmicPrimary.copy(alpha = 0.15f), CosmicPrimary)
        "pendente", "pending" -> Pair(Color(0xFFFFB500).copy(alpha = 0.15f), Color(0xFFFFB500))
        else -> Pair(Color(0xFFE02020).copy(alpha = 0.15f), Color(0xFFE02020))
    }

    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
