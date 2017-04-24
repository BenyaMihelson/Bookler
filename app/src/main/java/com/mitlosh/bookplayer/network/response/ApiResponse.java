package com.mitlosh.bookplayer.network.response;

public class ApiResponse<T extends BaseData> {

    private T data;
    private String status;

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return status != null && status.equals("success");
    }
}
