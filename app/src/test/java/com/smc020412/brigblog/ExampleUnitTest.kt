package com.smc020412.brigblog

import com.smc020412.brigblog.game.Board
import com.smc020412.brigblog.game.GameConstants
import com.smc020412.brigblog.game.GameEngine
import com.smc020412.brigblog.game.GameState
import com.smc020412.brigblog.game.Piece
import com.smc020412.brigblog.game.PieceType
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun groundedMovementConsumesLockResetsOnlyUpToLimit() {
        val engine = GameEngine()
        var state = groundedState()

        repeat(GameConstants.LOCK_RESET_LIMIT) { index ->
            state = engine.move(state, if (index % 2 == 0) -1 else 1, 0)
            assertEquals(index + 1, state.lockResetCount)
        }

        state = engine.move(state, -1, 0)

        assertEquals(GameConstants.LOCK_RESET_LIMIT, state.lockResetCount)
    }

    @Test
    fun lockingPieceResetsLockResetCountForNextPiece() {
        val engine = GameEngine()
        val state = groundedState().copy(lockResetCount = GameConstants.LOCK_RESET_LIMIT)

        val next = engine.lockCurrent(state)

        assertEquals(0, next.lockResetCount)
    }

    private fun groundedState(): GameState {
        val board = Board(
            width = GameConstants.BOARD_WIDTH,
            height = GameConstants.BOARD_HEIGHT,
            cells = List(GameConstants.BOARD_HEIGHT) {
                List<PieceType?>(GameConstants.BOARD_WIDTH) { null }
            }
        )

        return GameState(
            board = board,
            queue = listOf(PieceType.I, PieceType.J, PieceType.L, PieceType.S, PieceType.T, PieceType.Z),
            currentPiece = Piece(PieceType.O, x = 3, y = GameConstants.BOARD_HEIGHT - 2),
            heldPiece = null,
            canHold = true,
            score = 0,
            lines = 0,
            level = 1,
            isGameOver = false,
            isPaused = false,
            lastClearedRows = emptyList(),
            lastDropDistance = 0
        )
    }
}
