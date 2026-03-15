package com.campus.trading.module.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;

@ControllerAdvice(basePackages = "com.campus.trading.module.web.controller")
public class WebUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                              HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
            .put("errorMessage", "上传文件总大小不能超过 30MB，单张图片请控制在 5MB 以内。");
        return "redirect:" + resolveRedirectTarget(request);
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                String path = uri.getRawPath();
                String query = uri.getRawQuery();
                if (path != null && !path.isBlank()) {
                    return query == null || query.isBlank() ? path : path + "?" + query;
                }
            } catch (IllegalArgumentException ignored) {
                // Fall back to current request URI.
            }
        }

        String requestUri = request.getRequestURI();
        return requestUri == null || requestUri.isBlank() ? "/" : requestUri;
    }
}
