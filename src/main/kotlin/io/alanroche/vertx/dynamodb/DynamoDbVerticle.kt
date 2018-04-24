package io.alanroche.vertx.dynamodb

import io.vertx.kotlin.coroutines.CoroutineVerticle
import software.amazon.awssdk.services.dynamodb.DynamoDBAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse
import java.util.function.BiFunction


class DynamoDbVerticle : CoroutineVerticle {
    constructor() : super()


    override suspend fun start() {
        super.start()
        val h = vertx.nettyEventLoopGroup()
        vertx.eventBus()
                .localConsumer<String>(Endpoint.DATASTORE_ITEM_GET.name) { message ->
                    try {
                        val client = DynamoDBAsyncClient.create()

                        val id = message.body()
                        val request = GetItemRequest.builder()
                                .tableName("testtable")
                                .key(mapOf("id" to AttributeValue.builder().s(id).build()))
                                .build()
                        val resultFuture = client.getItem(request)

                        resultFuture.handleAsync<GetItemResponse>(BiFunction { v, th ->
                            if (th == null) {
                                message.reply("bar") // TODO
                                resultFuture.complete(v);
                            } else {
                                message.reply("Fail: ${th.message}")
                                resultFuture.completeExceptionally(th);
                            }
                            v
                        }, vertx.nettyEventLoopGroup()
                        )
                    } catch (e: Exception) {
//                        message.fail(1, e.message)
                        message.reply("Fail: ${e.message}")
                    }
                }
    }
}