package com.foxtask.app.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.foxtask.app.data.local.entities.Task
import com.foxtask.app.data.models.TaskPriority
import com.foxtask.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by remember(task.id) { mutableStateOf(task.isCompleted) }

    // Sync with task.isCompleted when it changes
    LaunchedEffect(task.isCompleted) {
        checked = task.isCompleted
    }

    val alpha by animateFloatAsState(
        targetValue = if (checked) 0.5f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                    else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "backgroundColor"
    )
    
    val titleColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                    else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "titleColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { newValue ->
                    checked = newValue
                    onCheckChange(newValue)
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    fontWeight = if (checked) FontWeight.Normal else FontWeight.Bold,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                )
                task.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority chip
                    AssistChip(
                        onClick = {},
                        label = { Text(getPriorityLabel(task.priority)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = getPriorityColor(task.priority).copy(alpha = 0.2f)
                        )
                    )
                    // Reward chip
                    AssistChip(
                        onClick = {},
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+${task.xpReward} XP")
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = XpProgress.copy(alpha = 0.2f)
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+${task.coinReward}")
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = OrangePrimary.copy(alpha = 0.2f)
                        )
                    )
                }
                if (task.isHabit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Стрик: ${task.streak} дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getPriorityLabel(priority: Int): String {
    return when (priority) {
        1 -> "Минимальный"
        2 -> "Низкий"
        3 -> "Средний"
        4 -> "Высокий"
        5 -> "Критический"
        else -> "Приоритет"
    }
}

@Composable
private fun getPriorityColor(priority: Int): Color {
    return when (priority) {
        1 -> PriorityMinimal
        2 -> PriorityLow
        3 -> PriorityMedium
        4 -> PriorityHigh
        5 -> PriorityCritical
        else -> MaterialTheme.colorScheme.onSurface
    }
}
