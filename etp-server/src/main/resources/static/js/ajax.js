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
                        if (res && res.code === 0) {
                            resolve(res.data);
                        } else {
                            const msg = res?.message || '操作失败';
                            if (options.silent !== true) {
                                layer.msg(msg, {icon: 2});
                            }
                            const err = new Error(msg);
                            err.response = res;
                            err.data = res?.data;
                            err.code = res?.code;
                            reject(err);
                        }
                    },

                    error: (xhr) => {
                        let msg = '网络异常';
                        if (xhr.status === 401) {
                            localStorage.clear();
                            layer.msg('登录已过期，正在跳转...', {icon: 2});
                            setTimeout(() => {
                                window.parent.parent.location.href = '/login.html';
                            }, 1000);
                            reject(new Error('未授权'));
                            return;
                        }

                        if (xhr.responseJSON?.message) {
                            msg = xhr.responseJSON.message;
                        } else if (xhr.status === 0) {
                            msg = '无法连接服务器';
                        } else if (xhr.status >= 500) {
                            msg = '服务器开小差了';
                        }

                        if (options.silent !== true) {
                            layer.msg(msg, {icon: 2});
                        }
                        reject(new Error(msg));
                    },

                    complete: () => {
                        loading && layer.close(loading);
                    }
                });
            })
                .then(data => {
                    return data;
                })
                .catch(err => {
                    // 统一拦截所有错误（业务 + 网络）
                    if (!options.quiet) {
                        console.warn('[RestAPI] 请求失败已自动处理', url, err);
                    }
                    // 返回一个永远是 null 的“兜底值”，让调用链继续走下去，不会因为未 catch 而报错,调用方即使不写 catch，也不会崩溃
                    return null;
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
