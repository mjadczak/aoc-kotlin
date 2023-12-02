package uk.co.mjdk.aoc

import java.io.Reader
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class AocClient {
    private val cachePath = Paths.get(".cache")
    private val cookieMgr = CookieManager()
    private val baseUri = URI.create("https://adventofcode.com/")
    private val client: HttpClient

    init {
        // Grab it out of the browser and place it at the path
        val session = cachePath.resolve("session.cookie").readText(StandardCharsets.UTF_8).trim()
        val cookie = HttpCookie("session", session).apply {
            path = "/"
            version = 0
        }
        cookieMgr.cookieStore.add(baseUri, cookie)

        client = HttpClient.newBuilder().cookieHandler(cookieMgr).connectTimeout(Duration.ofSeconds(10)).build()
    }

    private fun cachePath(year: Int, day: Int): Path = cachePath.resolve("$year/input${day.format(2)}.txt")

    private fun fetchInput(year: Int, day: Int): String {
        val req = HttpRequest.newBuilder(baseUri.resolve("$year/day/$day/input")).GET().build()
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .also { check(it.statusCode() in 200..<300) { "${it.statusCode()}: ${it.body()}" } }.body()
    }

    private fun readInputFromCache(year: Int, day: Int): String? =
        cachePath(year, day).takeIf { it.exists() }?.readText()

    private fun writeInputToCache(year: Int, day: Int, contents: String) {
        cachePath(year, day).createParentDirectories().writeText(contents)
    }

    fun getInput(year: Int, day: Int): String =
        readInputFromCache(year, day) ?: fetchInput(year, day).also { writeInputToCache(year, day, it) }
}

fun aocString(year: Int, day: Int, trimNewLine: Boolean = true): String =
    AocClient().getInput(year, day).let { if (trimNewLine) it.trim() else it }

// Old and busted, use the AocClient / aoc function, there is actually no need to read it in a buffered way...
fun aocReader(year: Int, day: Int): Reader = aocString(year, day).reader()
