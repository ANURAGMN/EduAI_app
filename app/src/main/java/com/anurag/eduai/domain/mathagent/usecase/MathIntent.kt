package com.anurag.eduai.domain.mathagent.usecase

sealed interface MathIntent {
    data class Initialize(val userId: String) : MathIntent
    data class AutoStartWithProblem(val problemId: String) : MathIntent
    data class UpdateInputText(val text: String) : MathIntent
    data class SetKannada(val enabled: Boolean) : MathIntent
    data class SelectProblem(val problemId: String) : MathIntent
    data class SelectImage(val imageUri: String) : MathIntent
    data class SendMessage(val message: String) : MathIntent
    data class SendMessageWithImage(val message: String, val imageBase64: String) : MathIntent
    data class ContinueExistingSession(val problemId: String) : MathIntent
    data class StartFreshSession(val problemId: String) : MathIntent
    object HideAutosuggestions : MathIntent
    object MarkUserActive : MathIntent
    object MarkUserInactive : MathIntent
    object RefreshProblems : MathIntent
    object StartIdleTimer : MathIntent
    object DismissSessionDialog : MathIntent
}
