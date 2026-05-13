package com.ncert7.aitutorandlab.ui.screens.mathagentscreen.dataclass

data class MathProblemUi(
    val id: String,
    val topic: String,
    val difficulty: String? = null
) {
    companion object {
        fun create(id: String, topic: String, difficulty: String? = null) =
            MathProblemUi(id, topic, difficulty)
    }
}
