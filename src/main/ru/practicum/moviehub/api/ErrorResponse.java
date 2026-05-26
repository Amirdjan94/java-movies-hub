package ru.practicum.moviehub.api;

public class ErrorResponse {
    String error;
    String [] details;

    public ErrorResponse(String[] details, String error) {
        this.details = details;
        this.error = error;
    }

    public String[] getDetails() {
        return details;
    }

    public void setDetails(String[] details) {
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}