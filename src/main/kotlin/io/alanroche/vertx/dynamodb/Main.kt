package io.alanroche.vertx.dynamodb

import io.vertx.core.Vertx.vertx

/**
 * Main driver app, starts Verticles
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = vertx()
        vertx.deployVerticle(RestServerVerticle()) {
            it.map { println("Application started") }.otherwise { println("Could not start application") }
        }
    }
}