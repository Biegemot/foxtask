package com.foxtask.app.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.foxtask.app.data.models.ItemTier
import com.foxtask.app.presentation.ui.theme.*

@Composable
fun ItemCard(
    name: String,
    description: String,
    cost: Int,
    tier: ItemTier,
    isOwned: Boolean,
    isEquipped: Boolean,
    onBuyClick: () -> Unit,
    onEquipClick: () -> Unit,
    itemDrawableResName: String? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val borderColor = when (tier) {
        ItemTier.COMMON -> TierCommon
        ItemTier.RARE -> TierRare
        ItemTier.EPIC -> TierEpic
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (isOwned && !isEquipped) {
                    onEquipClick()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Preview area with item image
            val context = LocalContext.current
            val drawableId = remember(itemDrawableResName) {
                itemDrawableResName?.let {
                    context.resources.getIdentifier(it, "drawable", context.packageName)
                } ?: 0
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (drawableId != 0 && itemDrawableResName != null) {
                    // Show actual item drawable
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = name,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    // Fallback to tier-based icon
                    Icon(
                        imageVector = when (tier) {
                            ItemTier.COMMON -> Icons.Filled.Star
                            ItemTier.RARE -> Icons.Filled.Diamond
                            ItemTier.EPIC -> Icons.Filled.Whatshot
                        },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = borderColor
                    )
                }
                
                // Premium badge for EPIC items
                if (tier == ItemTier.EPIC) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "P",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = borderColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = when (tier) {
                            ItemTier.COMMON -> "Обычный"
                            ItemTier.RARE -> "Редкий"
                            ItemTier.EPIC -> "Эпический"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor
                    )
                }
            }

            // Description
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cost and action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cost.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isEquipped) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Надет",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else if (isOwned) {
                    OutlinedButton(
                        onClick = onEquipClick,
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Экипировать", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onBuyClick,
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Купить", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
