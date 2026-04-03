package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val FieldShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPostDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPostProduct: (VendorProduct) -> Unit,
    onPostReservation: (VendorReservation) -> Unit,
    onPostService: (VendorServiceItem) -> Unit,
    nextProductId: () -> Int,
) {
    if (!visible) return

    var step by remember { mutableStateOf(PostDialogStep.PickType) }
    var kind by remember { mutableStateOf<PostKind?>(null) }

    // Product
    var pName by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pBrand by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf(ProductCategory.Grocery) }
    var pPrice by remember { mutableStateOf("") }
    var pUnit by remember { mutableStateOf("") }
    var pTags by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Reservation
    var rVenue by remember { mutableStateOf("") }
    var rDate by remember { mutableStateOf("") }
    var rTime by remember { mutableStateOf("") }
    var rPeople by remember { mutableStateOf("") }
    var rInstr by remember { mutableStateOf("") }
    var rPrice by remember { mutableStateOf("") }

    // Service
    var sName by remember { mutableStateOf("") }
    var sPrice by remember { mutableStateOf("") }
    var sAvail by remember { mutableStateOf("") }
    var sInstr by remember { mutableStateOf("") }

    LaunchedEffect(visible) {
        if (visible) {
            step = PostDialogStep.PickType
            kind = null
            pName = ""
            pDesc = ""
            pBrand = ""
            pCategory = ProductCategory.Grocery
            pPrice = ""
            pUnit = ""
            pTags = ""
            rVenue = ""
            rDate = ""
            rTime = ""
            rPeople = ""
            rInstr = ""
            rPrice = ""
            sName = ""
            sPrice = ""
            sAvail = ""
            sInstr = ""
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VendorUi.BrandBlue,
        focusedLabelColor = VendorUi.BrandBlue,
        cursorColor = VendorUi.BrandBlue,
        focusedTrailingIconColor = VendorUi.BrandBlue,
    )

    @Composable
    fun Field(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        singleLine: Boolean = true,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            shape = FieldShape,
            colors = fieldColors,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (step) {
                    PostDialogStep.PickType -> "Create post"
                    PostDialogStep.Form -> when (kind) {
                        PostKind.Product -> "New product"
                        PostKind.Reservation -> "New reservation"
                        PostKind.Service -> "New service"
                        null -> "New post"
                    }
                },
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (step) {
                    PostDialogStep.PickType -> {
                        Text(
                            text = "What would you like to add?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VendorUi.TextMuted,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                kind = PostKind.Product
                                step = PostDialogStep.Form
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = FieldShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = VendorUi.BrandBlue,
                            ),
                        ) {
                            Text("New product")
                        }
                        OutlinedButton(
                            onClick = {
                                kind = PostKind.Reservation
                                step = PostDialogStep.Form
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = FieldShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = VendorUi.BrandBlue,
                            ),
                        ) {
                            Text("New reservation")
                        }
                        OutlinedButton(
                            onClick = {
                                kind = PostKind.Service
                                step = PostDialogStep.Form
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = FieldShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = VendorUi.BrandBlue,
                            ),
                        ) {
                            Text("New service")
                        }
                    }
                    PostDialogStep.Form -> when (kind) {
                        PostKind.Product -> {
                            TextButton(onClick = { step = PostDialogStep.PickType }) {
                                Text("← Back", color = VendorUi.BrandBlue)
                            }
                            Field(pName, { pName = it }, "Name *")
                            Field(pDesc, { pDesc = it }, "Description", singleLine = false)
                            Field(pBrand, { pBrand = it }, "Brand")
                            ExposedDropdownMenuBox(
                                expanded = categoryMenuExpanded,
                                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OutlinedTextField(
                                    value = pCategory.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = categoryMenuExpanded,
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    shape = FieldShape,
                                    colors = fieldColors,
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false },
                                ) {
                                    ProductCategory.entries.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name) },
                                            onClick = {
                                                pCategory = cat
                                                categoryMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            Field(pPrice, { pPrice = it }, "Price *")
                            Field(pUnit, { pUnit = it }, "Unit (e.g. 1kg, 100ml)")
                            Field(pTags, { pTags = it }, "Tags (optional)")
                        }
                        PostKind.Reservation -> {
                            TextButton(onClick = { step = PostDialogStep.PickType }) {
                                Text("← Back", color = VendorUi.BrandBlue)
                            }
                            Field(rVenue, { rVenue = it }, "Venue name *")
                            Field(rDate, { rDate = it }, "Date")
                            Field(rTime, { rTime = it }, "Time slot")
                            Field(rPeople, { rPeople = it }, "Number of people")
                            Field(rInstr, { rInstr = it }, "Instructions", singleLine = false)
                            Field(rPrice, { rPrice = it }, "Price *")
                        }
                        PostKind.Service -> {
                            TextButton(onClick = { step = PostDialogStep.PickType }) {
                                Text("← Back", color = VendorUi.BrandBlue)
                            }
                            Field(sName, { sName = it }, "Service name *")
                            Field(sPrice, { sPrice = it }, "Fixed price *")
                            Field(sAvail, { sAvail = it }, "Availability")
                            Field(sInstr, { sInstr = it }, "Instruction", singleLine = false)
                        }
                        null -> {}
                    }
                }
            }
        },
        confirmButton = {
            if (step == PostDialogStep.Form && kind != null) {
                TextButton(
                    onClick = {
                        when (kind) {
                            PostKind.Product -> {
                                if (pName.isNotBlank() && pPrice.isNotBlank()) {
                                    onPostProduct(
                                        VendorProduct(
                                            id = nextProductId(),
                                            name = pName.trim(),
                                            description = pDesc.trim(),
                                            brand = pBrand.trim(),
                                            category = pCategory,
                                            price = pPrice.trim(),
                                            unit = pUnit.trim(),
                                            tags = pTags.trim().ifBlank { null },
                                        ),
                                    )
                                    onDismiss()
                                }
                            }
                            PostKind.Reservation -> {
                                if (rVenue.isNotBlank() && rPrice.isNotBlank()) {
                                    onPostReservation(
                                        VendorReservation(
                                            venueName = rVenue.trim(),
                                            date = rDate.trim(),
                                            timeSlot = rTime.trim(),
                                            numPeople = rPeople.trim(),
                                            instructions = rInstr.trim(),
                                            price = rPrice.trim(),
                                        ),
                                    )
                                    onDismiss()
                                }
                            }
                            PostKind.Service -> {
                                if (sName.isNotBlank() && sPrice.isNotBlank()) {
                                    onPostService(
                                        VendorServiceItem(
                                            serviceName = sName.trim(),
                                            priceFixed = sPrice.trim(),
                                            availability = sAvail.trim(),
                                            instruction = sInstr.trim(),
                                        ),
                                    )
                                    onDismiss()
                                }
                            }
                            null -> {}
                        }
                    },
                ) {
                    Text("Post", color = VendorUi.BrandBlue)
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VendorUi.TextMuted)
                }
            }
        },
    )
}

private enum class PostDialogStep { PickType, Form }

private enum class PostKind { Product, Reservation, Service }
