package com.example.reciclapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

val ReciclappGreen = Color(0xFF2E7D32)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 1. NUEVO: Variables para controlar el mensaje de estado y su color
    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Gray) }

    val apiUrl = "http://192.168.0.142:8000/"

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

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Ingresá tu email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
                // Limpiamos el mensaje anterior al hacer click
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
                        // Tip: Deberías enviar el email también si tu backend lo espera
                        // jsonObject.put("email", email)

                        connection.outputStream.use { os ->
                            val input = jsonObject.toString().toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                        }

                        val responseCode: Int = connection.responseCode

                        // 2. NUEVO: Actualizamos el estado según la respuesta
                        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                            statusMessage = "Usuario registrado con éxito"
                            statusColor = ReciclappGreen // Verde
                        } else {
                            // Leemos el error del servidor
                            val errorStream = connection.errorStream
                            val errorMsg = errorStream?.bufferedReader()?.use { it.readText() }
                                ?: "Error desconocido del servidor ($responseCode)"

                            statusMessage = errorMsg // Muestra lo que devolvió la API
                            statusColor = Color.Red // Rojo
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

        // 3. NUEVO: Componente para mostrar el mensaje de resultado
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

@Preview(showBackground = true)
@Composable
fun PreviewRegistro() {
    RegisterScreen()
}