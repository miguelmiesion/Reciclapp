package com.example.reciclapp.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.network.RankingEntry
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.ui.theme.DarkerPrimary
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// --- VIEWMODEL & ESTADO (Lógica) ---
data class RankingUiState(
    val topUsers: List<RankingEntry> = emptyList(),
    val userPosition: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankingViewModel(
    private val api: ReciclappApi,
    private val currentUserId: Int
) : ViewModel() {

    var uiState by mutableStateOf(RankingUiState())
        private set

    init {
        loadRanking()
    }

    private fun loadRanking() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                // Carga paralela de Top 10 y Posición
                val topDeferred = async { api.getTopRanking() }
                val posDeferred = async { api.getUserPosition(userId = currentUserId) }

                val topResponse = topDeferred.await()
                val posResponse = posDeferred.await()

                if (topResponse.isSuccessful && posResponse.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading = false,

                        // --- ¡ESTA ES LA LÍNEA QUE FALTABA! ---
                        topUsers = topResponse.body() ?: emptyList(),
                        // --------------------------------------

                        userPosition = posResponse.body()?.posicion
                    )
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Error al cargar datos")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }
}

class RankingViewModelFactory(private val api: ReciclappApi, private val userId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RankingViewModel(api, userId) as T
    }
}

// --- UI (Diseño Visual) ---

val CardDarkBackground = Color(0xFF424242)
val MyPositionGreen = Color(0xFFA5D6A7)
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFC0C0C0)
val Bronze = Color(0xFFCD7F32)

@Composable
fun RankingScreen(navController: NavController) {
    val context = LocalContext.current

    // TODO: Usar el ID real del usuario desde TokenManager
    val userId = 1

    val viewModel: RankingViewModel = viewModel(
        factory = RankingViewModelFactory(RetrofitClient.getApi(context), userId)
    )
    val state = viewModel.uiState

    // AQUÍ AGREGAMOS LA BARRA INFERIOR
    Scaffold(
        bottomBar = {
            // Llamamos a la barra que definiste en QrScan.kt
            ReciclappBottomBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header
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

            // Lista Top Usuarios
            Text("Usuarios top", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDarkBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp)) { // Agregamos Column para ordenar

                    if (state.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (state.topUsers.isEmpty()) {
                        // Mensaje si la lista está vacía
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay datos para mostrar", color = Color.White)
                        }
                    } else {
                        // La lista real
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(state.topUsers) { index, user ->
                                RankingItem(
                                    rank = index + 1,
                                    username = user.username,
                                    points = user.totalPoints,
                                    isCurrentUser = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tu Posición (Fija abajo)
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
                            username = "Vos",
                            points = null,
                            isCurrentUser = true
                        )
                    }
                }
            }
            // Espacio extra al final
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
        // 1. Icono de Rango
        Box(modifier = Modifier.width(32.dp)) {
            when (rank) {
                1 -> Icon(Icons.Default.EmojiEvents, null, tint = Gold)
                2 -> Icon(Icons.Default.EmojiEvents, null, tint = Silver)
                3 -> Icon(Icons.Default.EmojiEvents, null, tint = Bronze)
                else -> Text("$rank", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 2. Avatar
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

        // 3. Nombre
        Text(
            text = username,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Text(text = "|", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))

        // 4. Puntos
        Text(
            text = points?.toString() ?: "- -",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}