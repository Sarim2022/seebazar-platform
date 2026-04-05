package com.homeshop.seebazar.ui // Update with your actual package

import android.util.Log
import android.widget.Toast
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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.Source
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.UserFirestore
import com.homeshop.seebazar.data.UserProfilePrefs
import com.homeshop.seebazar.data.VendorFirestoreSync
import com.homeshop.seebazar.data.VendorLocationPrefs
import com.homeshop.seebazar.data.VendorPrefs

private val AuthFormMaxWidth = 420.dp
private const val AuthTestLogTag = "SeebazarAuth"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeebazarLoginScreen(
    marketplace: MarketplaceData? = null,
    onBackClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    /**
     * Invoked after successful sign-in, profile load, and `islogin=true` in Firestore.
     * @param isServiceTab `true` when Firestore `type` is **Service** (vendor home).
     */
    onLoginSuccess: (isServiceTab: Boolean) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val primaryTextColor = Color(0xFF1E232C)
    val textFieldBackground = Color(0xFFF7F8F9)
    val textFieldBorder = Color(0xFFE8ECF4)
    val grayText = Color(0xFF6A707C)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    val canSubmit = email.isNotBlank() && password.isNotBlank()

    fun login() {
        if (!canSubmit || isLoggingIn) return
        val trimmedEmail = email.trim()
        isLoggingIn = true
        auth.signInWithEmailAndPassword(trimmedEmail, password)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    isLoggingIn = false
                    when (val e = authTask.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(context, "No account for this email", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseNetworkException -> {
                            Toast.makeText(context, "Network error. Check your connection.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Log.e(AuthTestLogTag, "Login failed", e)
                            Toast.makeText(
                                context,
                                e?.localizedMessage ?: "Login failed",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                    return@addOnCompleteListener
                }
                val uid = authTask.result?.user?.uid
                if (uid == null) {
                    isLoggingIn = false
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                UserFirestore.usersCollection().document(uid).get(Source.SERVER)
                    .addOnCompleteListener { profileTask ->
                        if (!profileTask.isSuccessful) {
                            isLoggingIn = false
                            auth.signOut()
                            Log.e(AuthTestLogTag, "Profile fetch failed", profileTask.exception)
                            Toast.makeText(context, "Could not load your profile", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        val snap = profileTask.result
                        if (snap == null || !snap.exists()) {
                            isLoggingIn = false
                            auth.signOut()
                            Toast.makeText(
                                context,
                                "No profile found. Please register first.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@addOnCompleteListener
                        }
                        val storedEmail = snap.getString("email")?.trim()?.lowercase().orEmpty()
                        if (storedEmail.isNotEmpty() && storedEmail != trimmedEmail.lowercase()) {
                            isLoggingIn = false
                            auth.signOut()
                            Toast.makeText(context, "Email does not match this account", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        val type = snap.getString(UserFirestore.FIELD_TYPE)?.trim().orEmpty()
                        val isVendor = type.equals("Service", ignoreCase = true)
                        val displayName = snap.getString(UserFirestore.FIELD_NAME).orEmpty()
                        val profileEmail = snap.getString(UserFirestore.FIELD_EMAIL).orEmpty()
                        UserProfilePrefs.persist(
                            context,
                            uid,
                            displayName,
                            profileEmail,
                            type.ifBlank { if (isVendor) "Service" else "User" },
                        )
                        if (isVendor) {
                            marketplace?.let { m ->
                                VendorFirestoreSync.applySnapshot(snap, m)
                                VendorPrefs.persist(
                                    context,
                                    uid,
                                    displayName,
                                    profileEmail,
                                    m,
                                )
                            }
                        } else {
                            marketplace?.resetForUserSession()
                            VendorPrefs.clear(context)
                            VendorLocationPrefs.clear(context)
                        }
                        UserFirestore.usersCollection().document(uid)
                            .update(UserFirestore.FIELD_IS_LOGIN, true)
                            .addOnCompleteListener { upd ->
                                isLoggingIn = false
                                if (!upd.isSuccessful) {
                                    auth.signOut()
                                    Log.e(AuthTestLogTag, "islogin update failed", upd.exception)
                                    Toast.makeText(context, "Could not update session", Toast.LENGTH_SHORT).show()
                                    return@addOnCompleteListener
                                }
                                onLoginSuccess(isVendor)
                            }
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
                text = "Welcome back! Glad\nto see you, Again!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = AuthColors.AccentBlue,
                lineHeight = 39.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

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
                visualTransformation = PasswordVisualTransformation(),
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Forgot Password?",
                    color = grayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onForgotPasswordClick() },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuthColors.AccentBlue)
                    .clickable(enabled = canSubmit && !isLoggingIn) { login() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isLoggingIn) "Please wait…" else "Login",
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
                    .clickable { onRegisterClick() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeebazarLoginScreenPreview() {
    SeebazarLoginScreen()
}
