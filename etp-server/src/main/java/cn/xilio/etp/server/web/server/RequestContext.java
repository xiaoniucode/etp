package com.xiaoniucode.etp.server.web.server;

import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.util.*;

/**
 * HTTP 请求上下文对象
 * 包含请求行、请求头、请求参数、请求体等所有信息
 *
 * @author liuxin
 */
public class RequestContext {
    private final ChannelHandlerContext ctx;
    private FullHttpResponse httpResponse;
    private final FullHttpRequest request;
    private HttpMethod method;
    private String uri;
    private HttpVersion version;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, Object> queryParams = new HashMap<>();
    private final Map<String, String> formParams = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private String requestBody;
    private byte[] responseData;
    private String responseContentType;
    private boolean abort = false;
    private String responseContent;
    private HttpResponseStatus responseStatus = HttpResponseStatus.OK;
    private final Map<String, String> responseHeaders = new HashMap<>();

    public RequestContext(ChannelHandlerContext ctx, FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
        parseRequest();
    }

    /**
     * 解析 HTTP 请求的各个组成部分
     */
    private void parseRequest() {
        // 解析请求行
        this.method = request.method();
        this.uri = request.uri();
        this.version = request.protocolVersion();
        // 解析请求头
        parseHeaders();
        // 解析查询参数 (URL 参数)
        parseQueryParams();
        // 解析请求体
        parseRequestBody();
        // 解析表单参数 (如果是表单提交)
        if (isFormUrlEncoded()) {
            parseFormParams();
        }
    }

    /**
     * 解析请求头
     */
    private void parseHeaders() {
        HttpHeaders httpHeaders = request.headers();
        for (Map.Entry<String, String> entry : httpHeaders) {
            headers.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * 解析 URL 查询参数
     */
    private void parseQueryParams() {
        String[] parts = uri.split("\\?", 2);
        if (parts.length > 1) {
            String queryString = parts[1];
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    /**
     * 解析请求体
     */
    private void parseRequestBody() {
        if (request.content() != null && request.content().readableBytes() > 0) {
            this.requestBody = request.content().toString(CharsetUtil.UTF_8);
        }
    }

    /**
     * 解析表单参数 (application/x-www-form-urlencoded)
     */
    private void parseFormParams() {
        if (requestBody != null && !requestBody.isEmpty()) {
            String[] params = requestBody.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    formParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    /**
     * 判断是否是表单提交
     */
    public boolean isFormUrlEncoded() {
        String contentType = getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }

    /**
     * 判断是否是 JSON 请求
     */
    public boolean isJsonRequest() {
        String contentType = getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * 获取请求头
     */
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * 获取所有请求头
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    /**
     * 获取查询参数
     */
    public Object getQueryParam(String name) {
        return queryParams.get(name);
    }

    /**
     * 获取所有查询参数
     */
    public Map<String, Object> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    /**
     * 获取表单参数
     */
    public String getFormParam(String name) {
        return formParams.get(name);
    }

    /**
     * 获取所有表单参数
     */
    public Map<String, String> getFormParams() {
        return new HashMap<>(formParams);
    }

    /**
     * 获取请求体内容
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * 获取请求方法
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * 获取请求 URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * 获取 HTTP 版本
     */
    public HttpVersion getVersion() {
        return version;
    }

    /**
     * 获取 ChannelHandlerContext
     */
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    /**
     * 获取原始请求对象
     */
    public FullHttpRequest getRequest() {
        return request;
    }

    /**
     * 设置属性
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * 获取属性
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * 中止请求并设置响应
     */
    public void abortWithResponse(HttpResponseStatus status, String responseContext) {
        this.abort = true;
        this.responseStatus = status;
        this.responseContent = responseContext;
    }

    public void abortWithResponse(String responseContext) {
        this.abort = true;
        this.responseContent = responseContext;
    }

    public boolean isAborted() {
        return abort;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseData(byte[] data, String contentType) {
        this.responseData = data;
        this.responseContentType = contentType;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseJson(String responseContent) {
        this.responseContent = responseContent;
    }

    public HttpResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(HttpResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public FullHttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(FullHttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public void addResponseHeader(String name, String value) {
        this.responseHeaders.put(name, value);
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

}
