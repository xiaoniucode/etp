package cn.xilio.etp.server.web.server;

import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源处理器
 * @author liuxin
 */
public class StaticResourceHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHandler.class);

    /**
     * 支持的静态文件类型
     */
    private static final String[] STATIC_PATHS = {"/static/", "/template/"};
    private static final String WEB_ROOT = "src/main/resources";

    /**
     * 文件类型映射
     */
    private static final String[][] MIME_TYPES = {
        {".html", "text/html; charset=utf-8"},
        {".htm", "text/html; charset=utf-8"},
        {".css", "text/css; charset=utf-8"},
        {".js", "application/javascript; charset=utf-8"},
        {".json", "application/json; charset=utf-8"},
        {".png", "image/png"},
        {".jpg", "image/jpeg"},
        {".jpeg", "image/jpeg"},
        {".gif", "image/gif"},
        {".ico", "image/x-icon"},
        {".svg", "image/svg+xml"},
        {".txt", "text/plain; charset=utf-8"},
        {".pdf", "application/pdf"}
    };

    /**
     * 检查是否为静态资源请求
     */
    public static boolean isStaticResourceRequest(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return true;
        }

        for (String staticPath : STATIC_PATHS) {
            if (path.startsWith(staticPath)) {
                return true;
            }
        }

        // 检查文件扩展名
        for (String[] mime : MIME_TYPES) {
            if (path.endsWith(mime[0])) {
                return true;
            }
        }

        return false;
    }

    /**
     * 处理静态资源请求
     */
    public static void handleStaticResource(RequestContext context) {
        String path = getNormalPath(context.getRequest().uri());

        try {
            // 处理根路径，返回首页
            if ("/".equals(path) || path.isEmpty()) {
                serveIndexPage(context);
                return;
            }

            // 获取资源文件路径
            String resourcePath = getResourcePath(path);
            byte[] fileContent = readResourceFile(resourcePath);

            if (fileContent == null) {
                context.abortWithResponse(HttpResponseStatus.NOT_FOUND,
                    "{\"error\":\"资源不存在\",\"path\":\"" + path + "\"}");
                return;
            }

            // 设置响应
            String mimeType = getMimeType(path);
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                io.netty.buffer.Unpooled.copiedBuffer(fileContent)
            );

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileContent.length);
            // 缓存控制
            setCacheHeaders(response, path);
            context.setHttpResponse(response);

        } catch (Exception e) {
            logger.error("处理静态资源失败: {}", path, e);
            context.abortWithResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                "{\"error\":\"服务器内部错误\"}");
        }
    }

    /**
     * 服务首页
     */
    private static void serveIndexPage(RequestContext context) {
        String[] indexFiles = {"index.html", "index.htm"};
        for (String indexFile : indexFiles) {
            String resourcePath = "template/" + indexFile;
            byte[] fileContent = readResourceFile(resourcePath);

            if (fileContent != null) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    io.netty.buffer.Unpooled.copiedBuffer(fileContent)
                );
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileContent.length);
                context.setHttpResponse(response);
                return;
            }
        }
    }

    /**
     * 获取资源文件路径
     */
    private static String getResourcePath(String requestPath) {
        // 移除开头的斜杠
        if (requestPath.startsWith("/")) {
            requestPath = requestPath.substring(1);
        }

        // 处理绝对路径引用
        if (requestPath.startsWith("static/") || requestPath.startsWith("template/")) {
            return requestPath;
        }

        // 如果请求的是模板目录，但路径中没有指定，自动添加到template目录
        if (!requestPath.startsWith("static/") && !requestPath.startsWith("template/")) {
            // 根据文件类型判断放置的目录
            if (requestPath.endsWith(".html") || requestPath.endsWith(".htm")) {
                return "template/" + requestPath;
            } else {
                return "static/" + requestPath;
            }
        }

        return requestPath;
    }

    /**
     * 读取资源文件
     */
    private static byte[] readResourceFile(String resourcePath) {
        try {
            // 优先从文件系统读取（开发环境）
            Path filePath = Paths.get(WEB_ROOT, resourcePath);
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                return Files.readAllBytes(filePath);
            }
            // 从classpath读取（打包后环境）
            try (InputStream inputStream = StaticResourceHandler.class.getClassLoader()
                    .getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    return inputStream.readAllBytes();
                }
            }
            return null;
        } catch (IOException e) {
            logger.warn("读取资源文件失败: {}", resourcePath, e);
            return null;
        }
    }

    /**
     * 获取MIME类型
     */
    private static String getMimeType(String path) {
        for (String[] mime : MIME_TYPES) {
            if (path.endsWith(mime[0])) {
                return mime[1];
            }
        }
        return "application/octet-stream";
    }

    /**
     * 设置缓存头
     */
    private static void setCacheHeaders(FullHttpResponse response, String path) {
        // 静态资源缓存策略
        if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") ||
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") ||
            path.endsWith(".ico") || path.endsWith(".svg")) {
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3600");
        } else {
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        }
    }

    /**
     * 规范化路径
     */
    private static String getNormalPath(String uri) {
        int idx = uri.indexOf('?');
        return idx == -1 ? uri : uri.substring(0, idx);
    }
}
