import request from '@/utils/http'

/**
 * 获取服务器信息，用于监控
 * @returns 服务器信息
 */
export function fetchGetServerInfo() {
  return request.get<Api.Monitor.ServerInfo>({
    url: '/api/monitor/server-info'
  })
}

/**
 * 获取仪表盘摘要信息
 * @returns 仪表盘摘要信息
 */
export function fetchGetDashboardSummary() {
  return request.get<Api.Monitor.DashboardSummary>({
    url: '/api/monitor/get-dashboard-summary'
  })
}

/**
 * 获取代理协议统计信息
 * @returns 代理协议统计信息
 */
export function fetchGetProxyProtocolStats() {
  return request.get<Api.Monitor.ProxyProtocolCountDTO>({
    url: '/api/monitor/get-proxy-protocol-stats'
  })
}
