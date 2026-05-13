package com.ncert7.aitutorandlab.service.analytics


enum class ScreenName(val displayName: String) {
    LOGIN("LOGIN"),
    USER_DETAIL_ENTRY("USER_DETAIL_ENTRY"),
    HOME("HOME"),
    SUBJECT("SUBJECT"),
    CHAPTER("CHAPTER"),
    CONCEPT("CONCEPT"),
    CONCEPT_DETAIL("CONCEPT_DETAIL"),
    PROGRESS("PROGRESS"),
    SETTINGS("SETTINGS"),
    CHATBOT("CHATBOT"),
    SIMULATIONLIST("SIMULATION_LIST"),
    SIMULATIONVIEWER("SIMULATION_VIEWER"),
    SIMULATIONAGENT("SIMULATION_AGENT"),
    REVISION("REVISION"),
    MATH_AGENT("MATH_AGENT")

}

enum class EventType(val type: String) {
    ENTRY("ENTRY"),
    EXIT("EXIT")
}