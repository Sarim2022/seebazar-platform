package com.homeshop.seebazar.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PaymentSuccessScreen(
    onGoToOrders: () -> Unit,
) {
    BackHandler { onGoToOrders() }

    Scaffold(
        containerColor = Color(0xFFF7F8FA) // Off-white for visual consistency
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp), // Clear side padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to force content to center vertically
            Spacer(modifier = Modifier.weight(1f))

            // --- 3. Recreate the Success Icon ---
            Surface(
                modifier = Modifier
                    .size(120.dp) // Large and bold
                    .background(Color(0xFF2F80ED), CircleShape), // Consistent blue color
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(28.dp) // Position the checkmark
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Success Text ---
            Text(
                text = "Payment Successful",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center
            )

            // Second Spacer to push the button to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // --- 5. Action Button (New element to complete the UX) ---
            Button(
                onClick = onGoToOrders,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Premium, standard height
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F80ED), // Branded blue color
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // Consistent corners
            ) {
                Text(
                    text = "Go to Orders",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Final bottom spacing
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PaymentSuccessScreenPreview() {
    PaymentSuccessScreen(onGoToOrders = {})
}