package com.strollimo.android.models.network.requests;

import com.google.gson.annotations.Expose;

public class BaseRequest {
    @Expose
    private RequestHeader header;
    @Expose
    private String action;

    public BaseRequest(RequestHeader header, String action) {
        this.header = header;
        this.action = action;
    }
}
