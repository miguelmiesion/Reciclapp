package com.example.reciclapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reciclapp.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


val ReciclappGreen = Color(0xFF2E7D32)

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Variables para controlar el mensaje de estado y su color
    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Gray) }

    val apiUrl = BuildConfig.API_URL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Reciclapp",
            color = ReciclappGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Registro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Si ya tenés una cuenta registrada podés iniciar sesión acá!",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Ingresá tu nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Ingresá tu contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmá tu contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (password.length < 6 || !password.any { it.isDigit() } || !password.any { it.isUpperCase() } || !password.any { it.isLowerCase() }) {
                    statusMessage = "La contraseña debe tener mín. 6 caracteres, 1 número, 1 mayúscula y 1 minúscula."
                    statusColor = Color.Red
                    return@Button
                }
                else if(password != confirmPassword){
                    statusMessage = "Las contraseñas no coinciden"
                    statusColor = Color.Red
                    return@Button
                }

                statusMessage = "Cargando..."
                statusColor = Color.Gray

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url: URL = URI.create(apiUrl + "api/signup/").toURL()
                        val connection: HttpURLConnection =
                            url.openConnection() as HttpURLConnection

                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "application/json; utf-8")
                        connection.setRequestProperty("Accept", "application/json")
                        connection.doOutput = true

                        val jsonObject = JSONObject()
                        jsonObject.put("username", username)
                        jsonObject.put("password", password)
                        connection.outputStream.use { os ->
                            val input = jsonObject.toString().toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                        }

                        val responseCode: Int = connection.responseCode

                        // Actualizamos el estado según la respuesta
                        if (responseCode == HttpURLConnection.HTTP_CREATED) {

                            // CAMBIO IMPORTANTE: Navegamos en el hilo principal
                            CoroutineScope(Dispatchers.Main).launch {
                                statusMessage = "Usuario registrado con éxito"
                                statusColor = ReciclappGreen

                                // Navegar a la pantalla de Home
                                navController.navigate("home_screen") {
                                    popUpTo("register_screen") { inclusive = true }
                                }
                            }

                        }
                        else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST){
                            val errorMsg = "Ya existe un usuario con ese nombre."
                            statusMessage = errorMsg
                            statusColor = Color.Red
                        }
                        else {
                            val errorStream = connection.errorStream
                            val errorMsg = errorStream?.bufferedReader()?.use { it.readText() }
                                ?: "Error desconocido del servidor ($responseCode)"

                            statusMessage = errorMsg
                            statusColor = Color.Red
                        }

                        connection.disconnect()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        statusMessage = "Fallo la conexión: ${e.localizedMessage}"
                        statusColor = Color.Red
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReciclappGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Register", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}