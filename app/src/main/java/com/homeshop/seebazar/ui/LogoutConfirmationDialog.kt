package com.homeshop.seebazar.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.servicehome.VendorUi

private val LogoutRed = Color(0xFFDC2626)

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Log out?",
    ) {
        Text(
            text = "Are you sure you want to log out?",
            style = MaterialTheme.typography.bodyMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LogoutRed),
        ) {
            Text("Log out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.TextDark),
        ) {
            Text("Cancel", fontWeight = FontWeight.Medium)
        }
    }
}
