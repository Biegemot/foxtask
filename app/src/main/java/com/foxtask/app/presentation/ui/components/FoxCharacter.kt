package com.foxtask.app.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.foxtask.app.R
import com.foxtask.app.data.local.entities.Item
import com.foxtask.app.data.models.FoxOutfit
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.presentation.ui.theme.*

@Composable
fun FoxCharacter(
    outfit: FoxOutfit,
    level: Int,
    itemsMap: Map<Int, Item> = emptyMap(),
    modifier: Modifier = Modifier,
    animationProgress: Float = 1f
) {
    val context = LocalContext.current
    val size = 200.dp
    val baseScale = 0.8f + (level * 0.02f).coerceAtMost(0.3f)
    
    // Cache for drawable resource IDs to avoid repeated getIdentifier calls
    val drawableCache = remember { mutableMapOf<Int, Int>() }
    
    fun getDrawableId(item: Item): Int {
        return drawableCache.getOrPut(item.id) {
            context.resources.getIdentifier(item.drawableResName, "drawable", context.packageName)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Базовый лис
        Canvas(modifier = Modifier.matchParentSize()) {
            val scale = baseScale * animationProgress
            val centerX = this.size.width / 2
            val centerY = this.size.height / 2 + 20.dp.toPx()

            withTransform({
                translate(centerX, centerY)
                scale(scale, scale)
                translate(-centerX, -centerY)
            }) {
                drawFoxBase()
            }
        }

        // Слои аксессуаров
        outfit.hatItemId?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size * 0.8f)
                            .align(Alignment.TopCenter)
                            .offset(y = (-20).dp)
                    )
                }
            }
        }

        // Glasses/Mask
        val faceSlot = if (outfit.glassesItemId != null) outfit.glassesItemId else outfit.maskItemId
        faceSlot?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size * 0.7f)
                            .align(Alignment.Center)
                            .offset(y = (-5).dp)
                    )
                }
            }
        }

        // Cloak
        outfit.cloakItemId?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size * 1.1f)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        // Scarf/Bandana
        val neckSlot = if (outfit.scarfItemId != null) outfit.scarfItemId else outfit.bandanaItemId
        neckSlot?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size * 0.7f)
                            .align(Alignment.Center)
                            .offset(y = 25.dp)
                    )
                }
            }
        }

        // Fur color overlay (simplified: we just change base fox color, no overlay layer currently)
        // In future, could draw a semi-transparent colored rectangle over fox.

        // Background theme
        outfit.backgroundThemeId?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }

        // Maori pattern
        outfit.maoriPatternItemId?.let { itemId ->
            val item = itemsMap[itemId]
            item?.let {
                val drawableId = getDrawableId(it)
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size * 0.5f)
                            .align(Alignment.TopCenter)
                            .offset(y = 30.dp)
                    )
                }
            }
        }
    }
}

fun DrawScope.drawFoxBase() {
    val cx = size.width / 2
    val cy = size.height / 2

    // Body (ellipse)
    drawOval(
        color = FoxBaseOrange,
        topLeft = Offset(cx - 60f, cy + 20f),
        size = Size(120f, 60f)
    )

    // Head (circle)
    drawCircle(
        color = FoxBaseOrange,
        radius = 45f,
        center = Offset(cx, cy - 20f)
    )

    // Ears (triangles)
    val earPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx - 35f, cy - 50f)
        lineTo(cx - 55f, cy - 90f)
        lineTo(cx - 15f, cy - 65f)
        close()
    }
    drawPath(
        path = earPath,
        color = FoxBaseOrange
    )

    val earPath2 = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx + 35f, cy - 50f)
        lineTo(cx + 55f, cy - 90f)
        lineTo(cx + 15f, cy - 65f)
        close()
    }
    drawPath(
        path = earPath2,
        color = FoxBaseOrange
    )

    // Eyes
    drawCircle(
        color = Color.Black,
        radius = 6f,
        center = Offset(cx - 15f, cy - 25f)
    )
    drawCircle(
        color = Color.Black,
        radius = 6f,
        center = Offset(cx + 15f, cy - 25f)
    )

    // Nose
    drawCircle(
        color = Color.Black,
        radius = 6f,
        center = Offset(cx, cy - 5f)
    )

    // Nose highlight (white)
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = Offset(cx - 2f, cy - 7f)
    )
}
