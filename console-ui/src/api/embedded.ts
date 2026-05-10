import request from '@/utils/http'

/**
 * 获取隧道列表
 * @param params 查询参数
 * @returns 隧道列表
 */
export function fetchGetEmbeddedList(params: { page: number; size: number }) {
  return request.get<Api.Common.PaginatedResponse<Api.Embedded.TunnelListDTO>>({
    url: '/api/embedded',
    params
  })
}

/**
 * 获取隧道详情
 * @param proxyId 隧道ID
 * @returns 隧道详情
 */
export function fetchGetEmbeddedDetail(proxyId: string) {
  return request.get<Api.Embedded.TunnelDetailDTO>({
    url: `/api/embedded/${proxyId}`
  })
}

/**
 * 删除隧道
 * @param proxyId 隧道ID
 * @returns 删除结果
 */
export function fetchDeleteEmbedded(proxyId: string) {
  return request.del({
    url: `/api/embedded/${proxyId}`
  })
}
