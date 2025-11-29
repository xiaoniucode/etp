package cn.xilio.etp.server.web;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


/**
 * @author liuxin
 */
public class ResponseEntity implements Serializable {
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private Object data;
    private final static int OK_CODE = 0;
    private final static int ERROR_CODE = 0;
    private final static String OK_MSG = "成功";
    private final static String ERROR_MSG = "失败";

    public ResponseEntity() {
    }

    public ResponseEntity(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseEntity of(int code, String message, Object data) {
        return new ResponseEntity(code, message, data);
    }

    public static ResponseEntity of(int code, String message) {
        return new ResponseEntity(code, message, null);
    }

    public static ResponseEntity of(Object data) {
        return new ResponseEntity(OK_CODE, OK_MSG, data);
    }

    public static ResponseEntity of(String msg, Object data) {
        return new ResponseEntity(OK_CODE, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
