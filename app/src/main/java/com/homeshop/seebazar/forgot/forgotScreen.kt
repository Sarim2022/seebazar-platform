package com.homeshop.seebazar.forgot

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R
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

    var email by remember { mutableStateOf("") }

    val canSubmit = email.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.forgotbg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = AuthFormMaxWidth)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Forgot Password?\nDon't worry!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 39.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter the email address associated with your account.",
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuthColors.AccentBlue)
                    .clickable(enabled = canSubmit) {
                        Log.d(AuthTestLogTag, "[ forgot password, $email ]")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Reset My Password",
                    fontSize = 16.sp,
                    fontWeight = if (canSubmit) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
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
                    .clickable { onLoginClick() },
            )
        }
    }
}
