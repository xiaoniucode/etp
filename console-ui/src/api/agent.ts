import request from '@/utils/http'

/**
 * 获取客户端列表
 * @param params 查询参数
 * @returns 客户端列表
 */
export function fetchGetAgentListByPage(params: Api.Agent.AgentSearchParams) {
  return request.get<Api.Agent.AgentDTO[]>({
    url: '/api/agents/list-by-page',
    params
  })
}

/**
 * 获取所有客户端列表
 * @returns 客户端列表
 */
export function fetchGetAgentListAll() {
  return request.get<Api.Agent.AgentDTO[]>({
    url: '/api/agents/list'
  })
}

/**
 * 获取单个客户端详情
 * @param id 客户端ID
 * @returns 客户端详情
 */
export function fetchGetAgentById(id: string) {
  return request.get<Api.Agent.AgentDTO>({
    url: `/api/agents/${id}`
  })
}

/**
 * 剔除在线客户端
 * @param id 客户端ID
 * @returns 剔除结果
 */
export function fetchKickoutAgent(id: string) {
  return request.put({
    url: `/api/agents/kickout/${id}`
  })
}
