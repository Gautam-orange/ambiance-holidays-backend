package com.ambianceholidays.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final PageMeta meta;
    private final ApiError error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(T data, PageMeta meta) {
        return ApiResponse.<T>builder().success(true).data(data).meta(meta).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder().code(code).message(message).build())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String field) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder().code(code).message(message).field(field).build())
                .build();
    }
}
