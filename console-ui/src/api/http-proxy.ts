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
