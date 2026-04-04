package com.homeshop.seebazar.servicehome

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AddProductDialog(
    visible: Boolean,
    editingProduct: VendorProduct?,
    peekNextProductId: () -> Int,
    takeNextProductId: () -> Int,
    currentShopName: String = "",
    onDismiss: () -> Unit,
    onSubmit: (VendorProduct, isEdit: Boolean) -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val isEdit = editingProduct != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mrpPrice by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.Grocery) }
    var unit by remember { mutableStateOf("") }
    var shelfLife by remember { mutableStateOf("") }
    var quantityLeft by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var imageDrawableRes by remember { mutableStateOf<Int?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
        if (uri != null) {
            imageDrawableRes = null
        }
    }

    LaunchedEffect(visible, editingProduct?.id) {
        if (!visible) return@LaunchedEffect
        formError = null
        if (editingProduct != null) {
            val p = editingProduct
            name = p.name
            description = p.description
            mrpPrice = p.mrpPrice
            imageUri = p.imageUri
            brand = p.brand
            category = p.category
            unit = p.unit
            shelfLife = p.shelfLife
            quantityLeft = p.quantityLeft
            imageDrawableRes = p.imageDrawableRes
        } else {
            name = ""
            description = ""
            mrpPrice = ""
            imageUri = null
            imageDrawableRes = null
            brand = ""
            category = ProductCategory.Grocery
            unit = ""
            shelfLife = ""
            quantityLeft = ""
        }
    }

    val displayedProductId = editingProduct?.id?.toString() ?: peekNextProductId().toString()

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
        title = if (isEdit) "Edit product" else "Add product",
        subtitle = "Product ID is assigned automatically.",
    ) {
        FormSheetTextField(
            label = "Product ID",
            value = displayedProductId,
            onValueChange = {},
            readOnly = true,
        )
        FormSheetTextField(
            label = "Product name *",
            value = name,
            onValueChange = { name = it },
        )
        FormSheetTextField(
            label = "Description *",
            value = description,
            onValueChange = { description = it },
            singleLine = false,
        )
        FormSheetTextField(
            label = "MRP price *",
            value = mrpPrice,
            onValueChange = { mrpPrice = it },
        )
        OutlinedButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = FieldShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
        ) {
            Text(if (imageUri.isNullOrBlank()) "Pick product image" else "Change image")
        }
        ProductImagePreview(
            uriString = imageUri,
            drawableRes = imageDrawableRes?.takeIf { imageUri.isNullOrBlank() },
        )
        FormSheetTextField(
            label = "Brand *",
            value = brand,
            onValueChange = { brand = it },
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
                    ProductCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayLabel) },
                            onClick = {
                                category = cat
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
            label = "Unit (e.g. 1kg, 500gm, 1L) *",
            value = unit,
            onValueChange = { unit = it },
        )
        FormSheetTextField(
            label = "Shelf life *",
            value = shelfLife,
            onValueChange = { shelfLife = it },
        )
        FormSheetTextField(
            label = "Quantity left *",
            value = quantityLeft,
            onValueChange = { quantityLeft = it },
        )
        formError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val canSave = name.isNotBlank() && description.isNotBlank() && mrpPrice.isNotBlank() &&
            brand.isNotBlank() && unit.isNotBlank() && shelfLife.isNotBlank() && quantityLeft.isNotBlank()
        FormSheetPrimaryButton(
            text = if (isEdit) "Update product" else "POST Product",
            enabled = canSave,
            onClick = {
                formError = null
                if (!canSave) {
                    formError = "Please fill all required fields."
                    return@FormSheetPrimaryButton
                }
                val id = editingProduct?.id ?: takeNextProductId()
                val trimmedUri = imageUri?.trim()?.takeIf { it.isNotBlank() }
                val product = VendorProduct(
                    id = id,
                    name = name.trim(),
                    description = description.trim(),
                    mrpPrice = mrpPrice.trim(),
                    imageUri = trimmedUri,
                    brand = brand.trim(),
                    category = category,
                    unit = unit.trim(),
                    shelfLife = shelfLife.trim(),
                    quantityLeft = quantityLeft.trim(),
                    isActive = editingProduct?.isActive ?: true,
                    imageDrawableRes = if (trimmedUri != null) null else imageDrawableRes,
                    vendorShopName = editingProduct?.vendorShopName?.takeIf { it.isNotBlank() }
                        ?: currentShopName,
                )
                onSubmit(product, isEdit)
                onDismiss()
            },
        )
    }
}

@Composable
private fun ProductImagePreview(uriString: String?, drawableRes: Int?) {
    val context = LocalContext.current
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

    val resId = drawableRes?.takeIf { it != 0 }
    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Product preview",
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }
        resId != null -> {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Product preview",
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
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
