package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;

public class MoviesStore {
    private static HashMap<Integer, Movie> moviesMap = new HashMap<>();
    public static Integer movieID = 1;

    public void addMovies(String title, int year) {
        Movie movie = new Movie(title, year);
        movie.setId(movieID);
        moviesMap.put(movieID, movie);
        movieID++;
    }

    public void addMovies(Movie movie) {
        moviesMap.put(movieID, movie);
        movie.setId(movieID);
        movieID++;
    }

    public static HashMap<Integer, Movie> getMoviesMap() {
        return moviesMap;
    }

    public static void setMoviesMap(HashMap<Integer, Movie> moviesMap) {
        MoviesStore.moviesMap = moviesMap;
    }

    public void clearStore() {
        moviesMap = new HashMap<>();
        movieID = 1;
    }
}