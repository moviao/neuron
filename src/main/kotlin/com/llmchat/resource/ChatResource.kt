package com.llmchat.resource

import com.llmchat.model.ChatRequest
import com.llmchat.model.ChatMessage
import com.llmchat.model.ErrorResponse
import com.llmchat.service.ChatService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ChatResource {

    private val log = Logger.getLogger(ChatResource::class.java)

    @Inject
    lateinit var chatService: ChatService

    @ConfigProperty(name = "llm.api.url")
    lateinit var llmApiUrl: String

    @ConfigProperty(name = "llm.api.model", defaultValue = "local-model")
    lateinit var llmModel: String

    // ── Chat ──────────────────────────────────────────────────────────────────
    @POST
    @Path("/chat")
    fun chat(request: ChatRequest): Response {
        return try {
            log.debug("Received chat request with ${request.messages.size} messages")
            val response = chatService.chat(request)
            Response.ok(response).build()
        } catch (e: Exception) {
            log.error("Error processing chat request", e)
            Response.status(Response.Status.BAD_GATEWAY)
                .entity(ErrorResponse(
                    error = "Failed to communicate with LLM server",
                    details = e.message
                ))
                .build()
        }
    }

    // ── Models ────────────────────────────────────────────────────────────────
    @GET
    @Path("/models")
    fun listModels(): Response {
        return try {
            val models = chatService.listModels()
            Response.ok(models).build()
        } catch (e: Exception) {
            log.error("Error listing models", e)
            Response.status(Response.Status.BAD_GATEWAY)
                .entity(ErrorResponse(error = "Failed to list models", details = e.message))
                .build()
        }
    }

    // ── Health ────────────────────────────────────────────────────────────────
    @GET
    @Path("/health")
    fun health(): Response {
        return Response.ok(mapOf(
            "status"  to "ok",
            "service" to "llm-chat-ui",
            "llm_url" to llmApiUrl
        )).build()
    }

    // ── Diagnose ─────────────────────────────────────────────────────────────
    // GET http://localhost:8082/api/diagnose
    // Runs three checks and returns a JSON report — use this to debug 404s.
    @GET
    @Path("/diagnose")
    fun diagnose(): Response {
        val results = mutableMapOf<String, Any>()
        results["llm_api_url"] = llmApiUrl
        results["llm_api_model"] = llmModel
        results["target_completions"] = "$llmApiUrl/v1/chat/completions"
        results["target_models"]      = "$llmApiUrl/v1/models"

        // 1. Raw TCP reachability via URL connection
        val reachable = try {
            val url = java.net.URI(llmApiUrl).toURL()
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout    = 3000
            conn.requestMethod  = "GET"
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            results["tcp_check"] = "OK — HTTP $code"
            true
        } catch (e: Exception) {
            results["tcp_check"] = "FAILED — ${e.javaClass.simpleName}: ${e.message}"
            false
        }

        // 2. GET /v1/models
        if (reachable) {
            try {
                val models = chatService.listModels()
                results["models_check"] = "OK — ${models.map { it.id }}"
            } catch (e: Exception) {
                results["models_check"] = "FAILED — ${e.message}"
            }
        } else {
            results["models_check"] = "SKIPPED (server unreachable)"
        }

        // 3. Minimal POST /v1/chat/completions
        if (reachable) {
            try {
                val probe = ChatRequest(
                    messages = listOf(ChatMessage(role = "user", content = "Hi")),
                    model = llmModel,
                    temperature = 0.0,
                    maxTokens = 8
                )
                val resp = chatService.chat(probe)
                results["completions_check"] = "OK — reply: \"${resp.message.content.take(80)}\""
            } catch (e: Exception) {
                results["completions_check"] = "FAILED — ${e.message}"
            }
        } else {
            results["completions_check"] = "SKIPPED (server unreachable)"
        }

        // Overall verdict
        val allOk = results.values.none { it.toString().startsWith("FAILED") }
        results["verdict"] = if (allOk) "✅ Everything looks good" else "❌ See FAILED checks above"

        return Response.ok(results).build()
    }
}

