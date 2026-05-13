package com.ncert7.aitutorandlab.domain.mathagent.usecase

import com.ncert7.aitutorandlab.data.remote.AgenticAIClient
import com.ncert7.aitutorandlab.debug.DebugLogger
import com.ncert7.aitutorandlab.ui.screens.mathagentscreen.dataclass.MathProblemUi
import javax.inject.Inject

class MathProblemsUseCase @Inject constructor(
    private val agenticAIClient: AgenticAIClient
) {

    suspend fun getAvailableProblems(): Result<List<MathProblemUi>> {
        return try {
            val result = agenticAIClient.getAvailableMathProblems()

            if (result.isSuccess) {
                val response = result.getOrNull()
                DebugLogger.debugLog("MathProblemsUseCase", "Response received: success=${response?.success}, total=${response?.total}")

                val problems = response?.problems?.map { problem ->
                    MathProblemUi.create(
                        id = problem.problemId,
                        topic = problem.topic,
                        difficulty = problem.difficulty
                    )
                } ?: emptyList()

                DebugLogger.debugLog("MathProblemsUseCase", "Problems loaded: ${problems.size}")
                if (problems.isEmpty()) {
                    DebugLogger.errorLog("MathProblemsUseCase", "WARNING: No problems loaded! Raw response: ${response?.problems}")
                }
                Result.success(problems)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error loading problems"))
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("MathProblemsUseCase", "Exception loading problems: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun findProblemById(problems: List<MathProblemUi>, problemId: String): MathProblemUi? {
        return problems.find { it.id == problemId }
    }
}
