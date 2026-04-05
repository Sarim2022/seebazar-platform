package com.homeshop.seebazar.servicehome.cardsfeature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.rememberDecodedBitmap

private val WindowInsetsZero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

/**
 * Full vendor “My Products” route: single [Scaffold], top app bar, and compact product list.
 * Call this from navigation instead of nesting another [Scaffold] around [MyProducts].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    modifier: Modifier = Modifier,
    products: List<VendorProduct>,
    onBack: () -> Unit,
    onEdit: (VendorProduct) -> Unit,
    onActivate: (VendorProduct) -> Unit,
    onDeactivate: (VendorProduct) -> Unit,
    onDelete: (VendorProduct) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsetsZero,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsetsZero,
                title = {
                    Text(
                        text = "My Products",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VendorUi.TextDark,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VendorUi.TopBarBg,
                    titleContentColor = VendorUi.TextDark,
                    navigationIconContentColor = VendorUi.TextDark,
                ),
            )
        },
    ) { innerPadding ->
        MyProducts(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            products = products,
            onEdit = onEdit,
            onActivate = onActivate,
            onDeactivate = onDeactivate,
            onDelete = onDelete,
        )
    }
}

@Composable
fun MyProducts(
    modifier: Modifier = Modifier,
    products: List<VendorProduct>,
    onEdit: (VendorProduct) -> Unit,
    onActivate: (VendorProduct) -> Unit,
    onDeactivate: (VendorProduct) -> Unit,
    onDelete: (VendorProduct) -> Unit,
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No products yet",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextMuted,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VendorUi.ScreenBg),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(products, key = { it.id }) { product ->
            VendorProductCard(
                product = product,
                onEdit = { onEdit(product) },
                onActivate = { onActivate(product) },
                onDeactivate = { onDeactivate(product) },
                onDelete = { onDelete(product) },
            )
        }
    }
}

private val MrpGreen = Color(0xFF15803D)
private val ActiveBg = Color(0xFFDCFCE7)
private val ActiveText = Color(0xFF166534)
private val InactiveBg = Color(0xFFE2E8F0)
private val InactiveText = Color(0xFF475569)

@Composable
private fun VendorProductCard(
    product: VendorProduct,
    onEdit: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, VendorUi.CardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ProductThumb(
                    uriString = product.imageUri,
                    drawableRes = product.imageDrawableRes,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = VendorUi.TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(isActive = product.isActive)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildSubtitle(product),
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.brand.ifBlank { "No brand" },
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "MRP ${product.mrpPrice}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MrpGreen,
                        )
                        Text(
                            text = "Qty: ${product.quantityLeft.ifBlank { "—" }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = VendorUi.TextDark,
                        )
                    }
                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VendorUi.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        "Edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = VendorUi.BrandBlue,
                    )
                }
                OutlinedButton(
                    onClick = if (product.isActive) onDeactivate else onActivate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (product.isActive) "Deactivate" else "Activate",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (product.isActive) VendorUi.TextMuted else ActiveText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun buildSubtitle(product: VendorProduct): String {
    val unit = product.unit.ifBlank { "—" }
    return "${product.category.displayLabel} · $unit"
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val bg = if (isActive) ActiveBg else InactiveBg
    val fg = if (isActive) ActiveText else InactiveText
    val label = if (isActive) "Active" else "Inactive"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
private fun ProductThumb(uriString: String?, drawableRes: Int?) {
    val bitmap = rememberDecodedBitmap(uriString)
    val resId = drawableRes?.takeIf { it != 0 }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VendorUi.ScreenBg),
        contentAlignment = Alignment.Center,
    ) {
        when {
            resId != null -> {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Product",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            bitmap != null -> {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Product",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            else -> {
                Text(
                    text = "No img",
                    style = MaterialTheme.typography.labelSmall,
                    color = VendorUi.TextMuted,
                )
            }
        }
    }
}
