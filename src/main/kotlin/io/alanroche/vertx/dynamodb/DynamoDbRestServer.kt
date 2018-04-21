package io.alanroche.vertx.dynamodb

import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import io.vertx.kotlin.core.json.*
import java.util.*

class DynamoDbRestServer : CoroutineVerticle {
    init {
        println("Init ${javaClass.simpleName}")
    }

    constructor() : super()

    suspend override fun start() {
        // Start the server
        val router = Router.router(vertx)
        router.get("/item/:id").coroutineHandler { ctx -> getItem(ctx) }

        awaitResult<HttpServer> {
            vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config.getInteger("http.port", 8080), it)
        }
    }

    // Send info about a movie
    suspend fun getItem(ctx: RoutingContext) {
        val id = UUID.randomUUID().toString()
        println("id: $id")
        ctx.response().end(json {
            obj("id" to id).encode()
        })
    }

    /**
     * An extension method for simplifying coroutines usage with Vert.x Web routers
     */
    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }
}