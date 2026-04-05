package com.homeshop.seebazar.signup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.homeshop.seebazar.data.UserFirestore
import com.homeshop.seebazar.ui.AuthColors

private val AuthFormMaxWidth = 420.dp
private const val AuthTestLogTag = "SeebazarAuth"

/** Pure white register label when the form is valid (including matching passwords). */
private val RegisterTextValid = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeebazarSignupScreen(
    onLoginClick: () -> Unit = {},
    /** Called after Auth + Firestore profile succeed; host should sign out and show login. */
    onRegisterComplete: () -> Unit = {},
) {
    val context = LocalContext.current
    val primaryTextColor = Color(0xFF1E232C)
    val textFieldBackground = Color(0xFFF7F8F9)
    val textFieldBorder = Color(0xFFE8ECF4)
    val grayText = Color(0xFF6A707C)

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isRegistering by remember { mutableStateOf(false) }

    val tabs = listOf("User", "Service")
    val passwordsMatch = password == confirmPassword
    val formValid = name.isNotBlank() && email.isNotBlank() &&
        password.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch

    val auth = remember { FirebaseAuth.getInstance() }

    fun register() {
        if (!formValid || isRegistering) return
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()
        val accountType = tabs[selectedTabIndex]
        isRegistering = true
        auth.createUserWithEmailAndPassword(trimmedEmail, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    isRegistering = false
                    val err = task.exception
                    when (err) {
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(context, "Account already exists", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            Toast.makeText(context, "Password is too weak", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(context, "Invalid email or credentials", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseNetworkException -> {
                            Toast.makeText(context, "Network error. Check your connection.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Log.e(AuthTestLogTag, "Signup failed", err)
                            Toast.makeText(
                                context,
                                err?.localizedMessage ?: "Registration failed",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                    return@addOnCompleteListener
                }
                val user = task.result?.user
                if (user == null) {
                    isRegistering = false
                    Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                val uid = user.uid
                val profile = hashMapOf<String, Any>(
                    "uid" to uid,
                    "name" to trimmedName,
                    "email" to trimmedEmail.lowercase(),
                    "type" to accountType,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "islogin" to false,
                )
                if (accountType == "User") {
                    profile[UserFirestore.FIELD_MY_KART] = emptyList<Map<String, Any>>()
                    profile[UserFirestore.FIELD_MY_ORDER] = emptyList<Map<String, Any>>()
                }
                if (accountType == "Service") {
                    profile["isShopprofile"] = false
                    profile["isServiceprofile"] = false
                    profile["isReservation"] = false
                    profile[UserFirestore.FIELD_WALLET_VENDOR] = emptyList<String>()
                }
                UserFirestore.usersCollection().document(uid).set(profile)
                    .addOnCompleteListener { fsTask ->
                        isRegistering = false
                        if (!fsTask.isSuccessful) {
                            Log.e(AuthTestLogTag, "Firestore profile write failed", fsTask.exception)
                            Toast.makeText(
                                context,
                                "Account created but profile save failed. Try again from settings.",
                                Toast.LENGTH_LONG,
                            ).show()
                            return@addOnCompleteListener
                        }
                        Toast.makeText(context, "Account created", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                        onRegisterComplete()
                    }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
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
                text = "Hello! Register to get\nstarted",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = AuthColors.AccentBlue,
                lineHeight = 39.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                            .border(1.dp, textFieldBorder, RoundedCornerShape(2.dp)),
                    )
                },
                divider = {},
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
                                color = if (selectedTabIndex == index) AuthColors.AccentBlue else AuthColors.AccentBlueLight,
                            )
                        },
                        modifier = Modifier.zIndex(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name", color = grayText) },
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email", color = grayText) },
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = grayText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = textFieldBackground,
                    unfocusedContainerColor = textFieldBackground,
                    focusedBorderColor = textFieldBorder,
                    unfocusedBorderColor = textFieldBorder,
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password", color = grayText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = textFieldBackground,
                    unfocusedContainerColor = textFieldBackground,
                    focusedBorderColor = textFieldBorder,
                    unfocusedBorderColor = textFieldBorder,
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuthColors.AccentBlue)
                    .clickable(enabled = formValid && !isRegistering) { register() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isRegistering) "Please wait…" else "Register",
                    fontSize = 16.sp,
                    fontWeight = if (formValid) FontWeight.Bold else FontWeight.Medium,
                    color = if (formValid) RegisterTextValid else AuthColors.PrimaryButtonDisabledText,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = primaryTextColor)) {
                        append("Already have an account? ")
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

@Preview(showBackground = true)
@Composable
fun SeebazarLoginScreenPreview() {
    SeebazarSignupScreen(
        onLoginClick = {},
    )
}
