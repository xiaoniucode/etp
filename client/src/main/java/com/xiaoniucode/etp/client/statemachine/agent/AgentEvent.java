package com.xiaoniucode.etp.client.statemachine.agent;

public enum AgentEvent {
    START,              // 开始启动
    CONFIG_CHECKED,     // 配置检查完成
    SSL_INITIALIZED,    // SSL 初始化完成
    CONNECT_ATTEMPT,    // 尝试连接
    CONNECT_SUCCESS,    // 连接成功
    CONNECT_FAILURE,    // 连接失败
    RETRY,              // 重试
    ERROR,              // 错误
    AUTH_START,         // 开始认证
    AUTH_SUCCESS,       // 认证成功
    AUTH_FAILURE,        // 认证失败
    STOP,                // 停止
    NETWORK_ERROR,       //网络错误
    DISCONNECT,           //连接断开
    CREATE_TUNNEL_POOL,   //创建隧道池
    CREATE_NEW_CONN,      //创建单个连接
    CREATE_TUNNEL_POOL_RESP,   //创建隧道池响应

    PROXY_CREATE,          // 发送创建代理请求
    PROXY_CREATE_RESP,     // 接收创建代理响应
    PROXY_UPDATE,          // 发送更新代理请求
    PROXY_UPDATE_RESP,     // 接收更新代理响应
    PROXY_DELETE,          // 发送删除代理请求
    PROXY_DELETE_RESP,     // 接收删除代理响应
    PROXY_LIST,            // 发送查询代理列表请求
    PROXY_LIST_RESP,       // 接收代理列表响应
    PROXY_GET,             // 发送查询单个代理请求
    PROXY_GET_RESP,        // 接收单个代理响应
    PROXY_NOTIFY           // 接收服务端推送通知
}
