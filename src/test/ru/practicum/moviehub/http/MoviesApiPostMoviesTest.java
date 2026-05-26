package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesApiPostMoviesTest {
    private static MoviesServer moviesServer;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8080";
    private static final String MOVIES_TITLE = "Побег из шоушенга";
    private static final String ERROR_OF_VALIDATION = "Ошибка валидации";
    private static final String EMPTY_TITLE_ERROR = "название не должно быть пустым";
    private static final String BIG_TITLE_ERROR = "длина названия не должно превышать 100 символов";
    private static final String INVALID_YEAR_ERROR = "год должен быть между 1888 и 2026";
    private static final String DUBLICATE_ERROR = "фильм был добавлен ранее";
    private static final int MAX_YEAR = LocalDate.now().getYear() + 1;
    private static final int MIN_YEAR = 1888;
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
    void addMovies_whenValidData_returnsMovie() throws Exception {
        String json = gson.toJson(new Movie(MOVIES_TITLE, MOVIES_YEAR));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(201, response.statusCode(), "POST /movies должен вернуть 201");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        Movie newMovie = gson.fromJson(response.body(), Movie.class);
        assertEquals(1, newMovie.getId(), "Первый фильм должен иметь ID = 1");
        assertEquals(MOVIES_TITLE, newMovie.getTitle(), "Не правильное название фильма");
        assertEquals(MOVIES_YEAR, newMovie.getYear(), "Не правильное год выпуска фильма");
    }

    @Test
    void addMovies_whenValidDataAndMaxYear_returnsMovie() throws Exception {
        String json = gson.toJson(new Movie(MOVIES_TITLE, MAX_YEAR));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(201, response.statusCode(), "POST /movies должен вернуть 201");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        Movie newMovie = gson.fromJson(response.body(), Movie.class);
        assertEquals(1, newMovie.getId(), "Первый фильм должен иметь ID = 1");
        assertEquals(MOVIES_TITLE, newMovie.getTitle(), "Не правильное название фильма");
        assertEquals(MAX_YEAR, newMovie.getYear(), "Не правильное год выпуска фильма");
    }

    @Test
    void addMovies_whenValidDataAndMinYear_returnsMovie() throws Exception {
        String json = gson.toJson(new Movie(MOVIES_TITLE, MIN_YEAR));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(201, response.statusCode(), "POST /movies должен вернуть 201");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        Movie newMovie = gson.fromJson(response.body(), Movie.class);
        assertEquals(1, newMovie.getId(), "Первый фильм должен иметь ID = 1");
        assertEquals(MOVIES_TITLE, newMovie.getTitle(), "Не правильное название фильма");
        assertEquals(MIN_YEAR, newMovie.getYear(), "Не правильное год выпуска фильма");
    }

    @Test
    void addMovies_whenEmptyTitle_returnsError() throws Exception {
        String json = gson.toJson(new Movie("", MOVIES_YEAR));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(422, response.statusCode(), "POST /movies должен вернуть 422");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertEquals(ERROR_OF_VALIDATION, errorResponse.getError(), "Не правильное описание ошибки");
        assertEquals(EMPTY_TITLE_ERROR, errorResponse.getDetails()[0], "Не правильные детали ошибки");
    }

    @Test
    void addMovies_whenYearLessThan_MIN_YEAR_returnsError() throws Exception {
        String json = gson.toJson(new Movie(MOVIES_TITLE, MIN_YEAR - 1));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(422, response.statusCode(), "POST /movies должен вернуть 422");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertEquals(ERROR_OF_VALIDATION, errorResponse.getError(), "Не правильное описание ошибки");
        assertEquals(INVALID_YEAR_ERROR, errorResponse.getDetails()[0], "Не правильные детали ошибки");
    }

    @Test
    void addMovies_whenYearGreaterThan_MAX_YEAR_returnsError() throws Exception {
        String json = gson.toJson(new Movie(MOVIES_TITLE, MAX_YEAR + 1));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(422, response.statusCode(), "POST /movies должен вернуть 422");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertEquals(ERROR_OF_VALIDATION, errorResponse.getError(), "Не правильное описание ошибки");
        assertEquals(INVALID_YEAR_ERROR, errorResponse.getDetails()[0], "Не правильные детали ошибки");
    }

    @Test
    void addMovies_whenBigTitle_returnsError() throws Exception {
        String json = gson.toJson(new Movie("Побег".repeat(20) + "П", MOVIES_YEAR));
        HttpResponse<String> response = sendPostMoviesRequest(json);

        assertEquals(422, response.statusCode(), "POST /movies должен вернуть 422");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertEquals(ERROR_OF_VALIDATION, errorResponse.getError(), "Не правильное описание ошибки");
        assertEquals(BIG_TITLE_ERROR, errorResponse.getDetails()[0], "Не правильные детали ошибки");
    }

    @Test
    void addMovies_whenDublicateMovies_returnsError() throws Exception {
        moviesServer.getMoviesStore().addMovies(new Movie(MOVIES_TITLE, MOVIES_YEAR));
        String json = gson.toJson(new Movie(MOVIES_TITLE, MOVIES_YEAR));

        HttpResponse<String> response = sendPostMoviesRequest(json);
        assertEquals(422, response.statusCode(), "POST /movies должен вернуть 422");
        String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        ErrorResponse errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
        assertEquals(ERROR_OF_VALIDATION, errorResponse.getError(), "Не правильное описание ошибки");
        assertEquals(DUBLICATE_ERROR, errorResponse.getDetails()[0], "Не правильные детали ошибки");
    }

    @Test
    void addMovies_whenIncorrectContentType_returnsError() throws Exception {

        String json = gson.toJson(new Movie(MOVIES_TITLE, MOVIES_YEAR));
        HttpResponse<String> response = sendPostMoviesRequestWithIncorrectContentType(json);
        assertEquals(415, response.statusCode(), "POST /movies должен вернуть 415");
    }

    public HttpResponse<String> sendPostMoviesRequest(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(BASE + "/movies"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }

    public HttpResponse<String> sendPostMoviesRequestWithIncorrectContentType(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(BASE + "/movies"))
                .header("Accept", "application/json")
                .header("Content-Type", "text/plain")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; PracticumBot/1.0; +https://practicum.yandex.ru)")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        return client.send(request, responseBodyHandler);
    }
}
