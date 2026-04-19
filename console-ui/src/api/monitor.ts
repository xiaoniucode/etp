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
