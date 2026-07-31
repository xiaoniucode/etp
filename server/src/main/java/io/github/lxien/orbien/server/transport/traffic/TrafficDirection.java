package io.github.lxien.orbien.server.transport.traffic;

/**
 * 以访客视角描述流量方向
 */
public enum TrafficDirection {
    /**
     * 访客上传：Visitor -> Backend
     */
    UPLOAD,
    /**
     * 访客下载：Backend -> Visitor
     */
    DOWNLOAD
}
