package io.alanroche.vertx.dynamodb

import io.vertx.core.Vertx.vertx

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("hello 1")
        val vertx = vertx()
        vertx.deployVerticle(DynamoDbRestServer()) { ar ->
            if (ar.succeeded()) {
                println("Application started")
            } else {
                println("Could not start application")
                ar.cause().printStackTrace()
            }
        }
    }
}