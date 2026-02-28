package com.xiaoniucode.etp.server.web.common;

import java.util.HashMap;
public class Ajax extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR = 1;
    private Integer code;
    private String msg;
    private Object data;

    private Ajax() {
        this.code = CODE_SUCCESS;
        this.msg = "操作成功";
        this.data = null;
        updateMap();
    }

    private Ajax(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        updateMap();
    }

    private void updateMap() {
        put("code", code);
        put("msg", msg);
        put("data", data);
    }

    public static Ajax success() {
        return new Ajax();
    }

    public static Ajax success(Object data) {
        return new Ajax(CODE_SUCCESS, "操作成功", data);
    }

    public static Ajax success(String msg, Object data) {
        return new Ajax(CODE_SUCCESS, msg, data);
    }

    public static Ajax error() {
        return new Ajax(CODE_ERROR, "操作失败", null);
    }

    public static Ajax error(String msg) {
        return new Ajax(CODE_ERROR, msg, null);
    }

    public static Ajax error(int code, String msg) {
        return new Ajax(code, msg, null);
    }

    public static Ajax error(int code, String msg, Object data) {
        return new Ajax(code, msg, data);
    }

    @Override
    public Ajax put(String key, Object value) {
        super.put(key, value);
        switch (key) {
            case "code":
                this.code = (Integer) value;
                break;
            case "msg":
                this.msg = (String) value;
                break;
            case "data":
                this.data = value;
                break;
        }
        return this;
    }

    public boolean isSuccess() {
        return CODE_SUCCESS == code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }
}
