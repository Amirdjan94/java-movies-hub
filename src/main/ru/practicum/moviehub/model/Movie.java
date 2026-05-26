package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private String title;
    private Integer year; // TODO: Добавить валидацию поля
    private int Id;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "{" +
                "Id=" + Id +
                ", Название фильма ='" + title + '\'' +
                ", Год =" + year +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(title, movie.title) && Objects.equals(year, movie.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }
}