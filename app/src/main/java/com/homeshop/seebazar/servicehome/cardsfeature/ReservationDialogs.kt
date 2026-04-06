package com.homeshop.seebazar.servicehome.cardsfeature

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.servicehome.ReservationBookingType
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationBusinessType
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.ReservationSlotCategory
import com.homeshop.seebazar.data.VendorPrefs
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.FormBottomSheetScaffold
import com.homeshop.seebazar.ui.FormSheetDividerColor
import com.homeshop.seebazar.ui.FormSheetPrimaryButton
import com.homeshop.seebazar.ui.FormSheetTextField

private val FieldShape = RoundedCornerShape(12.dp)
private val SheetMenuTextStyle = TextStyle(
    color = VendorUi.TextDark,
    fontSize = 17.sp,
    fontWeight = FontWeight.SemiBold,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationPlaceDialog(
    visible: Boolean,
    peekNextReservationBusinessId: () -> String,
    takeNextReservationBusinessId: () -> String,
    defaultOwnerName: String,
    initialAddress: String,
    initialCity: String,
    initialPostalCode: String,
    initialUpiId: String = "",
    onDismiss: () -> Unit,
    onSubmit: (ReservationBusiness) -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf(ReservationBusinessType.Restaurant) }
    var contactNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("") }
    var closeTime by remember { mutableStateOf("") }
    var totalCapacity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isOpen by remember { mutableStateOf(true) }
    var upiId by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(visible, initialAddress, initialCity, initialPostalCode, initialUpiId) {
        if (!visible) return@LaunchedEffect
        formError = null
        businessName = ""
        businessType = ReservationBusinessType.Restaurant
        contactNumber = ""
        address = initialAddress
        city = initialCity
        postalCode = initialPostalCode
        openTime = ""
        closeTime = ""
        totalCapacity = ""
        imageUri = null
        isOpen = true
        upiId = initialUpiId.trim()
    }

    val displayedId = peekNextReservationBusinessId()

    val menuFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedTextColor = VendorUi.TextDark,
        unfocusedTextColor = VendorUi.TextDark,
        cursorColor = VendorUi.BrandBlue,
        focusedTrailingIconColor = VendorUi.TextMuted,
        unfocusedTrailingIconColor = VendorUi.TextMuted,
    )

    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Create Reservation Place",
        subtitle = "Owner: ${defaultOwnerName.trim().ifBlank { "—" }} · Business ID is assigned when you save.",
    ) {
        FormSheetTextField(
            label = "Reservation Business ID",
            value = displayedId,
            onValueChange = {},
            readOnly = true,
        )
        FormSheetTextField(
            label = "Business Name *",
            value = businessName,
            onValueChange = { businessName = it },
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Business Type",
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = businessType.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = null,
                    textStyle = SheetMenuTextStyle,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(0.dp),
                    colors = menuFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false },
                ) {
                    ReservationBusinessType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.displayLabel) },
                            onClick = {
                                businessType = t
                                typeMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = FormSheetDividerColor)
        }
        FormSheetTextField(
            label = "Contact Number *",
            value = contactNumber,
            onValueChange = { contactNumber = it },
        )
        FormSheetTextField(
            label = "Address *",
            value = address,
            onValueChange = { address = it },
            singleLine = false,
        )
        FormSheetTextField(label = "City *", value = city, onValueChange = { city = it })
        FormSheetTextField(
            label = "Postal Code *",
            value = postalCode,
            onValueChange = { postalCode = it },
        )
        FormSheetTextField(
            label = "Open Time *",
            value = openTime,
            onValueChange = { openTime = it },
        )
        FormSheetTextField(
            label = "Close Time *",
            value = closeTime,
            onValueChange = { closeTime = it },
        )
        FormSheetTextField(
            label = "Total Capacity *",
            value = totalCapacity,
            onValueChange = { totalCapacity = it },
        )
        FormSheetTextField(
            label = "UPI ID *",
            value = upiId,
            onValueChange = { upiId = it },
            placeholder = "e.g. merchant@okicici",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isOpen) "Open" else "Closed",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = isOpen,
                onCheckedChange = { isOpen = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VendorUi.BrandBlue,
                    checkedTrackColor = VendorUi.BrandBlue.copy(alpha = 0.45f),
                ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
            ) {
                Text("Pick image")
            }
            OutlinedButton(
                onClick = { imageUri = "dummy://reservation-place" },
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.TextMuted),
            ) {
                Text("Dummy URI")
            }
        }
        ReservationImagePreview(uriString = imageUri, context = context)
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val owner = defaultOwnerName.trim()
        val canSave = businessName.isNotBlank() && owner.isNotBlank() &&
            contactNumber.isNotBlank() && address.isNotBlank() && city.isNotBlank() &&
            postalCode.isNotBlank() && openTime.isNotBlank() && closeTime.isNotBlank() &&
            totalCapacity.isNotBlank() && VendorPrefs.isValidVendorUpiFormat(upiId)
        FormSheetPrimaryButton(
            text = "Save",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                val id = takeNextReservationBusinessId()
                onSubmit(
                    ReservationBusiness(
                        id = id,
                        businessName = businessName.trim(),
                        businessType = businessType,
                        ownerName = owner,
                        contactNumber = contactNumber.trim(),
                        address = address.trim(),
                        city = city.trim(),
                        postalCode = postalCode.trim(),
                        openTime = openTime.trim(),
                        closeTime = closeTime.trim(),
                        totalCapacity = totalCapacity.trim(),
                        imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
                        isOpen = isOpen,
                        upiId = upiId.trim(),
                    ),
                )
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReservationSlotDialog(
    visible: Boolean,
    editingSlot: ReservationSlot?,
    peekNextReservationSlotId: () -> String,
    takeNextReservationSlotId: () -> String,
    onDismiss: () -> Unit,
    onSubmit: (ReservationSlot, isEdit: Boolean) -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val isEdit = editingSlot != null

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ReservationSlotCategory.Table) }
    var description by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availableDaily by remember { mutableStateOf(true) }
    var specificDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var totalAvailable by remember { mutableStateOf("") }
    var bookingType by remember { mutableStateOf(ReservationBookingType.InstantBooking) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var bookingMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(visible, editingSlot?.id) {
        if (!visible) return@LaunchedEffect
        formError = null
        if (editingSlot != null) {
            val s = editingSlot
            title = s.title
            category = s.category
            description = s.description
            capacity = s.capacity
            price = s.price
            availableDaily = s.availableDaily
            specificDate = s.specificDate
            startTime = s.startTime
            endTime = s.endTime
            totalAvailable = s.totalAvailable
            bookingType = s.bookingType
            imageUri = s.imageUri
            isActive = s.isActive
        } else {
            title = ""
            category = ReservationSlotCategory.Table
            description = ""
            capacity = ""
            price = ""
            availableDaily = true
            specificDate = ""
            startTime = ""
            endTime = ""
            totalAvailable = ""
            bookingType = ReservationBookingType.InstantBooking
            imageUri = null
            isActive = true
        }
    }

    val displayedSlotId = editingSlot?.id ?: peekNextReservationSlotId()

    val menuFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedTextColor = VendorUi.TextDark,
        unfocusedTextColor = VendorUi.TextDark,
        cursorColor = VendorUi.BrandBlue,
        focusedTrailingIconColor = VendorUi.TextMuted,
        unfocusedTrailingIconColor = VendorUi.TextMuted,
    )

    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = if (isEdit) "Edit reservation slot" else "Post reservation slot",
        subtitle = "Slot ID is assigned automatically.",
    ) {
        FormSheetTextField(
            label = "Slot ID",
            value = displayedSlotId,
            onValueChange = {},
            readOnly = true,
        )
        FormSheetTextField(
            label = "Title / Slot Name *",
            value = title,
            onValueChange = { title = it },
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = category.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = null,
                    textStyle = SheetMenuTextStyle,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(0.dp),
                    colors = menuFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false },
                ) {
                    ReservationSlotCategory.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.displayLabel) },
                            onClick = {
                                category = c
                                categoryMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = FormSheetDividerColor)
        }
        FormSheetTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            singleLine = false,
        )
        FormSheetTextField(
            label = "Capacity *",
            value = capacity,
            onValueChange = { capacity = it },
        )
        FormSheetTextField(
            label = "Price *",
            value = price,
            onValueChange = { price = it },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Available daily",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = availableDaily,
                onCheckedChange = { availableDaily = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VendorUi.BrandBlue,
                    checkedTrackColor = VendorUi.BrandBlue.copy(alpha = 0.45f),
                ),
            )
        }
        if (!availableDaily) {
            FormSheetTextField(
                label = "Date *",
                value = specificDate,
                onValueChange = { specificDate = it },
            )
        }
        FormSheetTextField(
            label = "Start Time *",
            value = startTime,
            onValueChange = { startTime = it },
        )
        FormSheetTextField(
            label = "End Time *",
            value = endTime,
            onValueChange = { endTime = it },
        )
        FormSheetTextField(
            label = "Total Available *",
            value = totalAvailable,
            onValueChange = { totalAvailable = it },
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Booking Type",
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = bookingMenuExpanded,
                onExpandedChange = { bookingMenuExpanded = !bookingMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = bookingType.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = null,
                    textStyle = SheetMenuTextStyle,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookingMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(0.dp),
                    colors = menuFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = bookingMenuExpanded,
                    onDismissRequest = { bookingMenuExpanded = false },
                ) {
                    ReservationBookingType.entries.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.displayLabel) },
                            onClick = {
                                bookingType = b
                                bookingMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = FormSheetDividerColor)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isActive) "Active" else "Inactive",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = isActive,
                onCheckedChange = { isActive = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VendorUi.BrandBlue,
                    checkedTrackColor = VendorUi.BrandBlue.copy(alpha = 0.45f),
                ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
            ) {
                Text("Pick image")
            }
            OutlinedButton(
                onClick = { imageUri = "dummy://reservation-slot" },
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.TextMuted),
            ) {
                Text("Dummy URI")
            }
        }
        ReservationImagePreview(uriString = imageUri, context = context)
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val canSave = title.isNotBlank() && capacity.isNotBlank() && price.isNotBlank() &&
            startTime.isNotBlank() && endTime.isNotBlank() && totalAvailable.isNotBlank() &&
            (availableDaily || specificDate.isNotBlank())
        FormSheetPrimaryButton(
            text = if (isEdit) "Update" else "Post slot",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                val id = editingSlot?.id ?: takeNextReservationSlotId()
                val slot = ReservationSlot(
                    id = id,
                    title = title.trim(),
                    category = category,
                    description = description.trim(),
                    capacity = capacity.trim(),
                    price = price.trim(),
                    availableDaily = availableDaily,
                    specificDate = if (availableDaily) "" else specificDate.trim(),
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    totalAvailable = totalAvailable.trim(),
                    bookingType = bookingType,
                    imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
                    isActive = isActive,
                )
                onSubmit(slot, isEdit)
                onDismiss()
            },
        )
    }
}

@Composable
private fun ReservationImagePreview(uriString: String?, context: android.content.Context) {
    val bitmap = remember(uriString) {
        if (uriString.isNullOrBlank()) {
            null
        } else {
            runCatching {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }
    val modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 120.dp, max = 180.dp)
        .background(VendorUi.ScreenBg, RoundedCornerShape(12.dp))

    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }
        !uriString.isNullOrBlank() -> {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Dummy / preview URI",
                    style = MaterialTheme.typography.labelMedium,
                    color = VendorUi.TextMuted,
                )
                Text(
                    text = uriString,
                    style = MaterialTheme.typography.bodySmall,
                    color = VendorUi.BrandBlue,
                )
            }
        }
        else -> {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No image selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VendorUi.TextMuted,
                )
            }
        }
    }
}
