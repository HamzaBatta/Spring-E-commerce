package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.resources.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        var rt = returnType.getParameterType();
        if (ApiResponse.class.isAssignableFrom(rt)) return false;
        if (String.class.equals(rt)) return false;
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) {
            return ApiResponse.success(null, "Operation successful");
        }

        if (body instanceof ApiResponse) return body;

        if (body instanceof String || MediaType.TEXT_PLAIN.includes(selectedContentType)) {
            return body;
        }

        int status = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse) {
            HttpServletResponse servletResp = ((ServletServerHttpResponse) response).getServletResponse();
            status = servletResp.getStatus();
        }

        if (status >= 400) {
            return ApiResponse.error("Error", body);
        }

        // Handle Spring Data Page responses and add pagination meta
        if (body instanceof Page) {
            Page<?> page = (Page<?>) body;

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", page.getTotalElements());
            pagination.put("count", page.getNumberOfElements());
            pagination.put("per_page", page.getSize());
            pagination.put("current_page", page.getNumber() + 1);
            pagination.put("total_pages", page.getTotalPages());

            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(request.getURI());
            String first = builder.replaceQueryParam("page", 1).replaceQueryParam("size", page.getSize()).toUriString();
            String last = builder.replaceQueryParam("page", page.getTotalPages() == 0 ? 1 : page.getTotalPages()).replaceQueryParam("size", page.getSize()).toUriString();
            String next = page.hasNext() ? builder.replaceQueryParam("page", page.getNumber() + 2).replaceQueryParam("size", page.getSize()).toUriString() : null;
            String previous = page.hasPrevious() ? builder.replaceQueryParam("page", page.getNumber()).replaceQueryParam("size", page.getSize()).toUriString() : null;

            Map<String, Object> links = new HashMap<>();
            links.put("first", first);
            links.put("last", last);
            links.put("next", next);
            links.put("previous", previous);

            pagination.put("links", links);

            ApiResponse<Object> resp = ApiResponse.success(page.getContent(), "Operation successful");
            resp.setPagination(pagination);
            return resp;
        }

        return ApiResponse.success(body, "Operation successful");
    }
}
