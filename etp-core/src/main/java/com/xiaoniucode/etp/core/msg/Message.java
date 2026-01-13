package com.xiaoniucode.etp.core.msg;

public interface Message {

    byte TYPE_LOGIN                 = 0x6F;  // 'o'
    byte TYPE_LOGIN_RESP            = 0x31;  // '1'
    byte TYPE_NEW_PROXY             = 0x70;  // 'p'
    byte TYPE_NEW_PROXY_RESP        = 0x32;  // '2'
    byte TYPE_CLOSE_PROXY           = 0x63;  // 'c'
    byte TYPE_NEW_WORK_CONN         = 0x77;  // 'w'
    byte TYPE_REQ_WORK_CONN         = 0x72;  // 'r'
    byte TYPE_START_WORK_CONN       = 0x73;  // 's'
    byte TYPE_NEW_VISITOR_CONN      = 0x76;  // 'v'
    byte TYPE_NEW_VISITOR_CONN_RESP = 0x33;  // '3'
    byte TYPE_UNREGISTER_PROXY      = 0x75;  // 'u'
    byte TYPE_ERROR                 = 0x65;  // 'e'
    byte TYPE_KICKOUT_CLIENT        = 0x6B;  // 'k'
    byte TYPE_PING                  = 0x68;  // 'h'
    byte TYPE_PONG                  = 0x34;  // '4'

    byte getType();
}
