package com.homeshop.seebazar.servicehome

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
import com.homeshop.seebazar.data.ShopDetails
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
fun CreateShopAccountDialog(
    visible: Boolean,
    suggestedVendorId: String,
    defaultOwnerName: String,
    initialAddress: String,
    initialCity: String,
    initialPostalCode: String,
    onDismiss: () -> Unit,
    onSubmit: (ShopDetails) -> Unit,
) {
    if (!visible) return

    var shopName by remember { mutableStateOf("") }
    var vendorId by remember { mutableStateOf(suggestedVendorId) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var isOpen by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible, suggestedVendorId, initialAddress, initialCity, initialPostalCode) {
        if (!visible) return@LaunchedEffect
        formError = null
        shopName = ""
        vendorId = suggestedVendorId
        address = initialAddress
        city = initialCity
        postalCode = initialPostalCode
        isOpen = false
    }

    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Create shop account",
        subtitle = "Owner name comes from your account (${defaultOwnerName.trim().ifBlank { "—" }}).",
    ) {
        FormSheetTextField(
            label = "Shop name *",
            value = shopName,
            onValueChange = { shopName = it },
        )
        FormSheetTextField(
            label = "Vendor ID *",
            value = vendorId,
            onValueChange = { vendorId = it },
        )
        FormSheetTextField(
            label = "Address *",
            value = address,
            onValueChange = { address = it },
            singleLine = false,
        )
        FormSheetTextField(
            label = "City *",
            value = city,
            onValueChange = { city = it },
        )
        FormSheetTextField(
            label = "Postal code *",
            value = postalCode,
            onValueChange = { postalCode = it },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (isOpen) "Shop open" else "Shop closed",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = isOpen,
                onCheckedChange = { isOpen = it },
            )
        }
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val owner = defaultOwnerName.trim()
        val canSave = shopName.isNotBlank() && vendorId.isNotBlank() && owner.isNotBlank() &&
            address.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
        FormSheetPrimaryButton(
            text = "Save shop",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                onSubmit(
                    ShopDetails(
                        shopName = shopName.trim(),
                        vendorId = vendorId.trim(),
                        ownerName = owner,
                        address = address.trim(),
                        city = city.trim(),
                        postalCode = postalCode.trim(),
                        isOpen = isOpen,
                    ),
                )
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceProfileDialog(
    visible: Boolean,
    peekNextProfileId: () -> String,
    takeNextProfileId: () -> String,
    defaultProviderName: String,
    initialServiceArea: String,
    onDismiss: () -> Unit,
    onSubmit: (VendorServiceProfile) -> Unit,
) {
    if (!visible) return

    var profession by remember { mutableStateOf(ServiceProfession.Electrician) }
    var experienceYears by remember { mutableStateOf("") }
    var serviceArea by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var chargesType by remember { mutableStateOf(ServiceChargesType.PerVisit) }
    var baseCharge by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isAvailable by remember { mutableStateOf(true) }
    var professionMenuExpanded by remember { mutableStateOf(false) }
    var chargesMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(visible, initialServiceArea) {
        if (!visible) return@LaunchedEffect
        formError = null
        profession = ServiceProfession.Electrician
        experienceYears = ""
        serviceArea = initialServiceArea
        contactNumber = ""
        shortDescription = ""
        chargesType = ServiceChargesType.PerVisit
        baseCharge = ""
        imageUri = null
        isAvailable = true
    }

    val displayedId = peekNextProfileId()

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
        title = "Create Service Profile",
        subtitle = "Provider: ${defaultProviderName.trim().ifBlank { "—" }} · ID is assigned when you save.",
    ) {
        FormSheetTextField(
            label = "Service Profile ID",
            value = displayedId,
            onValueChange = {},
            readOnly = true,
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Profession",
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = professionMenuExpanded,
                onExpandedChange = { professionMenuExpanded = !professionMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = profession.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = null,
                    textStyle = SheetMenuTextStyle,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(0.dp),
                    colors = menuFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = professionMenuExpanded,
                    onDismissRequest = { professionMenuExpanded = false },
                ) {
                    ServiceProfession.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.displayLabel) },
                            onClick = {
                                profession = p
                                professionMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = FormSheetDividerColor)
        }
        FormSheetTextField(
            label = "Experience (years) *",
            value = experienceYears,
            onValueChange = { experienceYears = it },
        )
        FormSheetTextField(
            label = "Service area / city *",
            value = serviceArea,
            onValueChange = { serviceArea = it },
        )
        FormSheetTextField(
            label = "Contact number *",
            value = contactNumber,
            onValueChange = { contactNumber = it },
        )
        FormSheetTextField(
            label = "Short description *",
            value = shortDescription,
            onValueChange = { shortDescription = it },
            singleLine = false,
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Charges type",
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = chargesMenuExpanded,
                onExpandedChange = { chargesMenuExpanded = !chargesMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = chargesType.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = null,
                    textStyle = SheetMenuTextStyle,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = chargesMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(0.dp),
                    colors = menuFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = chargesMenuExpanded,
                    onDismissRequest = { chargesMenuExpanded = false },
                ) {
                    ServiceChargesType.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.displayLabel) },
                            onClick = {
                                chargesType = c
                                chargesMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = FormSheetDividerColor)
        }
        FormSheetTextField(
            label = "Base charge *",
            value = baseCharge,
            onValueChange = { baseCharge = it },
        )
        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = FieldShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
        ) {
            Text(if (imageUri.isNullOrBlank()) "Pick profile image" else "Change image")
        }
        ServiceImagePreview(uriString = imageUri, context = context)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (isAvailable) "Available" else "Busy",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = isAvailable,
                onCheckedChange = { isAvailable = it },
            )
        }
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val provider = defaultProviderName.trim()
        val canSave = provider.isNotBlank() && experienceYears.isNotBlank() &&
            serviceArea.isNotBlank() && contactNumber.isNotBlank() &&
            shortDescription.isNotBlank() && baseCharge.isNotBlank()
        FormSheetPrimaryButton(
            text = "Save profile",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                val id = takeNextProfileId()
                onSubmit(
                    VendorServiceProfile(
                        id = id,
                        providerName = provider,
                        profession = profession,
                        experienceYears = experienceYears.trim(),
                        serviceArea = serviceArea.trim(),
                        contactNumber = contactNumber.trim(),
                        shortDescription = shortDescription.trim(),
                        chargesType = chargesType,
                        baseCharge = baseCharge.trim(),
                        imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
                        isAvailable = isAvailable,
                    ),
                )
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceDialog(
    visible: Boolean,
    editingPost: VendorServicePost?,
    peekNextPostId: () -> String,
    takeNextPostId: () -> String,
    onDismiss: () -> Unit,
    onSubmit: (VendorServicePost, isEdit: Boolean) -> Unit,
) {
    if (!visible) return

    val isEdit = editingPost != null
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(VendorServicePostCategory.Installation) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var emergencyAvailable by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(visible, editingPost?.id) {
        if (!visible) return@LaunchedEffect
        formError = null
        if (editingPost != null) {
            val p = editingPost
            title = p.title
            category = p.category
            description = p.description
            price = p.price
            estimatedTime = p.estimatedTime
            emergencyAvailable = p.emergencyAvailable
            imageUri = p.imageUri
            isActive = p.isActive
        } else {
            title = ""
            category = VendorServicePostCategory.Installation
            description = ""
            price = ""
            estimatedTime = ""
            emergencyAvailable = false
            imageUri = null
            isActive = true
        }
    }

    val displayedPostId = editingPost?.id ?: peekNextPostId()

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
        title = if (isEdit) "Edit service" else "Your primary service",
        subtitle = "Service Post ID is assigned automatically.",
    ) {
        FormSheetTextField(
            label = "Service Post ID",
            value = displayedPostId,
            onValueChange = {},
            readOnly = true,
        )
        FormSheetTextField(
            label = "Service title *",
            value = title,
            onValueChange = { title = it },
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Service category",
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
                    VendorServicePostCategory.entries.forEach { c ->
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
            label = "Description *",
            value = description,
            onValueChange = { description = it },
            singleLine = false,
        )
        FormSheetTextField(
            label = "Price *",
            value = price,
            onValueChange = { price = it },
        )
        FormSheetTextField(
            label = "Estimated time (e.g. 30 min, 1 hour) *",
            value = estimatedTime,
            onValueChange = { estimatedTime = it },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Emergency available",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = emergencyAvailable,
                onCheckedChange = { emergencyAvailable = it },
            )
        }
        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = FieldShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
        ) {
            Text(if (imageUri.isNullOrBlank()) "Pick service image" else "Change image")
        }
        ServiceImagePreview(uriString = imageUri, context = context)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (isActive) "Active" else "Inactive",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextDark,
            )
            Switch(
                checked = isActive,
                onCheckedChange = { isActive = it },
            )
        }
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val canSave = title.isNotBlank() && description.isNotBlank() &&
            price.isNotBlank() && estimatedTime.isNotBlank()
        FormSheetPrimaryButton(
            text = if (isEdit) "Update service" else "Save service",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                val id = editingPost?.id ?: takeNextPostId()
                val post = VendorServicePost(
                    id = id,
                    title = title.trim(),
                    category = category,
                    description = description.trim(),
                    price = price.trim(),
                    estimatedTime = estimatedTime.trim(),
                    emergencyAvailable = emergencyAvailable,
                    imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
                    isActive = isActive,
                )
                onSubmit(post, isEdit)
                onDismiss()
            },
        )
    }
}

@Composable
private fun ServiceImagePreview(uriString: String?, context: android.content.Context) {
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

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Image preview",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
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
