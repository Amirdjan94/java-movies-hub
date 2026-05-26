package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiGetMoviesByYearTest {
    private static MoviesServer moviesServer;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8080";
    private static final String MOVIES_TITLE = "Побег из шоушенга";
    private static final String INVALID_YEAR = "19f4";
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
    void getMoviesByYear_existValidMovieByYear_returnsMovie() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        HttpResponse<String> response = sendGetMoviesRequest(MOVIES_YEAR);

        assertEquals(200, response.statusCode(), "GET /movies?year=YYYY должен вернуть 200");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        List<Movie> newMovie = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(MOVIES_YEAR, newMovie.get(0).getYear(), "Отображается не корректный фильм");
        assertEquals(MOVIES_TITLE, newMovie.get(0).getTitle(), "Отображается не корректный фильм");
        assertEquals(1, newMovie.get(0).getId(), "Отображается не корректный фильм");
    }

    @Test
    void getMoviesByYear_existValidMultipleMoviesByYear_returnsMovies() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        HttpResponse<String> response = sendGetMoviesRequest(MOVIES_YEAR);

        assertEquals(200, response.statusCode(), "GET /movies?year=YYYY должен вернуть 200");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        List<Movie> newMovie = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, newMovie.size(), "Отображается не корректный фильм");
        assertEquals(MOVIES_YEAR, newMovie.get(0).getYear(), "Отображается не корректный фильм");
        assertEquals(MOVIES_TITLE, newMovie.get(0).getTitle(), "Отображается не корректный фильм");
        assertEquals(1, newMovie.get(0).getId(), "Отображается не корректный фильм");
    }

    @Test
    void getMoviesByYear_notExistValidMultipleMoviesByYear_returnsNull() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        HttpResponse<String> response = sendGetMoviesRequest(MOVIES_YEAR);

        assertEquals(200, response.statusCode(), "GET /movies?year=YYYY должен вернуть 200");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        List<Movie> newMovie = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(0, newMovie.size(), "Отображается не корректный фильм");
    }

    @Test
    void getMoviesByYear_invalidYear_returnsError() throws Exception {

        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        moviesServer.getMoviesStore().addMovies(MOVIES_TITLE, MOVIES_YEAR + 1);
        HttpResponse<String> response = sendGetMoviesRequest(INVALID_YEAR);

        assertEquals(400, response.statusCode(), "GET /movies?year=YYYY должен вернуть 400");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertTrue(response.body().contains("Некорректный параметр запроса"), "Отображается не корректный текст ошибки");
    }

    public HttpResponse<String> sendGetMoviesRequest(int year) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }

    public HttpResponse<String> sendGetMoviesRequest(String year) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }

}
