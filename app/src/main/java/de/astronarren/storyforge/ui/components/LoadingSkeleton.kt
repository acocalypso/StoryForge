package de.astronarren.storyforge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    CompositionLocalProvider(
        LocalShimmerBrush provides brush
    ) {
        content()
    }
}

val LocalShimmerBrush = compositionLocalOf<Brush> { 
    Brush.linearGradient(listOf(Color.Gray, Color.LightGray, Color.Gray))
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = LocalShimmerBrush.current,
                shape = RoundedCornerShape(4.dp)
            )
    )
}

@Composable
fun BookCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title skeleton
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Author skeleton
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                    )
                }
                
                // Menu icon skeleton
                ShimmerBox(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description skeleton
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date skeleton
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                )
                
                // Edit button skeleton
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

@Composable
fun BookListSkeleton(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    ShimmerEffect {
        Column(modifier = modifier) {
            repeat(itemCount) {
                BookCardSkeleton()
                if (it < itemCount - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier
) {
    ShimmerEffect {
        ShimmerBox(modifier = modifier)
    }
}

@Composable
fun CharacterCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character avatar skeleton
            ShimmerBox(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Character name skeleton
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Character role skeleton
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                )
            }
            
            // Menu icon skeleton
            ShimmerBox(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

