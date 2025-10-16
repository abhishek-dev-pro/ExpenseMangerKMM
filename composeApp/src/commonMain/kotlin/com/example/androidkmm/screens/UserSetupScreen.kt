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
import com.example.androidkmm.database.InitializeDatabase
import com.example.androidkmm.utils.FormValidation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserSetupScreen(
    settingsDatabase: com.example.androidkmm.database.SQLiteSettingsDatabase,
    onSetupComplete: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var nameValidationError by remember { mutableStateOf("") }
    var emailValidationError by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize database
    InitializeDatabase()
    
    // Debug database state on startup
    LaunchedEffect(Unit) {
        settingsDatabase.debugDatabaseState()
    }
    
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
                    // Limit to 22 characters
                    if (it.length <= 22) {
                        userName = it
                        showError = false
                        // Validate name in real-time
                        val validation = FormValidation.validatePersonName(it)
                        nameValidationError = if (validation.isValid) "" else validation.errors["name"] ?: ""
                    }
                },
                label = { Text("Your Name", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                isError = nameValidationError.isNotEmpty(),
                supportingText = if (nameValidationError.isNotEmpty()) {
                    { Text(nameValidationError, color = Color.Red, fontSize = 12.sp) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (nameValidationError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (nameValidationError.isNotEmpty()) Color.Red else Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = if (nameValidationError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Input
            OutlinedTextField(
                value = userEmail,
                onValueChange = { 
                    // Limit to 50 characters
                    if (it.length <= 50) {
                        userEmail = it
                        showError = false
                        // Validate email in real-time
                        val validation = FormValidation.validateEmail(it)
                        emailValidationError = if (validation.isValid) "" else validation.errors["email"] ?: ""
                    }
                },
                label = { Text("Email (Optional)", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                isError = emailValidationError.isNotEmpty(),
                supportingText = if (emailValidationError.isNotEmpty()) {
                    { Text(emailValidationError, color = Color.Red, fontSize = 12.sp) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailValidationError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (emailValidationError.isNotEmpty()) Color.Red else Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = if (emailValidationError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.primary,
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
                    println("DEBUG: Get Started button clicked!")
                    println("DEBUG: userName = '$userName'")
                    
                    // Validate name using the new validation function
                    val nameValidation = FormValidation.validatePersonName(userName)
                    if (!nameValidation.isValid) {
                        println("DEBUG: Name validation failed: ${nameValidation.errors}")
                        showError = true
                        errorMessage = nameValidation.errors["name"] ?: "Please enter a valid name"
                        return@Button
                    }
                    
                    // Validate email if provided
                    if (userEmail.isNotBlank()) {
                        val emailValidation = FormValidation.validateEmail(userEmail)
                        if (!emailValidation.isValid) {
                            println("DEBUG: Email validation failed: ${emailValidation.errors}")
                            showError = true
                            errorMessage = emailValidation.errors["email"] ?: "Please enter a valid email"
                            return@Button
                        }
                    }
                    
                    println("DEBUG: Starting setup process...")
                    isLoading = true
                    showError = false
                    
                    coroutineScope.launch {
                        try {
                            println("DEBUG: Starting coroutine scope")
                            val trimmedName = userName.trim()
                            val trimmedEmail = userEmail.trim()
                            
                            println("DEBUG: Trimmed name: '$trimmedName', trimmed email: '$trimmedEmail'")
                            
                            // Check if name is still valid after trimming
                            if (trimmedName.isBlank()) {
                                println("DEBUG: Name is blank after trimming")
                                showError = true
                                errorMessage = "Please enter your name"
                                isLoading = false
                                return@launch
                            }
                            
                            // Format name: first letter capitalized, rest lowercase
                            val formattedName = FormValidation.capitalizeName(trimmedName)
                            
                            println("DEBUG: Saving formatted name: '$formattedName'")
                            // Save user name to settings
                            settingsDatabase.updateUserName(formattedName)
                            println("DEBUG: Name saved successfully")
                            
                            // Save email if provided, otherwise auto-generate
                            val finalEmail = if (trimmedEmail.isNotBlank()) {
                                // Normalize email (trim and convert to lowercase)
                                FormValidation.normalizeEmail(trimmedEmail)
                            } else {
                                // Auto-generate email from name: "firstname"@moneymate.com
                                val firstName = trimmedName.split(" ").firstOrNull()?.lowercase() ?: "user"
                                "${firstName}@moneymate.com"
                            }
                            
                            println("DEBUG: Final email: '$finalEmail'")
                            println("DEBUG: Saving email: '$finalEmail'")
                            // Save email
                            settingsDatabase.updateSetting("user_email", finalEmail)
                            println("DEBUG: Email saved successfully")
                            
                            // Add a small delay to ensure database operations complete
                            println("DEBUG: Waiting for database operations to complete...")
                            delay(1000)
                            
                            // Verify the data was saved
                            println("DEBUG: Verifying saved data...")
                            settingsDatabase.debugDatabaseState()
                            
                            println("DEBUG: Calling onSetupComplete()")
                            // Call the completion callback
                            onSetupComplete()
                            println("DEBUG: onSetupComplete() called")
                            
                        } catch (e: Exception) {
                            println("DEBUG: Error during setup: ${e.message}")
                            println("DEBUG: Error type: ${e::class.simpleName}")
                            e.printStackTrace()
                            showError = true
                            errorMessage = "Failed to save your information: ${e.message}"
                        } finally {
                            isLoading = false
                            println("DEBUG: Setup process completed")
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
            
        }
    }
}
