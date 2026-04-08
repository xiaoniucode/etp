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
