package com.homeshop.seebazar.servicehome.cardsfeature

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.VendorLocationPrefs
import com.homeshop.seebazar.data.VendorPrefs
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.VendorUi

@Composable
fun MyReservationsScreen(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
    onPersistVendor: () -> Unit = {},
) {
    val placeList = marketplace.reservationPlaceList
    val slotList = marketplace.reservationSlotList
    val business = placeList.firstOrNull()
    val context = LocalContext.current
    val accountNameForReservation = VendorPrefs.cachedDisplayName(context).ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
    }
    val prefAddr = VendorLocationPrefs.addressLine(context)
    val prefCity = VendorLocationPrefs.city(context)
    val prefPostal = VendorLocationPrefs.postalCode(context)

    var showCreatePlace by remember { mutableStateOf(false) }
    var showSlotDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<ReservationSlot?>(null) }

    BackHandler(enabled = showCreatePlace || showSlotDialog) {
        when {
            showSlotDialog -> {
                showSlotDialog = false
                editingSlot = null
            }
            showCreatePlace -> showCreatePlace = false
        }
    }

    CreateReservationPlaceDialog(
        visible = showCreatePlace,
        peekNextReservationBusinessId = marketplace::peekNextReservationBusinessId,
        takeNextReservationBusinessId = marketplace::takeNextReservationBusinessId,
        defaultOwnerName = accountNameForReservation,
        initialAddress = prefAddr,
        initialCity = prefCity,
        initialPostalCode = prefPostal,
        onDismiss = { showCreatePlace = false },
        onSubmit = { place ->
            placeList.clear()
            placeList.add(place)
            onPersistVendor()
        },
    )

    AddReservationSlotDialog(
        visible = showSlotDialog,
        editingSlot = editingSlot,
        peekNextReservationSlotId = marketplace::peekNextReservationSlotId,
        takeNextReservationSlotId = marketplace::takeNextReservationSlotId,
        onDismiss = {
            showSlotDialog = false
            editingSlot = null
        },
        onSubmit = { slot, isEdit ->
            if (isEdit) {
                val i = slotList.indexOfFirst { it.id == slot.id }
                if (i >= 0) slotList[i] = slot
            } else {
                slotList.add(slot)
            }
            onPersistVendor()
        },
    )

    if (business == null) {
        EmptyReservationPlaceState(
            modifier = modifier,
            onCreateClick = { showCreatePlace = true },
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VendorUi.ScreenBg),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ReservationIdentityCard(business = business)
        }
        item {
            Button(
                onClick = {
                    editingSlot = null
                    showSlotDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "POST Reservation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (slotList.isEmpty()) {
            item {
                Text(
                    text = "No reservation slots yet. Tap POST Reservation to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VendorUi.TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(slotList, key = { it.id }) { slot ->
                ReservationSlotCard(
                    slot = slot,
                    onEdit = {
                        editingSlot = slot
                        showSlotDialog = true
                    },
                    onActivate = { s ->
                        val i = slotList.indexOfFirst { it.id == s.id }
                        if (i >= 0) slotList[i] = s.copy(isActive = true)
                    },
                    onDeactivate = { s ->
                        val i = slotList.indexOfFirst { it.id == s.id }
                        if (i >= 0) slotList[i] = s.copy(isActive = false)
                    },
                    onDelete = { s -> slotList.remove(s) },
                )
            }
        }
    }
}

@Composable
private fun EmptyReservationPlaceState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VendorUi.ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No reservation place yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a reservation business to post bookable slots for your customers.",
            style = MaterialTheme.typography.bodyMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Reservation Place")
        }
    }
}

@Composable
private fun ReservationIdentityCard(business: ReservationBusiness) {
    val brandBlue = VendorUi.BrandBlue
    val textMuted = VendorUi.TextMuted
    val textDark = VendorUi.TextDark
    val addressLine = listOf(business.address, business.city, business.postalCode)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8EEF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ReservationPlaceThumb(uriString = business.imageUri)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = business.businessName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFEFF6FF),
                    ) {
                        Text(
                            text = business.businessType.displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = brandBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFF1F5F9),
                ) {
                    Text(
                        text = business.id,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = textMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = business.ownerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textDark,
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (addressLine.isNotBlank()) {
                    Text(
                        text = addressLine,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = textMuted,
                        lineHeight = 22.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                ReservationOpenChip(isOpen = business.isOpen)
            }
        }
    }
}

