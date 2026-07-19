package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Create a custom generic shape that looks like the modern hexagonal speech bubble in the image!
val HexagonBubbleShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    
    // Draw a rounded hexagon with a speech bubble pointer at the bottom left
    moveTo(w * 0.3f, 0f)
    lineTo(w * 0.7f, 0f)
    lineTo(w, h * 0.3f)
    lineTo(w, h * 0.7f)
    lineTo(w * 0.7f, h)
    
    // Speech bubble tail/pointer at the bottom left
    lineTo(w * 0.45f, h)
    lineTo(w * 0.2f, h * 1.15f) // tail tip
    lineTo(w * 0.25f, h * 0.85f)
    
    lineTo(w * 0.3f, h * 0.85f)
    lineTo(0f, h * 0.7f)
    lineTo(0f, h * 0.3f)
    close()
}

@Composable
fun DecisaoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8A30FF), // Neon Purple
                        Color(0xFF5311FD)  // Indigo/Deep Blue
                    )
                ),
                shape = HexagonBubbleShape
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        // Inner cutout to match the icon exactly (it has an inner lighter/white circular bubble cutout or transparent center)
        Box(
            modifier = Modifier
                .fillMaxSize(0.42f)
                .background(Color.White, shape = GenericShape { size, _ ->
                    val w = size.width
                    val h = size.height
                    // Mini conversation bubble inside
                    moveTo(w * 0.2f, 0f)
                    lineTo(w * 0.8f, 0f)
                    lineTo(w, h * 0.35f)
                    lineTo(w, h * 0.75f)
                    lineTo(w * 0.8f, h)
                    lineTo(w * 0.4f, h)
                    lineTo(w * 0.15f, h * 1.18f)
                    lineTo(w * 0.25f, h * 0.82f)
                    lineTo(0f, h * 0.75f)
                    lineTo(0f, h * 0.35f)
                    close()
                })
        )
    }
}

@Composable
fun DecisaoHeaderLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    DecisaoLogo(
        modifier = modifier,
        size = size
    )
}
