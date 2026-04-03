package com.homeshop.seebazar.forgot

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.homeshop.seebazar.ui.AuthColors

private val AuthFormMaxWidth = 420.dp
private const val AuthTestLogTag = "SeebazarAuth"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeebazarForgotScreen(onLoginClick: () -> Unit = {}) {
    val primaryTextColor = Color(0xFF1E232C)
    val textFieldBackground = Color(0xFFF7F8F9)
    val textFieldBorder = Color(0xFFE8ECF4)
    val grayText = Color(0xFF6A707C)

    // State
    var email by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("User", "Service")

    val canSubmit = email.isNotBlank()

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
            text = "Forgot Password?\nDon't worry!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AuthColors.AccentBlue,
            lineHeight = 39.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter the email address associated with your account.",
            fontSize = 15.sp,
            color = grayText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- MINIMAL SQUARE TAB LAYOUT ---
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = textFieldBackground,
            contentColor = AuthColors.AccentBlue,
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
                        .background(Color.White, RoundedCornerShape(2.dp))
                        .border(1.dp, textFieldBorder, RoundedCornerShape(2.dp))
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) AuthColors.AccentBlue else AuthColors.AccentBlueLight
                        )
                    },
                    modifier = Modifier.zIndex(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- EMAIL INPUT ---
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

        Spacer(modifier = Modifier.height(32.dp))

        // --- RESET BUTTON ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AuthColors.AccentBlue)
                .clickable(enabled = canSubmit) {
                    Log.d(
                        AuthTestLogTag,
                        "[ ${tabs[selectedTabIndex]}, $email ]",
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reset My Password",
                fontSize = 16.sp,
                fontWeight = if (canSubmit) FontWeight.Bold else FontWeight.Medium,
                color = if (canSubmit) Color.White else AuthColors.PrimaryButtonDisabledText,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = primaryTextColor)) {
                    append("Remember Password? ")
                }
                withStyle(style = SpanStyle(color = AuthColors.AccentBlue, fontWeight = FontWeight.Bold)) {
                    append("Login Now")
                }
            },
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onLoginClick() }
        )
        }
    }
}