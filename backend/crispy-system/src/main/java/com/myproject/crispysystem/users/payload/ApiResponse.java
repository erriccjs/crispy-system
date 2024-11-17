package com.myproject.crispysystem.users.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ApiResponse<T> {
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ApiResponse(String message) {
        this.message = message;
    }

    public ApiResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public ApiResponse(String message, String token, T data) {
        this.message = message;
        this.token = token;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
