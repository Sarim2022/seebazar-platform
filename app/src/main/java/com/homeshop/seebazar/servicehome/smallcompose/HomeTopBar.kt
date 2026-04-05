package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R
import com.homeshop.seebazar.data.ShopDetails
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorServiceProfile
import com.homeshop.seebazar.ui.ellipsizeLocationHeadline

private enum class HomeTopPagerPage {
    Shop,
    Service,
    Reservation,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTopBar(
    shop: ShopDetails?,
    serviceProfile: VendorServiceProfile?,
    reservationBusiness: ReservationBusiness?,
    /** Active listings preview on the service home card (same list as [com.homeshop.seebazar.data.MarketplaceData.servicePostList]). */
    servicePosts: List<VendorServicePost> = emptyList(),
    /** Top-left headline (e.g. city or “Location”). */
    locationHeadline: String,
    /** Top-left detail line (street or coordinates). */
    locationSubtitle: String,
    /** Initial in the profile chip. */
    profileInitial: String,
    onProfileClick: () -> Unit = {},
    onShopCardClick: () -> Unit = {},
    onServiceCardClick: () -> Unit = {},
    onReservationCardClick: () -> Unit = {},
    onShareDetails: (String) -> Unit = {},
    /** Tap on the top-left location block (e.g. request permission / refresh GPS). */
    onLocationClick: () -> Unit = {},
) {
    val brandBlue = Color(0xFF155AC1)
    val textMuted = Color(0xFF64748B)
    val textDark = Color(0xFF0F172A)

    val gradientColors = listOf(
        Color(0xFF6FA7F1),
        Color(0xFFA3D8FC),
        Color(0xFFF0F9FF),
    )

    val pages = remember(shop, serviceProfile, reservationBusiness) {
        buildList {
            if (shop != null) add(HomeTopPagerPage.Shop)
            if (serviceProfile != null) add(HomeTopPagerPage.Service)
            if (reservationBusiness != null) add(HomeTopPagerPage.Reservation)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                ),
            )
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // LEFT SECTION: vendor location
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationClick() },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ellipsizeLocationHeadline(locationHeadline, blankFallback = "Location"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Text(
                    text = locationSubtitle.ifBlank { "Add location permission to show your area" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // RIGHT SECTION: Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {


                // 3. Profile Initial Circle — opens vendor settings
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onProfileClick() },
                    shape = CircleShape,
                    color = Color(0xFFFDEFD5), // Cream color from image
                    border = BorderStroke(1.dp, Color(0xFF0090FF)), // Gold border
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profileInitial.ifBlank { "?" }.take(1),
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when {
            pages.isEmpty() -> Spacer(modifier = Modifier.height(8.dp))
            pages.size == 1 -> when (pages[0]) {
                HomeTopPagerPage.Shop -> {
                    val s = shop!!
                    ShopHomeCard(
                        shop = s,
                        brandBlue = brandBlue,
                        textMuted = textMuted,
                        textDark = textDark,
                        onCardClick = onShopCardClick,
                        onShareClick = { onShareDetails(shopDetailsShareText(s)) },
                    )
                }
                HomeTopPagerPage.Service -> {
                    val p = serviceProfile!!
                    ServiceHomeCard(
                        profile = p,
                        servicePosts = servicePosts,
                        brandBlue = brandBlue,
                        textMuted = textMuted,
                        textDark = textDark,
                        onCardClick = onServiceCardClick,
                        onShareClick = { onShareDetails(serviceProfileShareText(p)) },
                    )
                }
                HomeTopPagerPage.Reservation -> {
                    val b = reservationBusiness!!
                    ReservationHomeCard(
                        business = b,
                        brandBlue = brandBlue,
                        textMuted = textMuted,
                        textDark = textDark,
                        onCardClick = onReservationCardClick,
                        onShareClick = { onShareDetails(reservationBusinessShareText(b)) },
                    )
                }
            }
            else -> {
                val pagerState = rememberPagerState(pageCount = { pages.size })
                LaunchedEffect(pages.size) {
                    if (pagerState.currentPage >= pages.size) {
                        pagerState.scrollToPage(0)
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { index ->
                    when (pages[index]) {
                        HomeTopPagerPage.Shop -> {
                            val s = shop!!
                            ShopHomeCard(
                                shop = s,
                                brandBlue = brandBlue,
                                textMuted = textMuted,
                                textDark = textDark,
                                onCardClick = onShopCardClick,
                                onShareClick = { onShareDetails(shopDetailsShareText(s)) },
                            )
                        }
                        HomeTopPagerPage.Service -> {
                            val p = serviceProfile!!
                            ServiceHomeCard(
                                profile = p,
                                servicePosts = servicePosts,
                                brandBlue = brandBlue,
                                textMuted = textMuted,
                                textDark = textDark,
                                onCardClick = onServiceCardClick,
                                onShareClick = { onShareDetails(serviceProfileShareText(p)) },
                            )
                        }
                        HomeTopPagerPage.Reservation -> {
                            val b = reservationBusiness!!
                            ReservationHomeCard(
                                business = b,
                                brandBlue = brandBlue,
                                textMuted = textMuted,
                                textDark = textDark,
                                onCardClick = onReservationCardClick,
                                onShareClick = { onShareDetails(reservationBusinessShareText(b)) },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(pages.size) { i ->
                        val selected = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (selected) 8.dp else 6.dp)
                                .background(
                                    color = if (selected) brandBlue else textMuted.copy(alpha = 0.35f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
private fun ShopHomeCard(
    shop: ShopDetails,
    brandBlue: Color,
    textMuted: Color,
    textDark: Color,
    onCardClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
            ) {
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = shop.shopName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IdBadge(text = shop.vendorId, brandBlue = brandBlue)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = shop.ownerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = listOf(shop.address, shop.city, shop.postalCode)
                        .filter { it.isNotBlank() }
                        .joinToString(", "),
                    fontSize = 13.sp,
                    color = textMuted,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OpenClosedStatusChip(isOpen = shop.isOpen)
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = textMuted,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onShareClick() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceHomeCard(
    profile: VendorServiceProfile,
    servicePosts: List<VendorServicePost>,
    brandBlue: Color,
    textMuted: Color,
    textDark: Color,
    onCardClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CardIconBox(icon = Icons.Outlined.HomeRepairService, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = profile.providerName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IdBadge(text = profile.id, brandBlue = brandBlue)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.profession.displayLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val activePosts = servicePosts.filter { it.isActive }
                val listingsLine = when {
                    servicePosts.isEmpty() ->
                        "No listings yet · tap to add services"
                    activePosts.isEmpty() ->
                        "No active listings (${servicePosts.size} paused) · tap to manage"
                    else -> {
                        val head = activePosts.take(2).joinToString(" · ") { it.title }
                        val more = activePosts.size - 2
                        if (more > 0) "$head · +$more more" else head
                    }
                }
                Text(
                    text = listingsLine,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = brandBlue,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOf(profile.serviceArea, profile.contactNumber)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    fontSize = 13.sp,
                    color = textMuted,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvailableStatusChip(isAvailable = profile.isAvailable)
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = textMuted,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onShareClick() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationHomeCard(
    business: ReservationBusiness,
    brandBlue: Color,
    textMuted: Color,
    textDark: Color,
    onCardClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CardIconBox(icon = Icons.Outlined.CalendarMonth, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = business.businessName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IdBadge(text = business.id, brandBlue = brandBlue)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = business.businessType.displayLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOf(business.address, business.city, business.postalCode)
                        .filter { it.isNotBlank() }
                        .joinToString(", "),
                    fontSize = 13.sp,
                    color = textMuted,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OpenClosedStatusChip(isOpen = business.isOpen)
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = textMuted,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onShareClick() },
                    )
                }
            }
        }
    }
}

@Composable
private fun CardIconBox(icon: ImageVector, contentDescription: String?) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF6FF),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFF155AC1),
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun IdBadge(text: String, brandBlue: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEFF6FF),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = brandBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun OpenClosedStatusChip(isOpen: Boolean) {
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
private fun AvailableStatusChip(isAvailable: Boolean) {
    val bg = if (isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val fg = if (isAvailable) Color(0xFF166534) else Color(0xFF991B1B)
    val label = if (isAvailable) "Available" else "Busy"

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

@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(
        shop = ShopDetails(
            shopName = "FreshMart Provisions",
            vendorId = "VND-88421",
            ownerName = "Ahmed Khan",
            address = "12 Market Street",
            city = "Karachi",
            postalCode = "75500",
            isOpen = true,
        ),
        serviceProfile = null,
        reservationBusiness = null,
        locationHeadline = "Karachi",
        locationSubtitle = "12 Market Street",
        profileInitial = "A",
    )
}