@Composable
private fun ReservationOpenChip(isOpen: Boolean) {
    val bg = if (isOpen) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val fg = if (isOpen) Color(0xFF166534) else Color(0xFF991B1B)
    val label = if (isOpen) "Open Now" else "Closed"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ReservationPlaceThumb(uriString: String?) {
    val context = LocalContext.current
    val bitmap = remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            runCatching {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }
    Surface(
        modifier = Modifier.size(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF6FF),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(R.drawable.store),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .height(36.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationSlotCard(
    slot: ReservationSlot,
    onEdit: () -> Unit,
    onActivate: (ReservationSlot) -> Unit,
    onDeactivate: (ReservationSlot) -> Unit,
    onDelete: (ReservationSlot) -> Unit,
) {
    val activeGreenBg = Color(0xFFD1FAE5)
    val activeGreenFg = Color(0xFF047857)
    val inactivePillBg = Color(0xFFF1F5F9)
    val inactivePillFg = Color(0xFF64748B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8EEF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                SlotThumb(uriString = slot.imageUri)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = slot.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VendorUi.TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SlotActivePill(
                            isActive = slot.isActive,
                            activeBg = activeGreenBg,
                            activeFg = activeGreenFg,
                            inactiveBg = inactivePillBg,
                            inactiveFg = inactivePillFg,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${slot.id} · ${slot.category.displayLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.TextMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE8EEF4), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            SlotIconRow(
                icon = Icons.Outlined.Schedule,
                label = "Time",
                value = formatSlotTime(slot),
            )
            SlotIconRow(
                icon = Icons.Outlined.People,
                label = "Capacity",
                value = slot.capacity.ifBlank { "—" },
            )
            SlotIconRow(
                icon = Icons.Outlined.Payments,
                label = "Price",
                value = slot.price.ifBlank { "—" },
            )
            Spacer(modifier = Modifier.height(8.dp))
            SlotCompactLine(
                label = "Availability",
                value = buildString {
                    append(slot.totalAvailable.ifBlank { "—" })
                    append(" · ")
                    append(if (slot.availableDaily) "Daily" else slot.specificDate.ifBlank { "—" })
                },
            )
            SlotCompactLine(
                label = "Booking",
                value = slot.bookingType.displayLabel,
            )
            if (slot.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = slot.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF334155),
                    lineHeight = 22.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = VendorUi.BrandBlue, fontWeight = FontWeight.Medium)
                }
                TextButton(onClick = { onActivate(slot) }) {
                    Text("Activate", color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                }
                TextButton(onClick = { onDeactivate(slot) }) {
                    Text("Deactivate", color = VendorUi.TextMuted, fontWeight = FontWeight.Medium)
                }
                TextButton(onClick = { onDelete(slot) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SlotActivePill(
    isActive: Boolean,
    activeBg: Color,
    activeFg: Color,
    inactiveBg: Color,
    inactiveFg: Color,
) {
    val bg = if (isActive) activeBg else inactiveBg
    val fg = if (isActive) activeFg else inactiveFg
    val label = if (isActive) "Active" else "Inactive"
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SlotIconRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = VendorUi.BrandBlue,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
        }
    }
}

@Composable
private fun SlotCompactLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VendorUi.TextMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = VendorUi.TextDark,
        )
    }
}

private fun formatSlotTime(slot: ReservationSlot): String {
    val t = "${slot.startTime.ifBlank { "—" }} – ${slot.endTime.ifBlank { "—" }}"
    return t
}

@Composable
private fun SlotThumb(uriString: String?) {
    val context = LocalContext.current
    val bitmap = remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            runCatching {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VendorUi.ScreenBg),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = if (uriString.isNullOrBlank()) "No image" else "URI",
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
        }
    }
}
