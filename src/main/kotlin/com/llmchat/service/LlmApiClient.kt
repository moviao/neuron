package com.llmchat.service

import com.llmchat.model.OpenAiModelsResponse
import com.llmchat.model.OpenAiRequest
import com.llmchat.model.OpenAiResponse
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
@Path("/v1")
interface LlmApiClient {

    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun chatCompletions(request: OpenAiRequest): OpenAiResponse

    @GET
    @Path("/models")
    @Produces(MediaType.APPLICATION_JSON)
    fun listModels(): OpenAiModelsResponse
}
