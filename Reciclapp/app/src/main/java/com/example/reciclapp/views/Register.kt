package com.example.reciclapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.reciclapp.components.CommonUI
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.SignupRequest
import com.example.reciclapp.repository.AuthRepository
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.LightTextColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    var popupController = LocalPopupState.current

    val authRepository = remember { AuthRepository(RetrofitClient.getApi(context)) }

    // Input States
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // UI States
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Validation Logic (Reactive)
    val isPasswordLengthValid = password.length >= 6
    val isPasswordComplex = password.any { it.isDigit() } && password.any { it.isUpperCase() }
    val doPasswordsMatch = password == confirmPassword && password.isNotEmpty()
    val isFormValid = username.isNotEmpty() && isPasswordLengthValid && isPasswordComplex && doPasswordsMatch

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

            CommonUI().ReciclappLogo(Modifier.align(Alignment.Start))

            Spacer(modifier = Modifier.height(72.dp))

            // 2. Title
            Text(
                text = "Registro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Link to Login
            val annotatedString = buildAnnotatedString {
                append("Si ya tenés una cuenta registrada podés ")
                val link = LinkAnnotation.Clickable(
                    tag = "LOGIN_LINK",
                    linkInteractionListener = {
                        navController.navigate("login_screen") {
                            popUpTo("register_screen") { inclusive = true }
                        }
                    }
                )
                withLink(link) {
                    withStyle(SpanStyle(color = DarkerPrimary, fontWeight = FontWeight.Bold)) {
                        append("iniciar sesión acá!")
                    }
                }
            }

            Text(
                text = annotatedString,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Inputs (Styled like Login)

            // Username
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            // Helper text for password requirements
            if (password.isNotEmpty() && (!isPasswordLengthValid || !isPasswordComplex)) {
                Text(
                    text = "Mín. 6 caracteres, 1 mayúscula, 1 número",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmá tu contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password")
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (doPasswordsMatch) Color.Black else Color.Red, // Visual feedback
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 5. Register Button with Retrofit
            Button(
                onClick = {
                    isLoading = true

                    CoroutineScope(Dispatchers.IO).launch {
                        val result = authRepository.signup(SignupRequest(username, password))

                        withContext(Dispatchers.Main) {
                            when (result) {
                                is NetworkResult.Success -> {
                                    popupController.showSuccess("Te registraste con éxito!")
                                    navController.navigate("login_screen") {
                                        popUpTo("register_screen") { inclusive = true }
                                    }
                                }
                                is NetworkResult.Error -> {
                                    // El mensaje ya viene limpio desde BaseApiResponse
                                    popupController.showError(result.message ?: "Error desconocido")
                                }
                            }
                            isLoading = false
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkerPrimary,
                    disabledContainerColor = Color.LightGray,
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = if (!isLoading) "Registrar" else "Registrando..." , fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightTextColor) }
            }

            Spacer(modifier = Modifier.height(24.dp))
    }
}