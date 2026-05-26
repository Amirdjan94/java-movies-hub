package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    private static HashMap<Integer, Movie> moviesMap = new HashMap<>();
    public static Integer movieID = 1;

    public void addMovies(String title, int year) {
        Movie movie = new Movie(title, year);
        movie.setId(movieID);
        moviesMap.put(movieID, movie);
        movieID++;
    }

    public boolean addMovies(Movie movie) {
        if (!moviesMap.values().contains(movie)) {
            moviesMap.put(movieID, movie);
            movie.setId(movieID);
            movieID++;
            return true;
        }
        return false;
    }

    public boolean deleteMovies(int id) {
        if (moviesMap.containsKey(id)) {
            moviesMap.remove(id);
            return true;
        }
        return false;
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

    public Optional<Movie> getMovieById(int id) {
        if (moviesMap.containsKey(id)) {
            return Optional.of(moviesMap.get(id));
        }
        return Optional.empty();
    }

    public List<Movie> getMovieListByYear(int year) {
        List<Movie> listMovie = new ArrayList<>();
        for (int movieId : moviesMap.keySet()) {
            if (moviesMap.get(movieId).getYear() == year) {
                listMovie.add(moviesMap.get(movieId));
            }
        }
        return listMovie;
    }

}