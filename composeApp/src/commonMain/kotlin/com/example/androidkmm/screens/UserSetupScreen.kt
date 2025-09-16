package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserSetupScreen(
    onSetupComplete: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val settingsDatabase = rememberSQLiteSettingsDatabase()
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome Text
            Text(
                text = "Welcome to MoneyMate",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Let's personalize your experience",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Name Input
            OutlinedTextField(
                value = userName,
                onValueChange = { 
                    userName = it
                    showError = false
                },
                label = { Text("Your Name", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Input
            OutlinedTextField(
                value = userEmail,
                onValueChange = { 
                    userEmail = it
                    showError = false
                },
                label = { Text("Email (Optional)", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email explanation
            Text(
                text = "Email is used for backup purposes and important updates",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error message
            if (showError) {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Continue Button
            Button(
                onClick = {
                    if (userName.isBlank()) {
                        showError = true
                        errorMessage = "Please enter your name"
                        return@Button
                    }
                    
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            println("Debug: Starting setup process...")
                            val trimmedName = userName.trim()
                            val trimmedEmail = userEmail.trim()
                            
                            println("Debug: Name: '$trimmedName', Email: '$trimmedEmail'")
                            
                            // Save user name to settings
                            settingsDatabase.updateUserName(trimmedName)
                            println("Debug: Saved user name to database")
                            
                            // Save email if provided, otherwise auto-generate
                            val finalEmail = if (trimmedEmail.isNotBlank()) {
                                trimmedEmail
                            } else {
                                // Auto-generate email from name: "firstname"@moneymate.com
                                val firstName = trimmedName.split(" ").firstOrNull()?.lowercase() ?: "user"
                                "${firstName}@moneymate.com"
                            }
                            
                            println("Debug: Final email: '$finalEmail'")
                            
                            // Save email
                            settingsDatabase.updateSetting("user_email", finalEmail)
                            println("Debug: Saved email to database")
                            
                            // Add a small delay to ensure database operations complete
                            delay(100)
                            
                            println("Debug: Calling onSetupComplete()")
                            onSetupComplete()
                            println("Debug: Setup complete callback called")
                            
                            // Reset loading state
                            isLoading = false
                            
                        } catch (e: Exception) {
                            println("Debug: Error during setup: ${e.message}")
                            e.printStackTrace()
                            showError = true
                            errorMessage = "Failed to save your information. Please try again."
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms text
            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Debug buttons (temporary for testing)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                // Reset user setup for testing
                                settingsDatabase.updateSetting("user_name", "")
                                settingsDatabase.updateSetting("user_email", "")
                                println("Debug: Cleared user setup data")
                            } catch (e: Exception) {
                                println("Debug: Failed to reset user setup: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Clear User Data (Debug)",
                        fontSize = 14.sp
                    )
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                // Force complete setup for testing
                                settingsDatabase.updateUserName("TestUser")
                                settingsDatabase.updateSetting("user_email", "test@moneymate.com")
                                println("Debug: Set test user data")
                            } catch (e: Exception) {
                                println("Debug: Failed to set test data: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Set Test Data (Debug)",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
