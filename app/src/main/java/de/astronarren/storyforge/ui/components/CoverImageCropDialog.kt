package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlin.math.max
import kotlin.math.min

@Composable
fun CoverImageCropDialog(
    imageUri: String,
    onCropConfirmed: (Rect) -> Unit,
    onDismiss: () -> Unit
) {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Crop Cover Image",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Drag to adjust the crop area for your book cover",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Image with crop overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.67f) // Standard book cover ratio
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Cover image to crop",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = { state ->
                            imageSize = Size(
                                state.painter.intrinsicSize.width,
                                state.painter.intrinsicSize.height
                            )
                            // Initialize crop rect to center square
                            val minDimension = min(imageSize.width, imageSize.height)
                            val offsetX = (imageSize.width - minDimension) / 2
                            val offsetY = (imageSize.height - minDimension) / 2
                            cropRect = Rect(
                                offset = Offset(offsetX, offsetY),
                                size = Size(minDimension, minDimension)
                            )
                        }
                    )
                    
                    // Crop overlay
                    if (cropRect != Rect.Zero) {
                        CropOverlay(
                            cropRect = cropRect,
                            imageSize = imageSize,
                            onCropRectChanged = { newRect -> cropRect = newRect },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onCropConfirmed(cropRect) },
                        modifier = Modifier.weight(1f),
                        enabled = cropRect != Rect.Zero
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun CropOverlay(
    cropRect: Rect,
    imageSize: Size,
    onCropRectChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                val newRect = Rect(
                    offset = Offset(
                        x = max(0f, min(imageSize.width - cropRect.width, cropRect.left + dragAmount.x)),
                        y = max(0f, min(imageSize.height - cropRect.height, cropRect.top + dragAmount.y))
                    ),
                    size = cropRect.size
                )
                onCropRectChanged(newRect)
            }
        }
    ) {
        drawCropOverlay(cropRect, imageSize, size)
    }
}

private fun DrawScope.drawCropOverlay(
    cropRect: Rect,
    imageSize: Size,
    canvasSize: Size
) {
    val scaleX = canvasSize.width / imageSize.width
    val scaleY = canvasSize.height / imageSize.height
    val scale = min(scaleX, scaleY)
    
    val scaledCropRect = Rect(
        offset = Offset(
            cropRect.left * scale,
            cropRect.top * scale
        ),
        size = Size(
            cropRect.width * scale,
            cropRect.height * scale
        )
    )
    
    // Draw semi-transparent overlay outside crop area
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        size = canvasSize
    )
    
    // Clear the crop area
    drawRect(
        color = Color.Transparent,
        topLeft = scaledCropRect.topLeft,
        size = scaledCropRect.size
    )
    
    // Draw crop border
    drawRect(
        color = Color.White,
        topLeft = scaledCropRect.topLeft,
        size = scaledCropRect.size,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Draw corner handles
    val handleSize = 12.dp.toPx()
    val corners = listOf(
        scaledCropRect.topLeft,
        Offset(scaledCropRect.right - handleSize, scaledCropRect.top),
        Offset(scaledCropRect.left, scaledCropRect.bottom - handleSize),
        Offset(scaledCropRect.right - handleSize, scaledCropRect.bottom - handleSize)
    )
    
    corners.forEach { corner ->
        drawRect(
            color = Color.White,
            topLeft = corner,
            size = Size(handleSize, handleSize)
        )
    }
}

