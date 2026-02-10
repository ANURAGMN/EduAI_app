package com.anurag.eduai.domain.chatbot.usecase

import com.anurag.eduai.data.remote.SessionMetadata
import com.anurag.eduai.debug.DebugLogger
import com.anurag.eduai.domain.chatbot.model.ResourceDecision
import javax.inject.Inject

class ResourceDecisionUseCase @Inject constructor() {

    fun decide(metadata: SessionMetadata): ResourceDecision {
        try {
            val transition = metadata.nodeTransitions.last()
            val from = transition["from_node"] as? String
            val to = transition["to_node"] as? String

            DebugLogger.debugLog("ResourceDecisionUseCase", "Transition: $from → $to")

            return when {
                from == "APK" && to == "CI" && !metadata.imageUrl.isNullOrBlank() -> {
                    val processedUrl = processImageUrl(metadata.imageUrl)
                    DebugLogger.debugLog("ResourceDecisionUseCase", "Image URL: $processedUrl")
                    ResourceDecision.ShowImage(
                        url = processedUrl,
                        description = metadata.imageDescription
                    )
                }

                from == "CI" && to == "SIM_CC" -> {
                    ResourceDecision.ShowConceptMap(triggerText = "")
                }

                else -> ResourceDecision.None
            }
        } catch (e: Exception) {
            DebugLogger.errorLog("ResourceDecisionUseCase", "Error: ${e.message}")
            return ResourceDecision.None
        }
    }

    fun processImageUrl(url: String): String {
        return when {
            url.contains("github.com") && url.contains("/blob/") -> {
                url.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }
            else -> url
        }
    }
}

