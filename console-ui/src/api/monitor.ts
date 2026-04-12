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