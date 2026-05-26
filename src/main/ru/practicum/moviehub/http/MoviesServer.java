package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {

    private final HttpServer httpServer;
    private MoviesStore moviesStore;

    public MoviesServer(MoviesStore moviesStore, int port) {
        this.moviesStore = moviesStore;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/movies", new MoviesHandler(moviesStore));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public MoviesStore getMoviesStore() {
        return moviesStore;
    }

    public void start() {
        httpServer.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        httpServer.stop(2);
        System.out.println("Сервер остановлен");
    }

}