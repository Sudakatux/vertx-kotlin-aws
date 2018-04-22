package io.alanroche.vertx.dynamodb

import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.experimental.launch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


class DynamoDbVerticleSpek : Spek({
    given("a dynamodb verticle") {
        var vertx: Vertx? = null
        given("an event bus") {
            val eventBus = vertx!!.eventBus()
             on("a get message") {
                 val id = "test-id"
                 launch {
                     val reply: Message<String> = awaitResult {
                         eventBus?.send("datastore.item.get", id, it)
                     }
                 }


                it("should return a reply") {
                    // TODO
                }
            }
        }

        beforeEachTest {
            vertx = Vertx.vertx()
        }

        afterEachTest {
            vertx?.close()
            vertx = null
        }
    }
})