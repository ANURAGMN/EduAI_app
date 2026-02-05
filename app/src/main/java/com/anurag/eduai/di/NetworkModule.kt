package com.anurag.eduai.di

import com.anurag.eduai.BuildConfig
import com.anurag.eduai.data.remote.AgenticAIClient
import com.anurag.eduai.data.remote.GeminiLLMClient
import com.anurag.eduai.data.remote.LLMClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides network client dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAgenticAIClient(): AgenticAIClient {
        return AgenticAIClient(BuildConfig.AGENTIC_AI_BASE_URL)
    }

    @Provides
    @Singleton
    fun provideGeminiLLMClient(): GeminiLLMClient {
        return GeminiLLMClient(
            apiKey = BuildConfig.GEMINI_API_KEY,
            userClass = "7",
            nodeNumber = "8",
            maxWord = "250",
            model = "gemma-3-27b-it"
        )
    }

    @Provides
    @Singleton
    fun provideGroqLLMClient(): LLMClient {
        return LLMClient(
            apiKey = BuildConfig.GROQ_API_KEY,
            userClass = "7",
            nodeNumber = "8",
            maxWord = "250",
            model = "meta-llama/llama-4-scout-17b-16e-instruct"
        )
    }



}
