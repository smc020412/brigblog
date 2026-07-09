package com.smc020412.brigblog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smc020412.brigblog.game.GameConstants
import com.smc020412.brigblog.game.GameEngine
import com.smc020412.brigblog.game.GameState
import com.smc020412.brigblog.game.Piece
import com.smc020412.brigblog.game.PieceType
import com.smc020412.brigblog.game.SurvivalAttackKind

@Composable
fun GameBoardCanvas(
    state: GameState,
    engine: GameEngine,
    lineClearProgress: Float = 1f,
    noiseTimeMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .border(1.dp, GameColors.Grid)
            .fillMaxSize()
    ) {
        val cell = minOf(
            size.width / GameConstants.BOARD_WIDTH,
            size.height / GameConstants.BOARD_HEIGHT
        )
        val boardWidth = cell * GameConstants.BOARD_WIDTH
        val boardHeight = cell * GameConstants.BOARD_HEIGHT
        val origin = Offset((size.width - boardWidth) / 2f, (size.height - boardHeight) / 2f)

        drawRect(GameColors.Board, origin, Size(boardWidth, boardHeight))

        val clearingCoordinates = state.clearingBlocks
            .map { block -> block.x to block.y }
            .toSet()

        state.board.cells.forEachIndexed { y, row ->
            row.forEachIndexed { x, type ->
                if (type != null) {
                    if (x to y in clearingCoordinates) {
                        drawClearingBlock(
                            origin = origin,
                            cell = cell,
                            x = x,
                            y = y,
                            color = GameColors.piece(type),
                            progress = lineClearProgress
                        )
                    } else if (type == PieceType.Garbage) {
                        drawGarbageBlock(origin, cell, x, y, noiseTimeMs)
                    } else {
                        drawBlock(origin, cell, x, y, GameColors.piece(type))
                    }
                }
            }
        }

        engine.ghostPiece(state)?.let { ghost ->
            drawPiece(engine, ghost, origin, cell, alpha = 0.22f)
        }

        state.currentPiece?.let { piece ->
            drawPiece(engine, piece, origin, cell, alpha = 1f)
        }

        state.attackObjects.forEach { attackObject ->
            when (attackObject.kind) {
                SurvivalAttackKind.FallingGarbage -> drawAnimatedGarbageBlock(
                    origin = origin,
                    cell = cell,
                    x = attackObject.x,
                    y = attackObject.y,
                    noiseTimeMs = noiseTimeMs
                )
                SurvivalAttackKind.RisingGarbage -> drawRisingAttackRow(
                    origin = origin,
                    cell = cell,
                    y = attackObject.y,
                    cells = attackObject.cells,
                    noiseTimeMs = noiseTimeMs
                )
            }
        }

        for (x in 0..GameConstants.BOARD_WIDTH) {
            val px = origin.x + x * cell
            drawLine(
                color = GameColors.Grid.copy(alpha = 0.45f),
                start = Offset(px, origin.y),
                end = Offset(px, origin.y + boardHeight),
                strokeWidth = 1f
            )
        }

        for (y in 0..GameConstants.BOARD_HEIGHT) {
            val py = origin.y + y * cell
            drawLine(
                color = GameColors.Grid.copy(alpha = 0.45f),
                start = Offset(origin.x, py),
                end = Offset(origin.x + boardWidth, py),
                strokeWidth = 1f
            )
        }

        if (engine.dangerLevel(state) > 0f) {
            drawRect(
                color = GameColors.Danger.copy(alpha = 0.08f + engine.dangerLevel(state) * 0.12f),
                topLeft = origin,
                size = Size(boardWidth, boardHeight)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPiece(
    engine: GameEngine,
    piece: Piece,
    origin: Offset,
    cell: Float,
    alpha: Float
) {
    engine.getCells(piece).forEach { block ->
        if (block.y >= 0) {
            drawBlock(origin, cell, block.x, block.y, GameColors.piece(piece.type), alpha)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlock(
    origin: Offset,
    cell: Float,
    x: Int,
    y: Int,
    color: Color,
    alpha: Float = 1f
) {
    val inset = maxOf(1f, cell * 0.08f)
    val topLeft = Offset(origin.x + x * cell + inset, origin.y + y * cell + inset)
    val size = Size(cell - inset * 2f, cell - inset * 2f)
    drawRect(color.copy(alpha = alpha), topLeft, size)
    drawRect(Color.White.copy(alpha = alpha * 0.16f), topLeft, Size(size.width, maxOf(1f, size.height * 0.12f)))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGarbageBlock(
    origin: Offset,
    cell: Float,
    x: Int,
    y: Int,
    noiseTimeMs: Long
) {
    val inset = maxOf(1f, cell * 0.08f)
    val topLeft = Offset(origin.x + x * cell + inset, origin.y + y * cell + inset)
    val size = Size(cell - inset * 2f, cell - inset * 2f)
    val frame = (noiseTimeMs / 72L).toInt()
    val grid = 4
    val pixelW = size.width / grid
    val pixelH = size.height / grid

    drawRect(Color(0xFF101216), topLeft, size)
    repeat(grid) { py ->
        repeat(grid) { px ->
            val bright = ((px * 17 + py * 29 + x * 11 + y * 7 + frame) % 3) != 0
            drawRect(
                color = if (bright) Color(0xFFECEFF7) else Color(0xFF252A35),
                topLeft = Offset(topLeft.x + px * pixelW, topLeft.y + py * pixelH),
                size = Size(pixelW + 0.5f, pixelH + 0.5f)
            )
        }
    }
    drawRect(Color.White.copy(alpha = 0.18f), topLeft, Size(size.width, maxOf(1f, size.height * 0.10f)))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedGarbageBlock(
    origin: Offset,
    cell: Float,
    x: Int,
    y: Float,
    noiseTimeMs: Long
) {
    val inset = maxOf(1f, cell * 0.08f)
    val topLeft = Offset(origin.x + x * cell + inset, origin.y + y * cell + inset)
    val size = Size(cell - inset * 2f, cell - inset * 2f)
    val frame = (noiseTimeMs / 72L).toInt()
    val grid = 4
    val pixelW = size.width / grid
    val pixelH = size.height / grid

    drawRect(Color(0xFF101216), topLeft, size)
    repeat(grid) { py ->
        repeat(grid) { px ->
            val bright = ((px * 17 + py * 29 + x * 11 + y.toInt() * 7 + frame) % 3) != 0
            drawRect(
                color = if (bright) Color(0xFFECEFF7) else Color(0xFF252A35),
                topLeft = Offset(topLeft.x + px * pixelW, topLeft.y + py * pixelH),
                size = Size(pixelW + 0.5f, pixelH + 0.5f)
            )
        }
    }
    drawRect(Color.White.copy(alpha = 0.18f), topLeft, Size(size.width, maxOf(1f, size.height * 0.10f)))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRisingAttackRow(
    origin: Offset,
    cell: Float,
    y: Float,
    cells: List<Boolean>,
    noiseTimeMs: Long
) {
    cells.take(GameConstants.BOARD_WIDTH).forEachIndexed { x, filled ->
        if (filled) {
            drawAnimatedGarbageBlock(
                origin = origin,
                cell = cell,
                x = x,
                y = y,
                noiseTimeMs = noiseTimeMs + x * 23L
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClearingBlock(
    origin: Offset,
    cell: Float,
    x: Int,
    y: Int,
    color: Color,
    progress: Float
) {
    val sweepDelay = x * 0.035f
    val localProgress = ((progress.coerceIn(0f, 1f) - sweepDelay) / 0.72f).coerceIn(0f, 1f)

    if (localProgress >= 1f) return

    val popScale = if (localProgress < 0.22f) {
        1f + 0.12f * (localProgress / 0.22f)
    } else {
        1.12f * (1f - ((localProgress - 0.22f) / 0.78f))
    }.coerceIn(0f, 1.12f)
    val alpha = (1f - localProgress).coerceIn(0f, 1f)
    val flash = if (localProgress < 0.18f) 0.28f * (1f - localProgress / 0.18f) else 0f
    val baseSize = cell * 0.84f
    val blockSize = baseSize * popScale
    val center = Offset(origin.x + x * cell + cell / 2f, origin.y + y * cell + cell / 2f)
    val topLeft = Offset(center.x - blockSize / 2f, center.y - blockSize / 2f)

    drawRect(
        color = color.copy(alpha = alpha),
        topLeft = topLeft,
        size = Size(blockSize, blockSize)
    )
    if (flash > 0f) {
        drawRect(
            color = Color.White.copy(alpha = flash),
            topLeft = topLeft,
            size = Size(blockSize, blockSize)
        )
    }
}
