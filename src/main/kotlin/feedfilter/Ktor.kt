package com.eddsteel.feedfilter

import io.ktor.features.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.PipelineContext


class RSSConverter() : ContentConverter {
    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? =
        throw RuntimeException("Nah")
    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? = if (value is RSS) {
        TextContent(value.data, contentType.withCharset(context.call.suitableCharset()))
    } else null
}

fun serve(feeds: FeedFetcher) {
    embeddedServer(Netty, port = 8086) {
        install(ContentNegotiation) {
            register(ContentType("application", "xml+rss"), RSSConverter())
        }
        routing {
            get("/{feed}") {
                val feed = call.parameters["feed"]
                feeds.fetch(feed)?.let {
                    call.respond(it)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }.start(wait = true)
}
