layui.define(['layer', 'jquery'], function (exports) {
    const layer = layui.layer;
    const $ = layui.jquery;

    // ==================== 核心：Token 自动刷新逻辑（只加这几行）=================
    let isRefreshing = false;
    let retryQueue = []; // 存放等待重试的请求

    // 执行队列里的所有重试
    function processRetryQueue(newToken) {
        retryQueue.forEach(item => {
            item.resolve(newToken);
        });
        retryQueue = [];
    }
    // =====================================================================
// ====================== 刷新 Token 时自动携带旧 Token ======================
    function refreshTokenWithOld() {
        layer.msg('flush，', {icon: 2});

        const oldToken = localStorage.getItem('auth_token');
        if (!oldToken) {
            // 连旧 token 都没有，直接跳登录
            localStorage.clear();
            layer.msg('登录已过期，请重新登录', {icon: 2});
            setTimeout(() => {
                window.parent.parent.location.href = '/login.html';
            }, 1000);
            return Promise.reject(new Error('无旧Token'));
        }

        return new Promise((resolve, reject) => {
            $.ajax({
                url: '/api/user/flush-token',
                type: 'PUT',
                contentType: 'application/json;charset=UTF-8',
                headers: {
                    'Authorization': 'Bearer ' + oldToken   // 关键：携带旧 Token
                },
                timeout: 10000,
                success: (res) => {
                    if (res && res.code === 0) {
                        const newToken = res.data.auth_token || res.data.token;
                        localStorage.setItem('auth_token', newToken);
                        resolve(newToken);
                    } else {
                        reject(new Error(res?.message || '刷新失败'));
                    }
                },
                error: () => reject(new Error('刷新接口异常'))
            });
        });
    }
// =====================================================================
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
                                layer.msg(msg, {icon: 2, time: 1000});
                            }
                            const err = new Error(msg);
                            err.response = res;
                            err.data = res?.data;
                            err.code = res?.code;
                            reject(err);
                        }
                    },

                    error: (xhr) => {
                        // ================ 核心：401 时自动刷新 Token（携带旧 Token）================
                        if (xhr.status === 401) {
                            if (isRefreshing) {
                                // 正在刷新 → 排队
                                const waiter = new Promise((res) => {
                                    retryQueue.push({ resolve: res });
                                });
                                waiter.then(() => {
                                    this.request(method, url, params, options).then(resolve).catch(reject);
                                });
                                return;
                            }

                            isRefreshing = true;
                            layer.msg('登录状态已过期，自动续期中...', {icon: 16, shade: 0.3, time: 0});

                            refreshTokenWithOld().then((newToken) => {
                                isRefreshing = false;
                                layer.closeAll();
                                layer.msg('自动续期成功', {icon: 1, time: 800});
                                processRetryQueue(newToken);
                                // 重试原请求
                                this.request(method, url, params, options).then(resolve).catch(reject);
                            }).catch(() => {
                                isRefreshing = false;
                                processRetryQueue();
                                localStorage.clear();
                                layer.msg('登录已过期，请重新登录', {icon: 2});
                                setTimeout(() => {
                                    window.parent.parent.location.href = '/login.html';
                                }, 1200);
                                reject(new Error('未授权'));
                            });

                            return; // 阻止你原来的 401 跳转逻辑
                        }
                        // =====================================================================

                        // 你原来的其他错误处理完全不动
                        let msg = '网络异常';
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
                    return data; // 你原来的 .then 完全保留
                });
        }
    }

    const Rest = {
        get(url, params = {}, options = {}) { return RestAPI.request('GET', url, params, options); },
        post(url, data = {}, options = {}) { return RestAPI.request('POST', url, data, options); },
        put(url, data = {}, options = {}) { return RestAPI.request('PUT', url, data, options); },
        delete(url, params = {}, options = {}) { return RestAPI.request('DELETE', url, params, options); }
    };

    exports('Rest', Rest);
});
