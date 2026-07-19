package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    hasStoryBorder: Boolean = false,
    showOnlineStatus: Boolean = false
) {
    // Generate beautiful consistent background colors based on name
    val colorScheme = when (name.lowercase().trim()) {
        "marina", "marina souza" -> Pair(Color(0xFFE040FB), Color(0xFF6F3FF5)) // Purple-Magenta
        "beatriz", "beatriz lima" -> Pair(Color(0xFFFF4081), Color(0xFFFF80AB)) // Soft Pink
        "lucas", "lucas martins" -> Pair(Color(0xFF00E5FF), Color(0xFF2979FF)) // Blue-Teal
        "ana clara", "ana" -> Pair(Color(0xFFFF9100), Color(0xFFFF3D00)) // Coral-Orange
        "carlos", "carlos mendes" -> Pair(Color(0xFF00E676), Color(0xFF00B0FF)) // Green-Blue
        "rafael", "rafael oliveira" -> Pair(Color(0xFFE040FB), Color(0xFF00E5FF)) // Violet-Cyan
        "juliana", "juliana costa" -> Pair(Color(0xFFFFD600), Color(0xFFFF1744)) // Yellow-Red
        "time", "time decisões" -> Pair(Color(0xFF6F3FF5), Color(0xFF00E5FF)) // Team gradient
        else -> Pair(Color(0xFF7E57C2), Color(0xFFB39DDB)) // Default lavender
    }

    val nameLower = name.lowercase().trim()
    val isGroup = nameLower.contains("time") || 
                  nameLower.contains("grupo") || 
                  nameLower.contains("debates") || 
                  nameLower.contains("comunidade")

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer gradient ring for active stories
        if (hasStoryBorder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE040FB), // Magenta
                                Color(0xFF6F3FF5), // Deep Violet
                                Color(0xFF00E5FF)  // Cyan
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp)
            )
        }

        // Main circular avatar
        Box(
            modifier = Modifier
                .fillMaxSize(if (hasStoryBorder) 0.88f else 1f)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.first, colorScheme.second)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Draw a subtle modern geometric overlay design inside the circle to make it look extremely premium
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = w * 0.45f,
                    center = center.copy(x = center.x - w * 0.2f, y = center.y + w * 0.2f)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.08f),
                    radius = w * 0.35f,
                    center = center.copy(x = center.x + w * 0.3f, y = center.y - w * 0.2f)
                )
            }

            // Draw the classic network profile silhouette boneco(s)
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = this.size.width
                val h = this.size.height

                if (isGroup) {
                    // Draw Group / Multi-User silhouettes (two overlapping bonecos)
                    
                    // 1. Back-Left Silhouette (slightly smaller and semi-transparent)
                    val backHeadRadius = w * 0.13f
                    val backHeadCenterX = w * 0.38f
                    val backHeadCenterY = h * 0.44f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = backHeadRadius,
                        center = androidx.compose.ui.geometry.Offset(backHeadCenterX, backHeadCenterY)
                    )

                    val backBodyPath = androidx.compose.ui.graphics.Path().apply {
                        val startX = w * 0.08f
                        val endX = w * 0.68f
                        val bodyTopY = h * 0.72f
                        val bodyBottomY = h * 1.05f
                        moveTo(startX, bodyBottomY)
                        cubicTo(
                            startX, bodyTopY + h * 0.05f,
                            w * 0.22f, bodyTopY,
                            w * 0.38f, bodyTopY
                        )
                        cubicTo(
                            w * 0.54f, bodyTopY,
                            endX, bodyTopY + h * 0.05f,
                            endX, bodyBottomY
                        )
                        close()
                    }
                    drawPath(path = backBodyPath, color = Color.White.copy(alpha = 0.7f))

                    // 2. Front-Right Silhouette (larger and fully opaque)
                    val frontHeadRadius = frontHeadRadiusFront ?: (w * 0.16f)
                    val frontHeadCenterX = w * 0.62f
                    val frontHeadCenterY = h * 0.38f
                    drawCircle(
                        color = Color.White,
                        radius = frontHeadRadius,
                        center = androidx.compose.ui.geometry.Offset(frontHeadCenterX, frontHeadCenterY)
                    )

                    val frontBodyPath = androidx.compose.ui.graphics.Path().apply {
                        val startX = w * 0.28f
                        val endX = w * 0.92f
                        val bodyTopY = h * 0.66f
                        val bodyBottomY = h * 1.05f
                        moveTo(startX, bodyBottomY)
                        cubicTo(
                            startX, bodyTopY + h * 0.05f,
                            w * 0.45f, bodyTopY,
                            frontHeadCenterX, bodyTopY
                        )
                        cubicTo(
                            w * 0.79f, bodyTopY,
                            endX, bodyTopY + h * 0.05f,
                            endX, bodyBottomY
                        )
                        close()
                    }
                    drawPath(path = frontBodyPath, color = Color.White)
                } else {
                    // Draw Single Silhouette (the classic "boneco das redes")
                    val headRadius = w * 0.18f
                    val headCenterX = w * 0.5f
                    val headCenterY = h * 0.36f
                    drawCircle(
                        color = Color.White,
                        radius = headRadius,
                        center = androidx.compose.ui.geometry.Offset(headCenterX, headCenterY)
                    )

                    val bodyPath = androidx.compose.ui.graphics.Path().apply {
                        val startX = w * 0.15f
                        val endX = w * 0.85f
                        val bodyTopY = h * 0.66f
                        val bodyBottomY = h * 1.05f
                        moveTo(startX, bodyBottomY)
                        cubicTo(
                            startX, bodyTopY + h * 0.05f,
                            w * 0.3f, bodyTopY,
                            w * 0.5f, bodyTopY
                        )
                        cubicTo(
                            w * 0.7f, bodyTopY,
                            endX, bodyTopY + h * 0.05f,
                            endX, bodyBottomY
                        )
                        close()
                    }
                    drawPath(path = bodyPath, color = Color.White)
                }
            }
        }

        // Online green indicator dot
        if (showOnlineStatus) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
            )
        }
    }
}

// Helper value for clean sizing references
private val frontHeadRadiusFront: Float? = null

@Composable
fun VerificationBadge(modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Verificado",
        tint = Color(0xFF6F3FF5),
        modifier = modifier.size(size)
    )
}
