package ru.practicum.moviehub.http;

import com.google.gson.Gson;
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

public class MoviesApiDeleteMoviesByIdTest {
    private static MoviesServer moviesServer;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8080";
    private static final String MOVIES_TITLE = "Побег из шоушенга";
    private static final String EXIST_ID = "1";
    private static final String NOT_EXIST_ID = "2";
    private static final String INCORRECT_ID = "2f";
    private static final int MOVIES_YEAR = 1994;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
        MoviesStore moviesStore = new MoviesStore();
        moviesServer = new MoviesServer(moviesStore, 8080);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        gson = new Gson();
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
    void getMovieById_existValidMovieID_returnsMovie() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);

        HttpResponse<String> response = sendDeleteMoviesRequest(EXIST_ID);
        assertEquals(204, response.statusCode(), "DELETE /movies/{id} должен вернуть 204");
        assertTrue(moviesServer.getMoviesStore().getMoviesMap().isEmpty(), "Фильм не удален из хранилища");
    }

    @Test
    void getMovieById_whenNotExistMovieID_returnsError() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        HttpResponse<String> response = sendDeleteMoviesRequest(NOT_EXIST_ID);

        assertEquals(404, response.statusCode(), "DELETE /movies/{id} должен вернуть 404");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertTrue(response.body().contains("Фильм не найден"), "Не корректное сообщение об ошибке");

    }

    @Test
    void getMovieById_whenIncorrectMovieID_returnsError() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        HttpResponse<String> response = sendDeleteMoviesRequest(INCORRECT_ID);

        assertEquals(400, response.statusCode(), "DELETE /movies/{id} должен вернуть 400");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertTrue(response.body().contains("Некорректный ID"), "Не корректное сообщение об ошибке");

    }


    public HttpResponse<String> sendDeleteMoviesRequest(String ID) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/" + ID))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }

}
