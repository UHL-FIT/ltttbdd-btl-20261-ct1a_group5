package com.example.pokedex.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    val backgroundColor = Color(0xFFE3352F)

    var isRegisterMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRegisterMode) "Create account" else "Sign in",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                label = {
                    Text("Email")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = loginTextFieldColors()
            )

            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("First name & surname")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = loginTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = {
                    Text(if (isRegisterMode) "New password" else "Password")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showPassword = !showPassword
                        }
                    ) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = "Toggle password visibility",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                colors = loginTextFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()
                    val cleanName = name.trim()

                    if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please enter email and password",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (isRegisterMode && cleanName.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please enter your name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    isLoading = true

                    if (isRegisterMode) {
                        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = cleanName
                                    }

                                    auth.currentUser?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener {
                                            isLoading = false

                                            Toast.makeText(
                                                context,
                                                "Account created",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            onLoginSuccess()
                                        }
                                } else {
                                    isLoading = false

                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Register failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                            .addOnCompleteListener { task ->
                                isLoading = false

                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Signed in",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Login failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = backgroundColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = backgroundColor
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "CREATE ACCOUNT" else "SIGN IN",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                }
            ) {
                Text(
                    text = if (isRegisterMode) {
                        "Already have an account? Sign in"
                    } else {
                        "New user? Create account"
                    },
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun loginTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,

        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.65f),

        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.45f),

        cursorColor = Color.White,

        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )
}