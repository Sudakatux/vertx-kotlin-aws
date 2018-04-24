package io.alanroche.vertx.dynamodb


import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import io.kotlintest.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class DynamoDbVerticleTest {
    private lateinit var vertx: Vertx
    val server = ServerRunner.createServerFromCommandLineArgs(arrayOf("-inMemory"))

    @Before
    fun beforeEach() {
        vertx = Vertx.vertx()
        server.start()
        System.setProperty("aws.accessKeyId", "applmgr")
        System.setProperty("aws.secretKey", "applmgr")
        System.setProperty("aws.secretAccessKey", "applmgr")
        System.setProperty("aws.region", "us-west-2")
        System.setProperty("java.library.path", "build/libs")

        println("########## Started")
    }

    @After
    fun afterEach(testContext: TestContext) {
        server.stop()
        vertx.close(testContext.asyncAssertSuccess())
    }

    @Test
    fun test(testContext: TestContext) {
        val dynamodb = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                // we can use any region here
                AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "ddblocal"))
                .build()
        val result = dynamodb.listTables()
        result.tableNames.forEach { println("Table: $it") }
        val async = testContext.async()
        try {
            launch(vertx.dispatcher()) {
                awaitResult<String> { vertx.deployVerticle(DynamoDbVerticle(), it) }
                val reply: Message<String> = awaitResult {
                    vertx.eventBus().send(Endpoint.DATASTORE_ITEM_GET.name, "id", it)
                }
                println("######### Reply: $reply")
                async.complete()
                reply.body() shouldBe "bar"

            }

        } catch (e: ReplyException) {
            // Handle specific reply exception here
            println("Reply failure: ${e.message}")
        }
    }
}