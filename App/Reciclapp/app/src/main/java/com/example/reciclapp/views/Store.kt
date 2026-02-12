package com.example.reciclapp.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.R
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.components.ProfileDropdown
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.database.entities.ItemEntity
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.repository.RewardsRepository
import com.example.reciclapp.ui.theme.ConfirmGreen
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.DarkerText
import com.example.reciclapp.ui.theme.LightTextColor
import com.example.reciclapp.ui.theme.PointsPillBg
import com.example.reciclapp.ui.theme.PointsTextGreen
import com.example.reciclapp.ui.theme.PriceTextGreen
import com.example.reciclapp.ui.theme.StoreBackground
import com.example.reciclapp.viewmodels.PointsViewModel
import com.example.reciclapp.viewmodels.PointsViewModelFactory

data class StoreItem(
    val id: Int,
    val name: String,
    val price: Int,
    val isFeatured: Boolean,
    val iconResId: Int,
    val color: Color,
    val isOwned: Boolean = false
)

@Composable
fun StoreScreen(navController: NavController, tokenManager: TokenManager) {
    val context = LocalContext.current
    val popupController = LocalPopupState.current

    val viewModel: PointsViewModel = viewModel(
        factory = PointsViewModelFactory(context, RetrofitClient.getApi(context))
    )

    val state by viewModel.uiState.collectAsState()

    val featuredItems = remember(state.storeItems) {
        state.storeItems.filter { it.isFeatured }
    }
    val catalogItems = remember(state.storeItems) {
        state.storeItems.filter { !it.isFeatured }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<StoreItem?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { popupController.showError(it) }
    }

    if (showConfirmDialog && selectedItem != null) {
        RedeemConfirmationDialog(
            item = selectedItem!!,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                val item = selectedItem!!
                showConfirmDialog = false
                if (state.userBalance < item.price) {
                    popupController.showError("Créditos insuficientes.")
                } else {
                    viewModel.redeemItem(
                        itemId = item.id.toLong(),
                        onSuccess = { popupController.showSuccess("¡Canjeado: ${item.name}!") },
                        onError = { popupController.showError(it) }
                    )
                }
            }
        )
    }

    Scaffold(
        bottomBar = { ReciclappBottomBar(navController) },
        containerColor = StoreBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    bottom = 24.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    StoreHeader(state.userBalance, state.isLoading, navController, tokenManager)
                }

                // Si ya hay items, los mostramos
                if (featuredItems.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Items destacados",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkerText
                            )
                        )
                    }
                    items(featuredItems, span = { GridItemSpan(2) }) { item ->
                        FeaturedItemCard(item = item) {
                            if (item.isOwned) {
                                popupController.showError("Ya tenés este item")
                            } else {
                                selectedItem = item
                                showConfirmDialog = true
                            }
                        }
                    }
                }

                if (catalogItems.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Más para canjear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkerText
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(catalogItems) { item ->
                        StandardItemCard(item = item) {
                            if (item.isOwned) {
                                popupController.showError("Ya tenés este item")
                            } else {
                                selectedItem = item
                                showConfirmDialog = true
                            }
                        }
                    }
                }
            }

            if (state.isLoading && state.storeItems.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DarkerPrimary
                )
            }

            // 4. (Opcional) Mensaje si no hay items y ya terminó de cargar
            if (!state.isLoading && state.storeItems.isEmpty()) {
                Text(
                    text = "No hay items disponibles por el momento.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StoreHeader(
    balance: Int,
    isLoading: Boolean,
    navController: NavController,
    tokenManager: TokenManager
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tienda",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = DarkerText
            )
            Surface(color = PointsPillBg, shape = RoundedCornerShape(50)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Eco,
                        null,
                        tint = PointsTextGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLoading) "..." else "$balance",
                        color = PointsTextGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileDropdown(navController, tokenManager)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.4f))
    }
}

@Composable
fun FeaturedItemCard(item: StoreItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isOwned) Color.DarkGray.copy(alpha = 0.8f) else DarkerText
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = item.iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                tint = Color.Unspecified
            )
            Column(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (item.isOwned) {
                    Text("Obtenido", color = Color.LightGray, fontWeight = FontWeight.Bold)
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            null,
                            tint = PriceTextGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.price}",
                            color = PriceTextGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isOwned) Color(0xFFF5F5F5) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isOwned) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (item.isOwned) Color.LightGray.copy(alpha = 0.2f) else item.color.copy(
                    alpha = 0.1f
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (item.isOwned) Color.Gray else Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (item.isOwned) Color.Gray else DarkerText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (item.isOwned) {
                Text(
                    "Obtenido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PointsPillBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Eco,
                        null,
                        tint = PointsTextGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${item.price}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = PointsTextGreen
                    )
                }
            }
        }
    }
}

@Composable
fun RedeemConfirmationDialog(item: StoreItem, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "¿Confirmar canje?",
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Se descontarán ${item.price} puntos por '${item.name}'.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ConfirmGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold, color = LightTextColor)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar", color = Color.Gray) }
        },
        containerColor = Color.White, shape = RoundedCornerShape(28.dp)
    )
}

fun ItemEntity.toStoreItem(isOwned: Boolean): StoreItem {
    return StoreItem(
        id = this.itemId.toInt(),
        name = this.itemName,
        price = this.cost,
        isFeatured = this.isFeatured,


        iconResId = when (this.icon) {
            "shopping_bag" -> R.drawable.ic_totebag
            "premium" -> R.drawable.ic_premium_crown
            "oro" -> R.drawable.ic_gold_deco
            "eco" -> R.drawable.ic_eco_badge
            "tree" -> R.drawable.ic_plant_tree
            else -> R.drawable.ic_totebag
        },

        color = try {
            Color(this.color.toColorInt())
        } catch (e: Exception) {
            Color.Gray
        },
        isOwned = isOwned
    )
}