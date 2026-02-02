package com.example.reciclapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.repository.RewardsRepository
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.StoreBackground
import com.example.reciclapp.ui.theme.DarkerText
import com.example.reciclapp.ui.theme.PointsPillBg
import com.example.reciclapp.ui.theme.PointsTextGreen
import com.example.reciclapp.ui.theme.PriceTextGreen
import com.example.reciclapp.ui.theme.ConfirmGreen
import com.example.reciclapp.ui.theme.CancelRed
import com.example.reciclapp.ui.theme.Gold
import com.example.reciclapp.viewmodels.PointsViewModel

data class StoreItem(
    val id: Int,
    val name: String,
    val price: Int,
    val isFeatured: Boolean,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun StoreScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: PointsViewModel = viewModel(
        factory = PointsViewModelFactory(RetrofitClient.getApi(context))
    )
    val state by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<StoreItem?>(null) }

    val featuredItem = StoreItem(0, "Totebag Reciclapp", 9999, true, Icons.Default.ShoppingBag, Color.DarkGray)
    val gridItems = List(4) {
        StoreItem(it + 1, "Decoración Oro", 9999, false, Icons.Default.WorkspacePremium, Gold)
    }

    if (showDialog && selectedItem != null) {
        RedeemConfirmationDialog(
            item = selectedItem!!,
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = { ReciclappBottomBar(navController) },
        containerColor = StoreBackground
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 20.dp,
                bottom = paddingValues.calculateBottomPadding() + 20.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tienda",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = PointsPillBg,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = PointsTextGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isLoading) "..." else "${state.userBalance}",
                                    color = PointsTextGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Canjeá tus puntos por objetos de la tienda, logos, colores y más!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 2.dp, color = DarkerPrimary)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Items destacados",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                FeaturedItemCard(item = featuredItem) {
                    selectedItem = featuredItem
                    showDialog = true
                }
            }

            items(gridItems) { item ->
                StandardItemCard(item = item) {
                    selectedItem = item
                    showDialog = true
                }
            }
        }
    }
}

@Composable
fun FeaturedItemCard(item: StoreItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkerText),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.LightGray.copy(alpha = 0.5f), Color.White.copy(alpha = 0.8f))
                        )
                    )
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.DarkGray
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = PriceTextGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.price.toString(),
                        color = PriceTextGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StandardItemCard(item: StoreItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = item.color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PointsPillBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = PointsTextGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.price.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PointsTextGreen
                )
            }
        }
    }
}

@Composable
fun RedeemConfirmationDialog(item: StoreItem, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Confirmás la acción?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreen),
                shape = RoundedCornerShape(8.dp)
                // Removed invalid Modifier.weight(1f)
            ) {
                Text("Canjear", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CancelRed),
                shape = RoundedCornerShape(8.dp)
                // Removed invalid Modifier.weight(1f)
            ) {
                Text("No", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(16.dp)
    )
}

class PointsViewModelFactory(private val api: ReciclappApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PointsViewModel::class.java)) {
            val dummyRepo = RewardsRepository(api)
            @Suppress("UNCHECKED_CAST")
            return PointsViewModel(dummyRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}