package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSecondary
import com.example.ui.viewmodel.PreBiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: PreBiteViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf(0) } // 0: Login, 1: Register, 2: Forgot Password

    // Login state
    var loginEmail by remember { mutableStateOf("alex@techcorp.com") }
    var loginPassword by remember { mutableStateOf("user123") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register state
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("CUSTOMER") }
    var regCompany by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }

    // Forgot password state
    var forgotEmail by remember { mutableStateOf("") }
    var forgotOtp by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("8492") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("auth_screen")
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFBF360C), Color(0xFFE65100))
                    )
                )
                .padding(top = 40.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "PreBite Logo",
                            tint = CoralPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PreBite Accounts",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Scheduled Food Delivery & Operations Portal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }
        }

        // Mode Navigation Tabs
        TabRow(
            selectedTabIndex = authMode,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary
        ) {
            Tab(
                selected = authMode == 0,
                onClick = {
                    authMode = 0
                    errorMessage = null
                },
                text = { Text("Sign In") }
            )
            Tab(
                selected = authMode == 1,
                onClick = {
                    authMode = 1
                    errorMessage = null
                },
                text = { Text("Register") }
            )
            Tab(
                selected = authMode == 2,
                onClick = {
                    authMode = 2
                    errorMessage = null
                },
                text = { Text("Reset Password") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            if (successMessage != null) {
                item {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }

            // Quick Demo Auto-Fill Presets for Evaluation
            if (authMode == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralSecondary.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚡ Quick Demo Login (One-Tap)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CoralSecondary)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        loginEmail = "alex@techcorp.com"
                                        loginPassword = "user123"
                                        viewModel.login("alex@techcorp.com", "user123") { success, msg ->
                                            if (success) onAuthSuccess() else errorMessage = msg
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text("Customer Login", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        loginEmail = "admin@prebite.com"
                                        loginPassword = "admin123"
                                        viewModel.login("admin@prebite.com", "admin123") { success, msg ->
                                            if (success) onAuthSuccess() else errorMessage = msg
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text("Admin Portal", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Sign In Form
                item {
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                authMode = 2
                                forgotEmail = loginEmail
                            }
                        ) {
                            Text("Forgot Password?", color = CoralPrimary)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                errorMessage = "Please enter email and password."
                            } else {
                                viewModel.login(loginEmail, loginPassword) { success, msg ->
                                    if (success) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_login_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text("Sign In to Account", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (authMode == 1) {
                // Register Form
                item {
                    Text(
                        text = "Create Your Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Join PreBite to schedule meals with zero waste and free batched delivery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = regRole == "CUSTOMER",
                            onClick = { regRole = "CUSTOMER" },
                            label = { Text("Customer (Order Meals)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = regRole == "ADMIN",
                            onClick = { regRole = "ADMIN" },
                            label = { Text("Kitchen Admin") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regCompany,
                        onValueChange = { regCompany = it },
                        label = { Text("Office / Company Name") },
                        placeholder = { Text("e.g. TechCorp Tower B") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regAddress,
                        onValueChange = { regAddress = it },
                        label = { Text("Primary Delivery Address & Desk") },
                        placeholder = { Text("e.g. 100 Innovation Way, 4th Floor") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                errorMessage = "Please fill in all required fields."
                            } else {
                                viewModel.register(
                                    fullName = regName,
                                    email = regEmail,
                                    password = regPassword,
                                    phone = regPhone,
                                    role = regRole,
                                    company = regCompany,
                                    initialAddress = regAddress
                                ) { success, msg ->
                                    if (success) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_register_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text("Create Account", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Forgot Password Flow
                item {
                    Text(
                        text = "Reset Your Password",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Enter your registered email address to receive a secure recovery code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Registered Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                if (!otpSent) {
                    item {
                        Button(
                            onClick = {
                                if (forgotEmail.isBlank()) {
                                    errorMessage = "Please enter your email."
                                } else {
                                    otpSent = true
                                    generatedOtp = "${(1000..9999).random()}"
                                    successMessage = "Verification OTP code sent: $generatedOtp (Simulated SMS/Email)"
                                    Toast.makeText(context, "Verification code is $generatedOtp", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_otp_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                        ) {
                            Text("Send Recovery Code")
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = forgotOtp,
                            onValueChange = { forgotOtp = it },
                            label = { Text("Enter 4-Digit OTP Code") },
                            placeholder = { Text("Code: $generatedOtp") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("Set New Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("new_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (forgotOtp != generatedOtp) {
                                    errorMessage = "Invalid verification code. Please enter $generatedOtp."
                                } else if (forgotNewPassword.isBlank()) {
                                    errorMessage = "Please enter a new password."
                                } else {
                                    viewModel.forgotPassword(forgotEmail, forgotNewPassword) { success, msg ->
                                        if (success) {
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reset_password_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                        ) {
                            Text("Reset Password & Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
