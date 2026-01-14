package com.xiaoniucode.etp.server.web.common;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author liuxin
 */
public final class ResponseEntity {

    private static final int OK_CODE = 0;
    private static final String OK_MSG = "成功";

    private static final int ERROR_CODE = -1;
    private static final String ERROR_MSG = "失败";

    private final JSONObject json;

    private ResponseEntity(int code, String message, Object data) {
        this.json = new JSONObject();
        this.json.put("code", code);
        this.json.put("message", message != null ? message : (code == OK_CODE ? OK_MSG : ERROR_MSG));

        if (data != null) {
            if (data instanceof JSONObject || data instanceof JSONArray || data instanceof String
                || data instanceof Number || data instanceof Boolean || data == JSONObject.NULL) {
                this.json.put("data", data);
            } else {
                this.json.put("data", new JSONObject(data));
            }
        } else {
            this.json.put("data", JSONObject.NULL);
        }
    }

    public static ResponseEntity ok() {
        return new ResponseEntity(OK_CODE, OK_MSG, null);
    }

    public static ResponseEntity ok(Object data) {
        return new ResponseEntity(OK_CODE, OK_MSG, data);
    }

    public static ResponseEntity ok(String message) {
        return new ResponseEntity(OK_CODE, message, null);
    }

    public static ResponseEntity ok(String message, Object data) {
        return new ResponseEntity(OK_CODE, message, data);
    }

    public static ResponseEntity ok(JSONArray array) {
        return new ResponseEntity(OK_CODE, OK_MSG, array);
    }

    public static ResponseEntity error() {
        return new ResponseEntity(ERROR_CODE, ERROR_MSG, null);
    }

    public static ResponseEntity error(String message) {
        return new ResponseEntity(ERROR_CODE, message, null);
    }

    public static ResponseEntity error(int code, String message) {
        return new ResponseEntity(code, message, null);
    }

    public static ResponseEntity error(int code, String message, Object data) {
        return new ResponseEntity(code, message, data);
    }

    public JSONObject getJson() {
        return json;
    }

    public String toJson() {
        return json.toString();
    }

    @Override
    public String toString() {
        return json.toString();
    }

    public String toString(int indentFactor) {
        return json.toString(indentFactor);
    }
}
