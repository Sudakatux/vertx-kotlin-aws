package io.alanroche.vertx.dynamodb

import io.vertx.kotlin.coroutines.CoroutineVerticle

class DynamoDbVerticle : CoroutineVerticle {
    constructor() : super()

    override suspend fun start() {
        super.start()
        vertx.eventBus()
                .localConsumer<String>(Endpoint.DATASTORE_ITEM_GET.name)
                .handler {
                    it.reply("bar") // TODO
                }
    }
}