package com.homeshop.seebazar.ui // Update with your actual package

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

private val AuthFormMaxWidth = 420.dp
private const val AuthTestLogTag = "SeebazarAuth"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeebazarLoginScreen(
    onBackClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    /**
     * Invoked when Login is tapped.
     * @param isServiceTab `true` when the **Service** tab is selected (vendor flow), `false` for **User** (customer home).
     */
    onLoginSuccess: (isServiceTab: Boolean) -> Unit = { _ -> },
) {
    val primaryTextColor = Color(0xFF1E232C)
    val textFieldBackground = Color(0xFFF7F8F9)
    val textFieldBorder = Color(0xFFE8ECF4)
    val grayText = Color(0xFF6A707C)

    // State for inputs, password visibility, and Tab selection
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("User", "Service")

    val canSubmit = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = AuthFormMaxWidth)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome back! Glad\nto see you, Again!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AuthColors.AccentBlue,
            lineHeight = 39.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- MINIMAL SQUARE TAB LAYOUT ---
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = textFieldBackground,
            contentColor = AuthColors.AccentBlue,
            // Minimal square edges (2dp is virtually square but looks cleaner)
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(textFieldBackground, RoundedCornerShape(4.dp))
                .padding(2.dp),
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxHeight()
                        .background(Color.White, RoundedCornerShape(2.dp)) // The "Square" active background
                        .border(1.dp, textFieldBorder, RoundedCornerShape(2.dp))
                )
            },
            divider = {} // Remove the default bottom line
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) AuthColors.AccentBlue else AuthColors.AccentBlueLight
                            )
                        )
                    },
                    modifier = Modifier.zIndex(1f) // Ensures text is above indicator
                )
            }
        }

        // --- INPUT FIELDS ---
        Spacer(modifier = Modifier.height(32.dp))

        // Email TextField
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email", color = grayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = textFieldBackground,
                unfocusedContainerColor = textFieldBackground,
                focusedBorderColor = textFieldBorder,
                unfocusedBorderColor = textFieldBorder,
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your password", color = grayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = textFieldBackground,
                unfocusedContainerColor = textFieldBackground,
                focusedBorderColor = textFieldBorder,
                unfocusedBorderColor = textFieldBorder,
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )

        // --- FORGOT PASSWORD ---
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Forgot Password?",
                color = grayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { onForgotPasswordClick() }
            )
        }

        // --- LOGIN BUTTON ---
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AuthColors.AccentBlue)
                .clickable {
                    Log.d(
                        AuthTestLogTag,
                        "[ ${tabs[selectedTabIndex]}, $email, $password ]",
                    )
                    onLoginSuccess(selectedTabIndex == 1)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Login",
                fontSize = 16.sp,
                fontWeight = if (canSubmit) FontWeight.Bold else FontWeight.Medium,
                color = if (canSubmit) Color.White else AuthColors.PrimaryButtonDisabledText,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = primaryTextColor)) {
                    append("Don’t have an account? ")
                }
                withStyle(style = SpanStyle(color = AuthColors.AccentBlue, fontWeight = FontWeight.Bold)) {
                    append("Register Now")
                }
            },
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onRegisterClick() }
        )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeebazarLoginScreenPreview() {
    SeebazarLoginScreen()
}