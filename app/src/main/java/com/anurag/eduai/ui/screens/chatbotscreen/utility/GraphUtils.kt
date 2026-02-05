package com.anurag.eduai.ui.screens.chatbotscreen.utility

import com.anurag.eduai.ui.screens.chatbotscreen.dataclass.GraphData
import kotlinx.serialization.json.Json
import kotlin.text.trimIndent

class GraphUtils {
    fun createDefaultGraphData(): GraphData {
        val defaultJson = """
    {
      "visualization_type": "Concept Map",
      "main_concept": "Loading...",
      "nodes": [
        {"id": "A", "label": "Concept", "category": "Core"}
      ],
      "edges": [],
      "audioSegments": [
        {
          "segmentIndex": 0,
          "spokenText": "Loading concept map...",
          "estimatedDuration": 2.0,
          "highlightNodeIds": ["A"],
          "showNodeIds": ["A"],
          "highlightEdgeIds": [],
          "action": "introduce"
        }
      ]
    }
    """.trimIndent()

        try {
            return Json.decodeFromString<GraphData>(defaultJson)
        } catch (e: Exception) {
            throw kotlin.IllegalStateException("Graph class structure mismatch:\n $e")
        }
    }
}