package com.example.reciclapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.* // Esto incluye collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.viewmodels.RankingViewModel
import com.example.reciclapp.viewmodels.RankingViewModelFactory
import com.example.reciclapp.repository.RankingRepository

val CardDarkBackground = Color(0xFF424242)
val MyPositionGreen = Color(0xFFA5D6A7)
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFC0C0C0)
val Bronze = Color(0xFFCD7F32)

@Composable
fun RankingScreen(navController: NavController) {
    val context = LocalContext.current

    val api = RetrofitClient.getApi(context)
    val repository = remember { RankingRepository(api) }

    val viewModel: RankingViewModel = viewModel(
        factory = RankingViewModelFactory(repository)
    )

    // --- CORRECCIÓN AQUÍ ---
    // Antes: val state = viewModel.uiState
    // Ahora: Usamos 'by' y 'collectAsState()' para escuchar los cambios del Flow
    val state by viewModel.uiState.collectAsState()
    // -----------------------

    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("Todos", "Vidrio", "Carton", "Metal", "Papel")

    Scaffold(
        bottomBar = { ReciclappBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Ranking", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sumá puntos y superá a otros usuarios en rankings!",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, color = DarkerPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            // Título dinámico
            Text(
                text = if (state.currentFilter == null) "Usuarios top" else "Top - ${state.currentFilter}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // LISTA TOP
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDarkBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (state.isLoading && state.topUsers.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (state.topUsers.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay datos para mostrar", color = Color.White)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(state.topUsers) { index, user ->
                                RankingItem(
                                    rank = index + 1,
                                    username = user.username,
                                    points = user.totalPoints,
                                    isCurrentUser = user.username.equals(state.currentUserName, ignoreCase = true)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TU POSICIÓN
            state.userPosition?.let { pos ->
                Text("Tu posición", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkBackground)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        RankingItem(
                            rank = pos,
                            username = state.currentUserName,
                            points = null,
                            isCurrentUser = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN FILTRO
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { isFilterMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkerPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.currentFilter ?: "Filtrar por residuo",
                        color = Color.White
                    )
                }

                DropdownMenu(
                    expanded = isFilterMenuExpanded,
                    onDismissRequest = { isFilterMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                isFilterMenuExpanded = false
                                viewModel.updateFilter(if (option == "Todos") null else option)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RankingItem(rank: Int, username: String, points: Int?, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentUser) MyPositionGreen else Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(32.dp)) {
            when (rank) {
                1 -> Icon(Icons.Default.EmojiEvents, null, tint = Gold)
                2 -> Icon(Icons.Default.EmojiEvents, null, tint = Silver)
                3 -> Icon(Icons.Default.EmojiEvents, null, tint = Bronze)
                else -> Text(
                    text = if (rank == 0) "--" else "$rank",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = if (isCurrentUser) Color.Black else Color.Gray,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isCurrentUser) Color.White.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f))
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = username,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = points?.toString() ?: "",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}