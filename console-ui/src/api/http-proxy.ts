import request from '@/utils/http'

/**
 * 获取 HTTP 代理列表
 * @param params 搜索参数
 * @returns HTTP 代理列表
 */
export function fetchGetHttpProxyList(params: { keyword?: string; page: number; size: number }) {
  return request.get({
    url: '/api/proxies/http',
    params
  })
}

/**
 * 创建 HTTP 代理
 * @param data 创建参数
 * @returns 响应结果
 */
export function fetchCreateHttpProxy(data: any) {
  return request.post({
    url: '/api/proxies/http',
    params: data,
    showSuccessMessage: true
  })
}

/**
 * 更新 HTTP 代理
 * @param data 更新参数
 * @returns 响应结果
 */
export function fetchUpdateHttpProxy(data: any) {
  return request.put({
    url: '/api/proxies/http',
    params: data,
    showSuccessMessage: true
  })
}
