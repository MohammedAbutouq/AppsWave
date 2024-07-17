package com.appswave.rest.assignment.dto;

import com.appswave.rest.assignment.enums.Status;

public class NewsStatusUpdateRequest {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
