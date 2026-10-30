package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthManager
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioDarkText
import com.example.ui.theme.StudioGreenDark
import com.example.ui.theme.StudioGreenLight
import com.example.ui.theme.StudioRedDark
import com.example.ui.theme.StudioRedLight
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioSecondaryGray
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SignInScreen(
    authManager: AuthManager,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // 0 = Google Login, 1 = Email Login, 2 = New Account (Sign Up)
    var authMode by remember { mutableStateOf(0) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StudioBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Studio Brand Logo with Camera Icon & Gradient
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(StudioRedPrimary, StudioRedDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Studio Camera",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Studio Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StudioDarkText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "સ્ટુડિયો મેનેજમેન્ટ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = StudioRedPrimary,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ગ્રાહક, ઇવેન્ટ અને કેમેરા ક્રૂ ડેટા સુરક્ષિત સાચવવા લૉગિન કરો.",
                fontSize = 13.sp,
                color = StudioSecondaryGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Auth Mode Selector: Google vs Email Login vs Register
            TabRow(
                selectedTabIndex = authMode,
                containerColor = Color.White,
                contentColor = StudioRedPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[authMode]),
                        color = StudioRedPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = authMode == 0,
                    onClick = {
                        authMode = 0
                        errorMessage = null
                        successMessage = null
                    },
                    text = {
                        Text(
                            text = "Google લૉગિન",
                            fontWeight = if (authMode == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = authMode == 1,
                    onClick = {
                        authMode = 1
                        errorMessage = null
                        successMessage = null
                    },
                    text = {
                        Text(
                            text = "ઈમેલ લૉગિન",
                            fontWeight = if (authMode == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = authMode == 2,
                    onClick = {
                        authMode = 2
                        errorMessage = null
                        successMessage = null
                    },
                    text = {
                        Text(
                            text = "નવું ખાતું",
                            fontWeight = if (authMode == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Start,
                        lineHeight = 18.sp
                    )
                }
            }

            // Success Message Banner
            AnimatedVisibility(visible = successMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioGreenLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = successMessage ?: "",
                        color = StudioGreenDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (authMode == 0) {
                // Tab 0: Google Sign-In Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "સિંગલ-ટેપથી Google એકાઉન્ટ વડે લૉગિન કરો",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioDarkText,
                            textAlign = TextAlign.Center
                        )

                        // Google Sign-In Button
                        Button(
                            onClick = {
                                val activity = context.findActivity()
                                if (activity == null) {
                                    errorMessage = "લૉગિન શરૂ કરી શકાયું નથી. કૃપા કરીને ફરી પ્રયાસ કરો."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                coroutineScope.launch {
                                    val result = authManager.signInWithGoogle(activity)
                                    isLoading = false
                                    result.onSuccess {
                                        successMessage = "સફળતાપૂર્વક લૉગિન થયું!"
                                        onSignInSuccess()
                                    }.onFailure { err ->
                                        errorMessage = err.localizedMessage ?: "Google લૉગિન નિષ્ફળ ગયું."
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = StudioDarkText
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(12.dp))
                                .testTag("google_sign_in_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = StudioRedPrimary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("લૉગિન થઈ રહ્યું છે...", fontSize = 14.sp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4285F4)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Google દ્વારા સાઇન ઇન કરો",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = StudioDarkText
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1 & 2: Email & Password Form (Login or Create Account)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (authMode == 1) "તમારા ઈમેલ અને પાસવર્ડથી લૉગિન કરો" else "નવું ખાતું બનાવો (Create Account)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkText
                        )

                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("ઈમેલ સરનામું (Email)") },
                            placeholder = { Text("તમારો ઈમેલ દાખલ કરો") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = StudioRedPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioRedPrimary,
                                focusedLabelColor = StudioRedPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input_field")
                        )

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("પાસવર્ડ (Password)") },
                            placeholder = { Text("ઓછામાં ઓછા 6 અક્ષરો") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = StudioRedPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = StudioSecondaryGray
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (authMode == 1) ImeAction.Done else ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioRedPrimary,
                                focusedLabelColor = StudioRedPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input_field")
                        )

                        // Confirm Password (Sign-Up only)
                        if (authMode == 2) {
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    errorMessage = null
                                },
                                label = { Text("પાસવર્ડ ફરી દાખલ કરો") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = StudioRedPrimary)
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioRedPrimary,
                                    focusedLabelColor = StudioRedPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_password_input_field")
                            )
                        }

                        // Forgot Password Link (for Login tab)
                        if (authMode == 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    resetEmailInput = emailInput.trim()
                                    showForgotPasswordDialog = true
                                }) {
                                    Text(
                                        text = "પાસવર્ડ ભૂલી ગયા છો?",
                                        fontSize = 12.sp,
                                        color = StudioRedPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                val cleanEmail = emailInput.trim()
                                val cleanPass = passwordInput.trim()

                                if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
                                    errorMessage = "કૃપા કરીને સાચું ઈમેલ સરનામું દાખલ કરો."
                                    return@Button
                                }
                                if (cleanPass.length < 6) {
                                    errorMessage = "પાસવર્ડ ઓછામાં ઓછો 6 અક્ષરોનો હોવો જોઈએ."
                                    return@Button
                                }
                                if (authMode == 2 && cleanPass != confirmPasswordInput.trim()) {
                                    errorMessage = "બંને પાસવર્ડ મેળ ખાતા નથી."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                coroutineScope.launch {
                                    if (authMode == 1) {
                                        // Email Login
                                        val result = authManager.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                        isLoading = false
                                        result.onSuccess {
                                            successMessage = "સફળતાપૂર્વક લૉગિન થયું!"
                                            onSignInSuccess()
                                        }.onFailure { err ->
                                            errorMessage = err.localizedMessage ?: "લૉગિન નિષ્ફળ ગયું."
                                        }
                                    } else {
                                        // Email Sign-Up
                                        val result = authManager.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                                        isLoading = false
                                        result.onSuccess {
                                            successMessage = "એકાઉન્ટ સફળતાપૂર્વક બન્યું! લૉગિન થઈ રહ્યું છે..."
                                            onSignInSuccess()
                                        }.onFailure { err ->
                                            errorMessage = err.localizedMessage ?: "એકાઉન્ટ બની શક્યું નહીં."
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioRedPrimary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("email_auth_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("પ્રક્રિયા થઈ રહી છે...")
                            } else {
                                Text(
                                    text = if (authMode == 1) "લૉગિન કરો (Sign In)" else "ખાતું બનાવો (Create Account)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zero-trust data privacy badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = StudioGreenDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "તમારો તમામ ડેટા ખાનગી છે, અન્ય કોઈ વપરાશકર્તા જોઈ શકશે નહીં.",
                    fontSize = 11.sp,
                    color = StudioSecondaryGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var dialogError by remember { mutableStateOf<String?>(null) }
        var isSendingReset by remember { mutableStateOf(false) }
        var dialogSuccess by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                if (!isSendingReset) showForgotPasswordDialog = false
            },
            title = {
                Text(
                    text = "પાસવર્ડ રીસેટ કરો",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "તમારું ઈમેલ સરનામું દાખલ કરો. અમે તમને પાસવર્ડ રીસેટ કરવાની લિંક મોકલીશું.",
                        fontSize = 13.sp,
                        color = StudioSecondaryGray
                    )
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = {
                            resetEmailInput = it
                            dialogError = null
                        },
                        label = { Text("ઈમેલ સરનામું") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogError != null) {
                        Text(
                            text = dialogError ?: "",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp
                        )
                    }
                    if (dialogSuccess != null) {
                        Text(
                            text = dialogSuccess ?: "",
                            color = StudioGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = resetEmailInput.trim()
                        if (clean.isEmpty() || !clean.contains("@")) {
                            dialogError = "કૃપા કરીને માન્ય ઈમેલ દાખલ કરો."
                            return@Button
                        }
                        isSendingReset = true
                        dialogError = null
                        coroutineScope.launch {
                            val res = authManager.sendPasswordResetEmail(clean)
                            isSendingReset = false
                            res.onSuccess {
                                dialogSuccess = "રીસેટ લિંક ઈમેલ પર મોકલી દેવામાં આવી છે."
                            }.onFailure { err ->
                                dialogError = err.localizedMessage ?: "લિંક મોકલી શકાઈ નથી."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary)
                ) {
                    Text("લિંક મોકલો")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("બંધ કરો")
                }
            }
        )
    }
}
