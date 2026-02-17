package com.llmchat.service

import com.llmchat.model.ChatMessage
import com.llmchat.model.ChatRequest
import com.llmchat.model.ChatResponse
import com.llmchat.model.OpenAiModel
import com.llmchat.model.OpenAiRequest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class ChatService {

    private val log = Logger.getLogger(ChatService::class.java)

    @Inject
    @RestClient
    lateinit var llmApiClient: LlmApiClient

    @ConfigProperty(name = "llm.api.model", defaultValue = "local-model")
    lateinit var defaultModel: String

    @ConfigProperty(name = "llm.api.max-tokens", defaultValue = "2048")
    var defaultMaxTokens: Int = 2048

    @ConfigProperty(name = "llm.api.temperature", defaultValue = "0.7")
    var defaultTemperature: Double = 0.7

    fun chat(request: ChatRequest): ChatResponse {
        val model = request.model ?: defaultModel
        val maxTokens = request.maxTokens ?: defaultMaxTokens
        val temperature = request.temperature ?: defaultTemperature

        val openAiRequest = OpenAiRequest(
            model = model,
            messages = request.messages,
            temperature = temperature,
            maxTokens = maxTokens,
            stream = false
        )

        log.info("→ POST /v1/chat/completions  model=$model  messages=${request.messages.size}")

        return try {
            val response = llmApiClient.chatCompletions(openAiRequest)
            log.info("← OK  finish=${response.choices.firstOrNull()?.finishReason}  tokens=${response.usage?.totalTokens}")

            val assistantMessage = response.choices.firstOrNull()?.message
                ?: ChatMessage(role = "assistant", content = "No response received from the model.")

            ChatResponse(
                message = assistantMessage,
                model = response.model,
                usage = response.usage
            )
        } catch (e: jakarta.ws.rs.WebApplicationException) {
            val status = e.response.status
            val body = runCatching { e.response.readEntity(String::class.java) }.getOrElse { "(unreadable)" }
            log.error("← HTTP $status from llama.cpp — body: $body")
            throw RuntimeException("llama.cpp returned HTTP $status — $body", e)
        } catch (e: Exception) {
            log.error("← Connection error: ${e.javaClass.simpleName}: ${e.message}")
            throw e
        }
    }

    fun listModels(): List<OpenAiModel> {
        return try {
            val models = llmApiClient.listModels().data
            log.info("← /v1/models returned ${models.size} model(s): ${models.map { it.id }}")
            models
        } catch (e: Exception) {
            // llama.cpp does implement /v1/models but only when a model is loaded.
            // Fall back gracefully so the UI still works.
            log.warn("/v1/models failed (${e.message}) — returning configured default '$defaultModel'")
            listOf(OpenAiModel(id = defaultModel))
        }
    }
}

