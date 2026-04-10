import request from '@/utils/http'

/**
 * 获取 TCP 代理列表
 * @param params 搜索参数
 * @returns TCP 代理列表
 */
export function fetchGetTcpProxyList(params: { keyword?: string; page: number; size: number }) {
  return request.get({
    url: '/api/proxies/tcp',
    params
  })
}

/**
 * 获取 TCP 代理详情
 * @param id 代理ID
 * @returns TCP 代理详情
 */
export function fetchGetTcpProxyById(id: string) {
  return request.get({
    url: `/api/proxies/tcp/${id}`
  })
}

/**
 * 创建 TCP 代理
 * @param data 创建参数
 * @returns 响应结果
 */
export function fetchCreateTcpProxy(data: any) {
  return request.post({
    url: '/api/proxies/tcp',
    params: data,
    showSuccessMessage: true
  })
}

/**
 * 更新 TCP 代理
 * @param data 更新参数
 * @returns 响应结果
 */
export function fetchUpdateTcpProxy(data: any) {
  return request.put({
    url: '/api/proxies/tcp',
    params: data,
    showSuccessMessage: true
  })
}

/**
 * 批量删除 TCP 代理
 * @param data 删除参数
 * @returns 响应结果
 */
export function fetchBatchDeleteTcpProxy(data: { ids: string[] }) {
  return request.del({
    url: '/api/proxies',
    data,
    showSuccessMessage: true
  })
}
