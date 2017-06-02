package me.jacobrr.knf

import com.zaxxer.hikari.*
import freemarker.cache.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import java.util.*
import io.requery.sql.*
import mu.KotlinLogging
import org.jetbrains.ktor.logging.CallLogging

private val logger = KotlinLogging.logger {}

val properties = Properties().apply {
    setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
    setProperty("dataSource.user", System.getenv("DB_USER"))
    setProperty("dataSource.databaseName", System.getenv("DB_NAME"))
    setProperty("dataSource.portNumber", System.getenv("DB_PORT"))
    setProperty("dataSource.serverName", System.getenv("DB_SERVER"))
    setProperty("dataSource.password", System.getenv("DB_PASS"))
}

val hikariConfig = HikariConfig(properties)

val dataSource = if (hikariConfig.dataSourceClassName != null)
    HikariDataSource(hikariConfig)
else
    HikariDataSource()

val html_utf8 = ContentType.Text.Html.withCharset(Charsets.UTF_8)


val configuration = KotlinConfiguration(dataSource = dataSource, model = Models.DEFAULT)
val data = KotlinEntityDataStore<Any>(configuration)


fun Application.module() {
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(PartialContentSupport)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(environment.classLoader, "templates")
    }
    install(CallLogging)
    install(StatusPages) {
        exception<Exception> { exception ->
            call.respond(FreeMarkerContent("error.ftl", exception, "", html_utf8))
        }
    }

    install(Routing) {


        get("/envs") {
            call.respond(System.getenv("DATABASE_URL"))
        }

        get("hello") {
            call.respond("Hello World")
        }

        get("error") {
            throw IllegalStateException("An invalid place to be …")
        }

        get("/") {
            val model = HashMap<String, Any>()
            model.put("message", "Hello World!")
            val etag = model.toString().hashCode().toString()
            call.respond(FreeMarkerContent("index.ftl", model, etag, html_utf8))
        }

        get("/db") {
            val model = HashMap<String, Any>()

            val user = UserAccountEntity()
            user.active = true
            user.facebookToken = "mine" + Random().nextInt()
            data.insert(user as UserAccount)

            data {
                val result = select(UserAccount::class) limit 10
                val list = result.get().toList()

                model.put("results", list.map { "${it.id} have fb token ${it.facebookToken} is active: ${it.active}" })

            }

            val etag = model.toString().hashCode().toString()
            call.respond(FreeMarkerContent("db.ftl", model, etag, html_utf8))
        }
    }
}

fun main(args: Array<String>) {
    val debug = System.getenv("DEBUG").toBoolean()

    if (debug) SchemaModifier(configuration).run {
        logger.info { "Reinstantiating tables" }
        dropTables()
        createTables(TableCreationMode.CREATE)
        logger.info { "Reinstantiated tables" }
    }
    val port = Integer.valueOf(System.getenv("PORT"))
    embeddedServer(Netty, port, reloadPackages = listOf("knf"), module = Application::module).start()
}


