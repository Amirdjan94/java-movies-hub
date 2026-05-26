package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiGetMoviesTest {
    private static MoviesServer moviesServer;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8080";
    private static final String MOVIES_TITLE = "Побег из шоушенга";
    private static final int MOVIES_YEAR = 1994;

    @BeforeAll
    static void beforeAll() {
        MoviesStore moviesStore = new MoviesStore();
        moviesServer = new MoviesServer(moviesStore, 8080);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        moviesServer.start();
    }

    @BeforeEach
    void beforeEach() {
        moviesServer.getMoviesStore().clearStore();
    }

    @AfterAll
    static void afterAll() {
        moviesServer.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpResponse<String> response = sendGetMoviesRequest();

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = response.body();
        assertTrue(body.startsWith("[") && body.endsWith("]") && body.length() == 2, "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsArray() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        HttpResponse<String> response = sendGetMoviesRequest();

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = response.body();
        assertTrue(body.startsWith("[") && body.endsWith("]") && body.indexOf(MOVIES_TITLE) != -1, "Ожидается не пустой JSON-массив");
    }

    @Test
    void getMovies_whenMultipleMovies_returnsArray() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        HttpResponse<String> response = sendGetMoviesRequest();

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = response.body();
        assertTrue(body.startsWith("[") && body.endsWith("]") && body.indexOf(MOVIES_TITLE) != body.lastIndexOf(MOVIES_TITLE), "Ожидается не пустой JSON-массив");
    }

    public HttpResponse<String> sendGetMoviesRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .header("Accept", "application/json")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }
}