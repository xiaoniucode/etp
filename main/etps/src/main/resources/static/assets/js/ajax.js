layui.define(['layer', 'jquery'], function (exports) {
    const layer = layui.layer;
    const $ = layui.jquery;

    class RestAPI {
        static token() {
            return localStorage.getItem('auth_token');
        }

        static headers(extra = {}) {
            const h = {};
            const token = this.token();
            if (token) h['Authorization'] = 'Bearer ' + token;
            return Object.assign(h, extra);
        }

        static request(method, url, params = {}, options = {}) {
            const loading = options.loading !== false ? layer.load(2) : 0;

            return new Promise((resolve, reject) => {
                $.ajax({
                    url,
                    type: method,
                    data: method === 'GET' ? params : JSON.stringify(params),
                    contentType: method === 'GET' ? undefined : 'application/json;charset=UTF-8',
                    headers: this.headers(options.headers),
                    timeout: options.timeout || 30000,

                    success: (res) => {
                        if (!res) {
                            const msg = '请求失败，返回数据为空';
                            if (options.silent !== true) {
                                layer.msg(msg, {icon: 2, time: 1500});
                            }
                            reject(new Error(msg));
                            return;
                        }
                        
                        if (res.code === 401) {
                            localStorage.clear();
                            layer.msg('登录已过期，请重新登录', {icon: 2});
                            setTimeout(() => {
                                window.parent.parent.location.href = '/login.html';
                            }, 800);
                            reject(new Error(res.msg || '未授权访问'));
                        } else if (res.code === 404) {
                            window.parent.parent.location.href = '/error/404.html';
                            reject(new Error('找不到访问资源'));
                        } else if (res.code === 403) {
                            window.parent.parent.location.href = '/error/403.html';
                            reject(new Error(res.msg || '没有资源访问权限'));
                        } else if (res.code === 500) {
                            const msg = res.msg || '操作失败';
                            if (options.silent !== true) {
                                layer.msg(msg, {icon: 2, time: 1500});
                            }
                            const err = new Error(msg);
                            err.response = res;
                            err.data = res.data;
                            err.code = res.code;
                            reject(err);
                        } else if (res.code === 0) {
                            resolve(res.data);
                        } else {
                            // 处理其他未知状态码
                            const msg = res.msg || '请求失败';
                            if (options.silent !== true) {
                                layer.msg(msg, {icon: 2, time: 1500});
                            }
                            const err = new Error(msg);
                            err.response = res;
                            err.data = res.data;
                            err.code = res.code;
                            reject(err);
                        }
                    },

                    error: (xhr) => {
                        // 处理HTTP状态码错误
                        if (xhr.status === 401) {
                            localStorage.clear();
                            layer.msg('登录已过期，请重新登录', {icon: 2});
                            setTimeout(() => {
                                window.parent.parent.location.href = '/login.html';
                            }, 800);
                            reject(new Error('未授权'));
                            return;
                        } else if (xhr.status === 404) {
                            window.parent.parent.location.href = '/error/404.html';
                            reject(new Error('找不到访问资源'));
                            return;
                        } else if (xhr.status === 403) {
                            window.parent.parent.location.href = '/error/403.html';
                            reject(new Error('没有资源访问权限'));
                            return;
                        } else if (xhr.status >= 500) {
                            window.parent.parent.location.href = '/error/500.html';
                            reject(new Error('服务器内部错误'));
                            return;
                        }
                        
                        // 处理网络错误
                        let msg = '网络异常';
                        if (xhr.responseJSON?.message) {
                            msg = xhr.responseJSON.message;
                        } else if (xhr.status === 0) {
                            msg = '无法连接服务器';
                        } else if (xhr.status === 400) {
                            msg = '请求参数错误';
                        } else if (xhr.status === 408) {
                            msg = '请求超时';
                        }
                        
                        if (options.silent !== true) {
                            layer.msg(msg, {icon: 2, time: 1500});
                        }
                        
                        const err = new Error(msg);
                        err.status = xhr.status;
                        err.response = xhr.responseJSON || xhr.responseText;
                        err.headers = xhr.getAllResponseHeaders();
                        reject(err);
                    },

                    complete: () => {
                        loading && layer.close(loading);
                    }
                });
            }).then(data => {
                return data;
            });
        }
    }

    const Rest = {
        get(url, params = {}, options = {}) {
            return RestAPI.request('GET', url, params, options);
        },
        post(url, data = {}, options = {}) {
            return RestAPI.request('POST', url, data, options);
        },
        put(url, data = {}, options = {}) {
            return RestAPI.request('PUT', url, data, options);
        },
        delete(url, params = {}, options = {}) {
            return RestAPI.request('DELETE', url, params, options);
        }
    };

    exports('Rest', Rest);
});
