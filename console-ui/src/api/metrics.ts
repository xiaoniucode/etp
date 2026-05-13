import request from '@/utils/http'

/**
 * 获取流量统计数据
 * @param proxyId 代理ID
 * @returns 流量统计数据
 */
export function fetchGetMetrics(proxyId: string) {
  return request.get<Api.Metrics.MetricsDTO>({
    url: `/api/metrics/${proxyId}`
  })
}

/**
 * 获取所有代理的流量统计数据（分页）
 * @param params 分页参数
 * @returns 流量统计数据分页列表
 */
export function fetchGetMetricsList(params: Api.Common.CommonSearchParams) {
  return request.get<Api.Common.PaginatedResponse<Api.Metrics.MetricsItemDTO>>({
    url: '/api/metrics/list',
    params
  })
}

/**
 * 获取24小时流量统计数据
 * @returns 24小时流量统计数据
 */
export function fetchGet24hMetrics() {
  return request.get<Api.Metrics.Metrics24LineDTO>({
    url: '/api/metrics/24h'
  })
}
