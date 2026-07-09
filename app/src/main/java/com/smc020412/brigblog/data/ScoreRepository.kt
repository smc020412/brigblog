package com.smc020412.brigblog.data

import android.content.Context

class ScoreRepository(
    context: Context
) {
    private val preferences = context.getSharedPreferences("brigblog_scores", Context.MODE_PRIVATE)

    fun getScores(): List<Int> =
        preferences.getString(KEY_SCORES, "")
            .orEmpty()
            .split(",")
            .mapNotNull { value -> value.toIntOrNull() }
            .sortedDescending()
            .take(MAX_SCORES)

    fun getBestScore(): Int =
        getScores().firstOrNull() ?: 0

    fun saveScore(score: Int): List<Int> {
        if (score <= 0) return getScores()

        val scores = (getScores() + score)
            .sortedDescending()
            .take(MAX_SCORES)

        preferences.edit()
            .putString(KEY_SCORES, scores.joinToString(","))
            .apply()

        return scores
    }

    fun getTimeScores(durationSeconds: Int): List<Int> =
        getScoresForKey(timeScoreKey(durationSeconds))

    fun saveTimeScore(durationSeconds: Int, score: Int): List<Int> {
        if (score <= 0) return getTimeScores(durationSeconds)

        val scores = (getTimeScores(durationSeconds) + score)
            .sortedDescending()
            .take(MAX_SCORES)

        preferences.edit()
            .putString(timeScoreKey(durationSeconds), scores.joinToString(","))
            .apply()

        return scores
    }

    fun getSurvivalScores(): List<Int> =
        getScoresForKey(KEY_SURVIVAL_SCORES)

    fun saveSurvivalScore(score: Int): List<Int> {
        if (score <= 0) return getSurvivalScores()

        val scores = (getSurvivalScores() + score)
            .sortedDescending()
            .take(MAX_SCORES)

        preferences.edit()
            .putString(KEY_SURVIVAL_SCORES, scores.joinToString(","))
            .apply()

        return scores
    }

    private fun getScoresForKey(key: String): List<Int> =
        preferences.getString(key, "")
            .orEmpty()
            .split(",")
            .mapNotNull { value -> value.toIntOrNull() }
            .sortedDescending()
            .take(MAX_SCORES)

    companion object {
        const val MAX_SCORES = 10
        private const val KEY_SCORES = "scores"
        private const val KEY_SURVIVAL_SCORES = "survival_scores"

        private fun timeScoreKey(durationSeconds: Int): String =
            "time_scores_$durationSeconds"
    }
}
