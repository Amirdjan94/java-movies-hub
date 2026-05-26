package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    MoviesStore moviesStore;
    private Gson gson = new Gson();

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getQuery(),
                exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_ALL_MOVIES:
                handleGetMovies(exchange);
                break;
            case POST_ADD_MOVIES:
                if (checkContentType(exchange)) handleAddMovies(exchange);
                break;
            case GET_MOVIE_BY_ID:
                if (checkContentType(exchange)) handleGetMoviesById(exchange);
                break;
            case GET_MOVIES_BY_YEAR:
                if (checkContentType(exchange)) handleGetMoviesByYear(exchange);
                break;
            case DELETE:
                if (checkContentType(exchange)) handleDeleteMoviesById(exchange);
                break;
            case UNKNOWN:
                handleMethodNotAllowed(exchange);
                break;
        }
    }

    private boolean checkContentType(HttpExchange exchange) throws IOException {
        if ((exchange.getRequestHeaders().get("Content-Type") != null) &&
                !(exchange.getRequestHeaders().get("Content-Type").contains(CT_JSON))) {
            sendUnsupportedMediaType(exchange);
            return false;
        }
        return true;
    }

    private Endpoint getEndpoint(String queryString, String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (requestMethod.equalsIgnoreCase("GET")) {
            if (queryString != null) {
                if ((pathParts.length == 2) && (pathParts[1].equalsIgnoreCase("movies"))) {
                    return Endpoint.GET_MOVIES_BY_YEAR;
                } else return Endpoint.UNKNOWN;
            } else if ((pathParts.length == 2) && (pathParts[1].equalsIgnoreCase("movies"))) {
                return Endpoint.GET_ALL_MOVIES;
            } else if ((pathParts.length == 3)) {
                return Endpoint.GET_MOVIE_BY_ID;
            } else return Endpoint.UNKNOWN;
        } else if (requestMethod.equalsIgnoreCase("POST")) {
            return Endpoint.POST_ADD_MOVIES;
        } else if (requestMethod.equalsIgnoreCase("DELETE")) {
            return Endpoint.DELETE;
        } else {
            return Endpoint.UNKNOWN;
        }
    }

    private void handleGetMovies(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, gson.toJson(moviesStore.getMoviesMap().values()));
    }

    private void handleAddMovies(HttpExchange exchange) throws IOException {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Movie movie = gson.fromJson(body, Movie.class);
            String[] errorArray = movieValidator(movie);
            if (errorArray.length != 0) {
                ErrorResponse errorResponse = new ErrorResponse(errorArray, "Ошибка валидации");
                sendJson(exchange, 422, gson.toJson(errorResponse));
            } else if (moviesStore.getMoviesMap().containsValue(movie)) {
                ErrorResponse errorResponse = new ErrorResponse(new String[]{"фильм был добавлен ранее"}, "Ошибка валидации");
                sendJson(exchange, 422, gson.toJson(errorResponse));
            } else {
                moviesStore.addMovies(movie);
                sendJson(exchange, 201, gson.toJson(moviesStore.getMoviesMap().get(moviesStore.movieID - 1)));
            }
        } catch (IOException e) {
            sendJson(exchange, 400, "Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, параметры запроса и повторите попытку");
        }
    }

    private void handleGetMoviesById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int moviesID = Integer.parseInt(pathParts[2]);
            if (moviesStore.getMoviesMap().containsKey(moviesID)) {
                sendJson(exchange, 200, gson.toJson(moviesStore.getMoviesMap().get(moviesID)));
            } else {
                sendJson(exchange, 404, gson.toJson("Фильм не найден"));
            }
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, gson.toJson("Некорректный ID"));
        }
    }

    private void handleDeleteMoviesById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int moviesID = Integer.parseInt(pathParts[2]);
            if (moviesStore.getMoviesMap().containsKey(moviesID)) {
                moviesStore.getMoviesMap().remove(moviesID);
                sendNoContent(exchange);
            } else {
                sendJson(exchange, 404, gson.toJson("Фильм не найден"));
            }
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, gson.toJson("Некорректный ID"));
        }
    }

    private void handleGetMoviesByYear(HttpExchange exchange) throws IOException {
        String[] queryParts = exchange.getRequestURI().getQuery().split("&");
        if (queryParts.length != 1) {
            sendJson(exchange, 400, gson.toJson("Некорректный параметр запроса"));
        } else {
            try {
                int year = Integer.parseInt(queryParts[0].substring(5));
                List<Movie> lisMovie = getMovieStorByYear(year);
                sendJson(exchange, 200, gson.toJson(lisMovie));
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, gson.toJson("Некорректный параметр запроса — 'year'"));
            }
        }
    }

    private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendNotAllowed(exchange);
    }

    private String[] movieValidator(Movie movie) {
        List<String> errorList = new ArrayList<>();
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errorList.add("название не должно быть пустым");
        } else if (movie.getTitle().trim().length() > 100) {
            errorList.add("длина названия не должно превышать 100 символов");
        }
        if (movie.getYear() == 0 || movie.getYear() < 1888 || movie.getYear() > (LocalDate.now().getYear() + 1)) {
            errorList.add("год должен быть между 1888 и 2026");
        }
        return errorList.toArray(new String[0]);
    }

    private List<Movie> getMovieStorByYear(int year) {
        List<Movie> lisMovie = new ArrayList<>();
        for (int movieId : moviesStore.getMoviesMap().keySet()) {
            if (moviesStore.getMoviesMap().get(movieId).getYear() == year) {
                lisMovie.add(moviesStore.getMoviesMap().get(movieId));
            }
        }
        return lisMovie;
    }
}


