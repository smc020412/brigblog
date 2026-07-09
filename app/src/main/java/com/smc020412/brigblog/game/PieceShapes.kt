package com.smc020412.brigblog.game

object PieceShapes {
    val shapes: Map<PieceType, List<List<Cell>>> = mapOf(
        PieceType.I to listOf(
            listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(3, 1)),
            listOf(Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 3)),
            listOf(Cell(0, 2), Cell(1, 2), Cell(2, 2), Cell(3, 2)),
            listOf(Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(1, 3))
        ),
        PieceType.J to listOf(
            listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
            listOf(Cell(1, 0), Cell(2, 0), Cell(1, 1), Cell(1, 2)),
            listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(2, 2)),
            listOf(Cell(1, 0), Cell(1, 1), Cell(0, 2), Cell(1, 2))
        ),
        PieceType.L to listOf(
            listOf(Cell(2, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
            listOf(Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 2)),
            listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(0, 2)),
            listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(1, 2))
        ),
        PieceType.O to List(4) {
            listOf(Cell(1, 0), Cell(2, 0), Cell(1, 1), Cell(2, 1))
        },
        PieceType.S to listOf(
            listOf(Cell(1, 0), Cell(2, 0), Cell(0, 1), Cell(1, 1)),
            listOf(Cell(1, 0), Cell(1, 1), Cell(2, 1), Cell(2, 2)),
            listOf(Cell(1, 1), Cell(2, 1), Cell(0, 2), Cell(1, 2)),
            listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(1, 2))
        ),
        PieceType.T to listOf(
            listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
            listOf(Cell(1, 0), Cell(1, 1), Cell(2, 1), Cell(1, 2)),
            listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(1, 2)),
            listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(1, 2))
        ),
        PieceType.Z to listOf(
            listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 1)),
            listOf(Cell(2, 0), Cell(1, 1), Cell(2, 1), Cell(1, 2)),
            listOf(Cell(0, 1), Cell(1, 1), Cell(1, 2), Cell(2, 2)),
            listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(0, 2))
        )
    )
}
