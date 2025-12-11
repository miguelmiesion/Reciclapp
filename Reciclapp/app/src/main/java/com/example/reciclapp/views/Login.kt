package com.example.reciclapp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@Composable
fun LoginScreen(navController: NavController) {
    // State variables
    var username by remember { mutableStateOf("") } // Using email as per design, but API sends username
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Gray) }

    val apiUrl = "http://192.168.0.142:8000/" // Update if your IP changes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Logo
        Text(
            text = "Reciclapp",
            color = ReciclappGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Title
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Subtitle with Clickable "Register here"
        val annotatedString = buildAnnotatedString {
            append("Si no tenés una cuenta registrada podés ")
            withStyle(style = SpanStyle(color = ReciclappGreen, fontWeight = FontWeight.Bold)) {
                pushStringAnnotation(tag = "REGISTER", annotation = "register")
                append("registrarte acá !")
                pop()
            }
        }

        androidx.compose.foundation.text.ClickableText(
            text = annotatedString,
            style = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Gray),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                    .firstOrNull()?.let {
                        navController.navigate("register_screen")
                    }
            },
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 4. Email Input (Styled with Bottom Border like image)
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("Ingresá tu nombre de usuario") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "User Icon") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black, // Dark line when active
                unfocusedIndicatorColor = Color.Gray  // Gray line when inactive
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Password Input
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            placeholder = { Text("Ingresá tu contraseña") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon") },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle Password")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = ReciclappGreen)
                )
                Text(text = "Recuérdame", fontSize = 14.sp, color = Color.DarkGray)
            }

            Text(
                text = "Olvidaste tu contraseña?",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { /* Handle Forgot Password */ }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 7. Login Button
        Button(
            onClick = {
                statusMessage = "Iniciando sesión..."
                statusColor = Color.Gray

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url: URL = URI.create(apiUrl + "api/login/").toURL()
                        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

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

                        val responseCode = connection.responseCode

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            val jsonResponse = JSONObject(response)

                            // Here you get the tokens:
                            val accessToken = jsonResponse.getString("access")
                            val refreshToken = jsonResponse.getString("refresh")

                            // TODO: Save these tokens in DataStore or SharedPreferences

                            CoroutineScope(Dispatchers.Main).launch {
                                statusMessage = "Login Exitoso!"
                                statusColor = ReciclappGreen
                                navController.navigate("home_screen") {
                                    popUpTo("login_screen") { inclusive = true }
                                }
                            }
                        } else {
                            statusMessage = "Error: Usuario o contraseña incorrectos"
                            statusColor = Color.Red
                        }
                        connection.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        CoroutineScope(Dispatchers.Main).launch {
                            statusMessage = "Error de conexión"
                            statusColor = Color.Red
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReciclappGreen),
            shape = RoundedCornerShape(25.dp) // More rounded as per image
        ) {
            Text(text = "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. "Or continue with" Section
        Text(
            text = "o continúa con",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Google Button Placeholder
        // If you have the asset: painter = painterResource(id = R.drawable.google_logo)
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clickable { /* Handle Google Login */ },
            shape = CircleShape,
            shadowElevation = 4.dp,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "G", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
            }
        }

        // Status Message for Errors
        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusMessage,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}