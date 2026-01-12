package com.xiaoniucode.etp.core.msg;

public interface Message {
    char TYPE_LOGIN = 'o';
    char TYPE_LOGIN_RESP = '1';
    char TYPE_NEW_PROXY = 'p';
    char TYPE_NEW_PROXY_RESP = '2';
    char TYPE_CLOSE_PROXY = 'c';
    char TYPE_NEW_WORK_CONN = 'w';
    char TYPE_REQ_WORK_CONN = 'r';
    char TYPE_START_WORK_CONN = 's';
    char TYPE_NEW_VISITOR_CONN = 'v';
    char TYPE_NEW_VISITOR_CONN_RESP = '3';
    char TYPE_UNREGISTER_PROXY = 'u';
    char TYPE_ERROR = 'e';
    char TYPE_PING = 'h';
    char TYPE_PONG = '4';
    char getType();
}
