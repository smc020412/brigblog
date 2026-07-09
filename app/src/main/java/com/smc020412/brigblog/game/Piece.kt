package com.smc020412.brigblog.game

// 현재 낙하 중인 블록의 상태.
data class Piece(
    val type: PieceType,
    val rotation: Int = 0,
    val x: Int = 3, // x 좌표 보드의 중앙
    val y: Int = -1 // y 좌표 보드의 최상단 음수라 보드에 표시안됨.
)
