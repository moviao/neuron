package com.llmchat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// ---- Incoming request from browser ----
data class ChatRequest(
    val messages: List<ChatMessage>,
    val model: String? = null,
    val temperature: Double? = null,
    @JsonProperty("max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String,   // "system" | "user" | "assistant"
    val content: String
)

// ---- OpenAI-compatible request to LLM server ----
data class OpenAiRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double,
    @JsonProperty("max_tokens") val maxTokens: Int,
    val stream: Boolean = false
)

// ---- OpenAI-compatible response from LLM server ----
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChoice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    @JsonProperty("finish_reason") val finishReason: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiUsage(
    @JsonProperty("prompt_tokens") val promptTokens: Int = 0,
    @JsonProperty("completion_tokens") val completionTokens: Int = 0,
    @JsonProperty("total_tokens") val totalTokens: Int = 0
)

// ---- Response back to browser ----
data class ChatResponse(
    val message: ChatMessage,
    val model: String?,
    val usage: OpenAiUsage?
)

data class ErrorResponse(
    val error: String,
    val details: String? = null
)

// ---- Model listing ----
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiModelsResponse(
    val data: List<OpenAiModel> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiModel(
    val id: String,
    val `object`: String? = null,
    val created: Long? = null,
    @JsonProperty("owned_by") val ownedBy: String? = null
)
