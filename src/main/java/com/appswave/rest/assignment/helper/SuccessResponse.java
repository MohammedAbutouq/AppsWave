package com.appswave.rest.assignment.config;

public class SuccessResponse {
    private String message;
    private Object object ;


    public SuccessResponse(String message) {
        this.message = message;
    }

    public SuccessResponse(String message, Object object) {
        this.message = message;
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
