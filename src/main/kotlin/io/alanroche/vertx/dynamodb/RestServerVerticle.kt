package io.alanroche.vertx.dynamodb

import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.core.streams.end
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import software.amazon.awssdk.services.dynamodb.DynamoDBAsyncClient


/**
 * Non blocking Rest Server, delegates to data repository verticle
 */
class RestServerVerticle : CoroutineVerticle {

    init {
        println("Init ${javaClass.simpleName}") // TODO change to log4j2
    }

    val client = DynamoDBAsyncClient.builder()

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

    suspend fun getItem(ctx: RoutingContext) {
        val id = ctx.request().getParam("id")

        // Send a message and wait for a reply
        try {
            val reply: Message<String> = awaitResult {
                vertx.eventBus().send("datastore.item.get", id, it)
            }
            ctx.response().end {
                obj("id" to reply.body()).encode()
            }
        } catch(e: ReplyException) {
            // Handle specific reply exception here
            println("Reply failure: ${e.message}")
        }
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