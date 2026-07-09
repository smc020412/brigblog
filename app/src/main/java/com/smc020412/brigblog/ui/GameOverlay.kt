package com.smc020412.brigblog.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PausePanel(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    BoardPanel {
        Text("Paused", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Resume, restart, or leave.", style = MaterialTheme.typography.bodySmall, color = GameColors.MutedText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = onResume,
                modifier = Modifier.weight(1.25f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text(
                    text = "Resume",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            TextButton(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text(
                    text = "Restart",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
        TextButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) {
            Text("Main Menu", style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun GameOverPanel(
    score: Int,
    rank: Int?,
    scores: List<Int>,
    title: String = "Game Over",
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    BoardPanel {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Score $score", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(rank?.let { "Rank $it" } ?: "Rank -", style = MaterialTheme.typography.bodySmall, color = GameColors.Accent)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(10) { index ->
                val value = scores.getOrNull(index)
                Text(
                    text = "${index + 1}. ${value ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (value == score && rank == index + 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (value == null) GameColors.MutedText else GameColors.Text
                )
            }
        }
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Restart", style = MaterialTheme.typography.labelSmall)
        }
        TextButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) {
            Text("Main Menu", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun RestartConfirmPanel(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BoardPanel {
        Text("Restart?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Current score will not be saved.", style = MaterialTheme.typography.bodySmall, color = GameColors.MutedText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                Text("Yes", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("No", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun MainMenuConfirmPanel(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BoardPanel {
        Text("Main Menu?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Current game progress will be lost.", style = MaterialTheme.typography.bodySmall, color = GameColors.MutedText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                Text("Yes", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("No", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun SettingsPanel(
    volume: Float,
    hapticEnabled: Boolean,
    onVolumeChange: (Float) -> Unit,
    onHapticToggle: () -> Unit,
    onOpenControlsEditor: () -> Unit,
    onMainMenu: () -> Unit,
    onClose: () -> Unit
) {
    BoardPanel {
        Text("Menu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Sound volume", style = MaterialTheme.typography.bodySmall, color = GameColors.MutedText)
        Slider(value = volume, onValueChange = onVolumeChange)
        SettingsActionButton(
            label = if (hapticEnabled) "Haptic On" else "Haptic Off",
            onClick = onHapticToggle,
            highlighted = hapticEnabled
        )
        SettingsActionButton(
            label = "Edit Controls",
            onClick = onOpenControlsEditor
        )
        SettingsActionButton(
            label = "Main Menu",
            onClick = onMainMenu,
            danger = true
        )
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ControlsEditorPanel(
    placements: List<ControlPlacement>,
    selectedAction: ControlAction,
    onButtonSizeChange: (ControlAction, Float) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit
) {
    BoardPanel {
        val normalizedPlacements = DefaultControlLayout.normalized(placements)
        val selectedPlacement = normalizedPlacements.firstOrNull { it.action == selectedAction } ?: normalizedPlacements.first()

        Text("Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Tap a button below, then adjust its size.", style = MaterialTheme.typography.bodySmall, color = GameColors.MutedText)
        Text(
            text = "Selected ${selectedPlacement.action.controlLabel()}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Size ${selectedPlacement.sizeDp.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = GameColors.MutedText
        )
        Slider(
            value = selectedPlacement.sizeDp,
            onValueChange = { size -> onButtonSizeChange(selectedPlacement.action, size) },
            valueRange = 44f..86f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text("Reset", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text("Done", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

private fun ControlAction.controlLabel(): String =
    when (this) {
        ControlAction.MoveLeft -> "Left"
        ControlAction.MoveRight -> "Right"
        ControlAction.SoftDrop -> "Down"
        ControlAction.HardDrop -> "Hard Drop"
        ControlAction.RotateLeft -> "Rotate -"
        ControlAction.RotateRight -> "Rotate +"
        ControlAction.Hold -> "Hold"
    }

@Composable
private fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
    danger: Boolean = false
) {
    val borderColor = when {
        danger -> GameColors.Danger
        highlighted -> GameColors.Accent
        else -> GameColors.Grid
    }
    val containerColor = when {
        danger -> GameColors.Danger.copy(alpha = 0.14f)
        highlighted -> GameColors.Accent.copy(alpha = 0.18f)
        else -> GameColors.Board.copy(alpha = 0.42f)
    }
    val contentColor = when {
        danger -> GameColors.Danger
        highlighted -> GameColors.Text
        else -> GameColors.Text
    }

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (highlighted || danger) 0.95f else 0.72f)),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun BoardPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = GameColors.Panel.copy(alpha = 0.94f),
        contentColor = GameColors.Text,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            content = content
        )
    }
}
