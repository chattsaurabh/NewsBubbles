package com.mobiwiki.news.bubbles.ui.bubbles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mobiwiki.news.bubbles.model.NewsCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.isActive

@Composable
fun BubblesScreen(
    onCategoryClick: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val physicsBubbles = remember(containerSize, density.density) {
        if (containerSize.width > 0 && containerSize.height > 0) {
            createInitialBubbles(
                width = containerSize.width.toFloat(),
                height = containerSize.height.toFloat(),
                density = density.density
            )
        } else {
            mutableListOf()
        }
    }
    var renderedBubbles by remember(physicsBubbles) {
        mutableStateOf(physicsBubbles.map(BubbleState::toRenderState))
    }

    LaunchedEffect(containerSize, density.density) {
        if (physicsBubbles.isEmpty()) return@LaunchedEffect

        var previousFrameNanos = withFrameNanos { it }
        while (isActive) {
            val frameNanos = withFrameNanos { it }
            val deltaTimeSeconds = (frameNanos - previousFrameNanos) / NANOS_PER_SECOND

            BubblePhysics.step(
                bubbles = physicsBubbles,
                width = containerSize.width.toFloat(),
                height = containerSize.height.toFloat(),
                deltaTimeSeconds = deltaTimeSeconds
            )
            renderedBubbles = physicsBubbles.map(BubbleState::toRenderState)
            previousFrameNanos = frameNanos
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it }
        ) {
            renderedBubbles.forEach { bubble ->
                CategoryBubble(
                    category = bubble.category,
                    radiusPx = bubble.radius,
                    centerX = bubble.x,
                    centerY = bubble.y,
                    onClick = { onCategoryClick(bubble.category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryBubble(
    category: NewsCategory,
    radiusPx: Float,
    centerX: Float,
    centerY: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val diameter = remember(radiusPx, density.density) {
        with(density) { (radiusPx * 2f).toDp() }
    }
    val colorScheme = MaterialTheme.colorScheme
    val colors = remember(category, colorScheme) {
        bubbleColors(category, colorScheme)
    }
    val icon = remember(category) { category.icon() }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(diameter)
            .graphicsLayer {
                translationX = centerX - radiusPx
                translationY = centerY - radiusPx
            }
            .clearAndSetSemantics {
                contentDescription = "Open ${category.displayName} news"
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            },
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun bubbleColors(
    category: NewsCategory,
    colorScheme: ColorScheme
): BubbleColors = when (category) {
        NewsCategory.BUSINESS -> BubbleColors(colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
        NewsCategory.ENTERTAINMENT -> BubbleColors(colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        NewsCategory.GENERAL -> BubbleColors(colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        NewsCategory.HEALTH -> BubbleColors(colorScheme.errorContainer, colorScheme.onErrorContainer)
        NewsCategory.SCIENCE -> BubbleColors(colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)
        NewsCategory.SPORTS -> BubbleColors(colorScheme.primary, colorScheme.onPrimary)
        NewsCategory.TECHNOLOGY -> BubbleColors(colorScheme.secondary, colorScheme.onSecondary)
}

private fun NewsCategory.icon(): ImageVector = when (this) {
    NewsCategory.BUSINESS -> Icons.Default.BusinessCenter
    NewsCategory.ENTERTAINMENT -> Icons.Default.Movie
    NewsCategory.GENERAL -> Icons.Default.Public
    NewsCategory.HEALTH -> Icons.Default.Favorite
    NewsCategory.SCIENCE -> Icons.Default.Science
    NewsCategory.SPORTS -> Icons.Default.SportsSoccer
    NewsCategory.TECHNOLOGY -> Icons.Default.Memory
}

private fun createInitialBubbles(
    width: Float,
    height: Float,
    density: Float
): MutableList<BubbleState> {
    val sizeScale = minOf(1f, minOf(width, height) / (REFERENCE_MIN_SIZE_DP * density))
    val radii = BASE_RADII_DP.map { it * density * sizeScale }
    val velocities = createVelocities(density)
    val random = Random(PLACEMENT_SEED)
    val bubbles = mutableListOf<BubbleState>()
    val spacing = PLACEMENT_SPACING_DP * density * sizeScale

    NewsCategory.entries.forEachIndexed { index, category ->
        val radius = radii[index]
        var position: BubblePosition? = null

        for (attempt in 0 until MAX_PLACEMENT_ATTEMPTS) {
            val candidate = BubblePosition(
                x = random.nextFloatIn(radius, width - radius),
                y = random.nextFloatIn(radius, height - radius)
            )
            if (bubbles.none { it.overlaps(candidate, radius, spacing) }) {
                position = candidate
                break
            }
        }

        val acceptedPosition = position ?: return createGridFallback(
            width = width,
            height = height,
            radii = radii,
            velocities = velocities
        )
        val velocity = velocities[index]
        bubbles += BubbleState(
            category = category,
            x = acceptedPosition.x,
            y = acceptedPosition.y,
            radius = radius,
            velocityX = velocity.x,
            velocityY = velocity.y
        )
    }

    return bubbles
}

private fun createGridFallback(
    width: Float,
    height: Float,
    radii: List<Float>,
    velocities: List<BubblePosition>
): MutableList<BubbleState> = NewsCategory.entries.mapIndexed { index, category ->
    val cell = FALLBACK_CELLS[index]
    val velocity = velocities[index]
    BubbleState(
        category = category,
        x = width * (cell.first + 0.5f) / 3f,
        y = height * (cell.second + 0.5f) / 3f,
        radius = radii[index],
        velocityX = velocity.x,
        velocityY = velocity.y
    )
}.toMutableList()

private fun createVelocities(density: Float): List<BubblePosition> =
    NewsCategory.entries.mapIndexed { index, _ ->
        val angle = INITIAL_ANGLES_DEGREES[index] * PI.toFloat() / 180f
        val speed = INITIAL_SPEEDS_DP_PER_SECOND[index] * density
        BubblePosition(
            x = cos(angle) * speed,
            y = sin(angle) * speed
        )
    }

private fun Random.nextFloatIn(minimum: Float, maximum: Float): Float {
    if (maximum <= minimum) return (minimum + maximum) / 2f
    return minimum + nextFloat() * (maximum - minimum)
}

private fun BubbleState.overlaps(
    candidate: BubblePosition,
    candidateRadius: Float,
    spacing: Float
): Boolean {
    val deltaX = candidate.x - x
    val deltaY = candidate.y - y
    val minimumDistance = radius + candidateRadius + spacing
    return deltaX * deltaX + deltaY * deltaY < minimumDistance * minimumDistance
}

private fun BubbleState.toRenderState() = BubbleRenderState(
    category = category,
    x = x,
    y = y,
    radius = radius
)

private data class BubbleRenderState(
    val category: NewsCategory,
    val x: Float,
    val y: Float,
    val radius: Float
)

private data class BubblePosition(
    val x: Float,
    val y: Float
)

private data class BubbleColors(
    val container: Color,
    val content: Color
)

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val REFERENCE_MIN_SIZE_DP = 320f
private const val PLACEMENT_SPACING_DP = 4f
private const val MAX_PLACEMENT_ATTEMPTS = 250
private const val PLACEMENT_SEED = 7

private val BASE_RADII_DP = listOf(46f, 49f, 43f, 44f, 47f, 45f, 48f)
private val INITIAL_SPEEDS_DP_PER_SECOND = listOf(58f, 72f, 64f, 80f, 68f, 76f, 61f)
private val INITIAL_ANGLES_DEGREES = listOf(28f, 146f, 215f, 322f, 78f, 252f, 188f)
private val FALLBACK_CELLS = listOf(
    0 to 0,
    2 to 0,
    1 to 1,
    0 to 2,
    2 to 2,
    0 to 1,
    2 to 1
)
