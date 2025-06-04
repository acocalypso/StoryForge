package de.astronarren.storyforge.ui.screens.timeline.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.database.entities.Book
import kotlinx.coroutines.Job

@Composable
fun DraggableTimelineList(
    events: List<TimelineEvent>,
    characters: List<Character> = emptyList(),
    scenes: List<Scene> = emptyList(),
    currentBook: Book? = null,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onEditEvent: (TimelineEvent) -> Unit,
    onDeleteEvent: (TimelineEvent) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var draggedEvent by remember { mutableStateOf<TimelineEvent?>(null) }
    var draggedIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    
    // Animation for drag
    val draggedItemScale by animateFloatAsState(
        targetValue = if (draggedEvent != null) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "draggedItemScale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = events,
                key = { event -> event.id }
            ) { event ->                val isDragged = draggedEvent?.id == event.id
                val currentIndex = events.indexOf(event)
                
                TimelineEventCard(
                    event = event,
                    characters = characters,
                    scenes = scenes,
                    currentBook = currentBook,
                    onEdit = { onEditEvent(event) },
                    onDelete = { onDeleteEvent(event) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = if (isDragged) draggedItemScale else 1f,
                            scaleY = if (isDragged) draggedItemScale else 1f,
                            translationY = if (isDragged) dragOffset.y else 0f
                        )
                        .shadow(
                            elevation = if (isDragged) 8.dp else 0.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .zIndex(if (isDragged) 1f else 0f)
                        .onGloballyPositioned { coordinates ->
                            if (itemHeight == 0.dp) {
                                itemHeight = with(density) { coordinates.size.height.toDp() }
                            }
                        }                        .pointerInput(event.id) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    draggedEvent = event
                                    draggedIndex = currentIndex
                                    dragOffset = Offset.Zero
                                },
                                onDragEnd = {
                                    draggedEvent?.let { draggedItem ->
                                        val fromIndex = events.indexOf(draggedItem)
                                        val toIndex = calculateDropIndex(
                                            dragOffset.y,
                                            itemHeight,
                                            density,
                                            fromIndex,
                                            events.size
                                        )
                                        
                                        if (fromIndex != toIndex && toIndex in events.indices) {
                                            onReorder(fromIndex, toIndex)
                                        }
                                    }
                                    
                                    draggedEvent = null
                                    draggedIndex = -1
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { change, dragAmount ->
                                    if (draggedEvent?.id == event.id) {
                                        dragOffset += dragAmount
                                    }
                                }
                            )
                        }
                )
            }
        }
        
        // Drag indicator/helper
        if (draggedEvent != null) {
            DragIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun DragIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            2.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}

private fun calculateDropIndex(
    dragOffset: Float,
    itemHeight: androidx.compose.ui.unit.Dp,
    density: androidx.compose.ui.unit.Density,
    currentIndex: Int,
    totalItems: Int
): Int {
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val offsetInItems = (dragOffset / itemHeightPx).toInt()
    val newIndex = currentIndex + offsetInItems
    
    return newIndex.coerceIn(0, totalItems - 1)
}
