package com.example.reciclapp.views

import android.app.AlertDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import com.example.reciclapp.components.CommonUI
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.LightTextColor
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.LoginRequest
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // State variables
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isFormValid = username.isNotBlank() && password.isNotBlank()

    var popupController = LocalPopupState.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            CommonUI().ReciclappLogo(
                Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(72.dp))

            // 2. Title
            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val annotatedString = buildAnnotatedString {
                append("Si no tenés una cuenta registrada podés ")

                val link = LinkAnnotation.Clickable(
                    tag = "REGISTER_LINK",
                    linkInteractionListener = {
                        navController.navigate("register_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                )
                withLink(link) {
                    withStyle(SpanStyle(color = DarkerPrimary, fontWeight = FontWeight.Bold)) {
                        append("registrarte acá !")
                    }
                }
            }

            Text(
                text = annotatedString,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                placeholder = { Text("Ingresá tu nombre de usuario") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "User Icon") },
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

            Spacer(modifier = Modifier.height(20.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Olvidaste tu contraseña?",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        AlertDialog.Builder(context)
                            .setMessage("TO-DO!")
                            .setTitle("Funcionalidad no desarrollada :(!")
                            .create()
                            .show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    isLoading = true

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val api = RetrofitClient.getApi(context)
                            val request = LoginRequest(username, password)
                            val response = api.login(request)

                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful && response.body() != null) {
                                    // Respuesta exitosa

                                    val tokens = response.body()!!

                                    tokenManager.saveTokens(tokens.access, tokens.refresh)
                                    navController.navigate("home_screen") {
                                        popUpTo("login_screen") { inclusive = true }
                                    }
                                } else {
                                    // Error
                                    if (response.code() == 401) popupController.showError("Credenciales incorrectas") else popupController.showError("Error del servidor")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            CoroutineScope(Dispatchers.Main).launch {
                                popupController.showError("Error de conexión")
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFormValid) DarkerPrimary else Color.Gray),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = if (!isLoading) "Login" else "Iniciando sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightTextColor)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}