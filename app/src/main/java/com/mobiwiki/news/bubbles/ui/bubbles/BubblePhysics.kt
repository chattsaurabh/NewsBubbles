package com.mobiwiki.news.bubbles.ui.bubbles

import com.mobiwiki.news.bubbles.model.NewsCategory
import kotlin.math.sqrt

data class BubbleState(
    val category: NewsCategory,
    var x: Float,
    var y: Float,
    val radius: Float,
    var velocityX: Float,
    var velocityY: Float
)

object BubblePhysics {

    private const val MAX_DELTA_TIME_SECONDS = 0.05f
    private const val DISTANCE_EPSILON = 0.0001f

    fun step(
        bubbles: MutableList<BubbleState>,
        width: Float,
        height: Float,
        deltaTimeSeconds: Float
    ) {
        if (!width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return
        if (!deltaTimeSeconds.isFinite() || deltaTimeSeconds <= 0f) return

        val deltaTime = deltaTimeSeconds.coerceAtMost(MAX_DELTA_TIME_SECONDS)

        bubbles.forEach { bubble ->
            bubble.x += bubble.velocityX * deltaTime
            bubble.y += bubble.velocityY * deltaTime
            resolveWallCollisions(bubble, width, height)
        }

        for (firstIndex in 0 until bubbles.lastIndex) {
            for (secondIndex in firstIndex + 1 until bubbles.size) {
                resolveBubbleCollision(bubbles[firstIndex], bubbles[secondIndex])
            }
        }

        bubbles.forEach { bubble ->
            resolveWallCollisions(bubble, width, height)
        }
    }

    private fun resolveWallCollisions(
        bubble: BubbleState,
        width: Float,
        height: Float
    ) {
        if (bubble.radius * 2f >= width) {
            bubble.x = width / 2f
            bubble.velocityX = 0f
        } else {
            val minimumX = bubble.radius
            val maximumX = width - bubble.radius

            if (bubble.x < minimumX) {
                bubble.x = minimumX
                if (bubble.velocityX < 0f) bubble.velocityX = -bubble.velocityX
            } else if (bubble.x > maximumX) {
                bubble.x = maximumX
                if (bubble.velocityX > 0f) bubble.velocityX = -bubble.velocityX
            }
        }

        if (bubble.radius * 2f >= height) {
            bubble.y = height / 2f
            bubble.velocityY = 0f
        } else {
            val minimumY = bubble.radius
            val maximumY = height - bubble.radius

            if (bubble.y < minimumY) {
                bubble.y = minimumY
                if (bubble.velocityY < 0f) bubble.velocityY = -bubble.velocityY
            } else if (bubble.y > maximumY) {
                bubble.y = maximumY
                if (bubble.velocityY > 0f) bubble.velocityY = -bubble.velocityY
            }
        }
    }

    private fun resolveBubbleCollision(first: BubbleState, second: BubbleState) {
        val deltaX = second.x - first.x
        val deltaY = second.y - first.y
        val minimumDistance = first.radius + second.radius
        val distanceSquared = deltaX * deltaX + deltaY * deltaY

        if (!distanceSquared.isFinite() || minimumDistance <= 0f) return
        if (distanceSquared >= minimumDistance * minimumDistance) return

        val normalX: Float
        val normalY: Float
        val distance: Float

        if (distanceSquared <= DISTANCE_EPSILON * DISTANCE_EPSILON) {
            normalX = 1f
            normalY = 0f
            distance = 0f
        } else {
            distance = sqrt(distanceSquared)
            normalX = deltaX / distance
            normalY = deltaY / distance
        }

        val overlap = minimumDistance - distance
        val correctionX = normalX * overlap / 2f
        val correctionY = normalY * overlap / 2f
        first.x -= correctionX
        first.y -= correctionY
        second.x += correctionX
        second.y += correctionY

        val relativeVelocityX = second.velocityX - first.velocityX
        val relativeVelocityY = second.velocityY - first.velocityY
        val relativeSpeedAlongNormal =
            relativeVelocityX * normalX + relativeVelocityY * normalY

        if (relativeSpeedAlongNormal >= 0f) return

        first.velocityX += relativeSpeedAlongNormal * normalX
        first.velocityY += relativeSpeedAlongNormal * normalY
        second.velocityX -= relativeSpeedAlongNormal * normalX
        second.velocityY -= relativeSpeedAlongNormal * normalY
    }
}
