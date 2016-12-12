package com.kilogramm.mattermost.model.error;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 27.07.2016.
 */
public class HttpError {

    @SerializedName("id")
    private String id;
    @SerializedName("message")
    private String message;
    @SerializedName("detailed_error")
    private String detailedError;
    @SerializedName("request_id")
    private String requestId;
    @SerializedName("status_code")
    private Integer statusCode;
    @SerializedName("is_oauth")
    private Boolean isOauth;
    @SerializedName("error")
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetailedError() {
        return detailedError;
    }

    public void setDetailedError(String detailedError) {
        this.detailedError = detailedError;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getOauth() {
        return isOauth;
    }

    public void setOauth(Boolean oauth) {
        isOauth = oauth;
    }
}
