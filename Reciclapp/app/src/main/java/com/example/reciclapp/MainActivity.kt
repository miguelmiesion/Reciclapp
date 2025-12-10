package com.example.reciclapp // Asegúrate que esto coincida con tu paquete real

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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

// Definimos el color verde de tu imagen (aprox)
val ReciclappGreen = Color(0xFF2E7D32)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aquí llamamos a nuestra pantalla
            MaterialTheme {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {
    // Estas variables guardan lo que el usuario escribe (El "Estado")
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val apiUrl = "http://192.168.0.142:8000/"

    // Column organiza los elementos uno debajo del otro
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .padding(24.dp) // Margen general
            .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. Logo / Título superior
        Text(
            text = "Reciclapp",
            color = ReciclappGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp)) // Espacio vacío

        // 2. Título Principal
        Text(
            text = "Registro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Subtítulo
        Text(
            text = "Si ya tenés una cuenta registrada podés iniciar sesión acá!",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 4. Campos de Texto (Inputs)

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Ingresá tu email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
            // Aquí podrías agregar el icono (leadingIcon)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Usuario
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Ingresá tu nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Ingresá tu contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(), // Esto pone los puntitos ****
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar Contraseña
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

        // 5. Botón
        Button(
            onClick = {
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
                        println("Respuesta del servidor: $responseCode")

                        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                            // Éxito: Aquí podrías navegar a otra pantalla
                            println("¡Usuario registrado!")
                        } else {
                            // Error: Leer el mensaje de error del servidor
                            val errorMsg = connection.errorStream.bufferedReader().use { it.readText() }
                            println("Error al registrar: $errorMsg")
                        }

                        connection.disconnect()


                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Fallo la conexion: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReciclappGreen),
            shape = RoundedCornerShape(12.dp) // Bordes redondeados
        ) {
            Text(text = "Register", fontSize = 18.sp)
        }
    }
}

// Esto permite ver el diseño sin ejecutar la app en el emulador
@Preview(showBackground = true)
@Composable
fun PreviewRegistro() {
    RegisterScreen()
}