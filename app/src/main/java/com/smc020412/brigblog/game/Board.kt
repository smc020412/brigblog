package com.smc020412.brigblog.game

// 게임판 자체를 저장함.
data class Board(
    val width: Int,
    val height: Int,
    val cells: List<List<PieceType?>>
)
