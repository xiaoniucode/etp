import request from '@/utils/http'

/**
 * 获取客户端列表
 * @param params 查询参数
 * @returns 客户端列表
 */
export function fetchGetClientList(params: { keyword?: string; page: number; size: number }) {
  return request.get({
    url: '/api/agents',
    params
  })
}

/**
 * 获取单个客户端详情
 * @param id 客户端ID
 * @returns 客户端详情
 */
export function fetchGetClientById(id: string) {
  return request.get({
    url: `/api/agents/${id}`
  })
}

/**
 * 删除客户端
 * @param id 客户端ID
 * @returns 删除结果
 */
export function fetchDeleteClient(id: string) {
  return request.del({
    url: `/api/agents/${id}`
  })
}

/**
 * 批量删除客户端
 * @param ids 客户端ID列表
 * @returns 删除结果
 */
export function fetchDeleteBatchClients(ids: string[]) {
  return request.del({
    url: '/api/agents',
    data: { ids }
  })
}

/**
 * 剔除在线客户端
 * @param id 客户端ID
 * @returns 剔除结果
 */
export function fetchKickoutClient(id: string) {
  return request.put({
    url: `/api/agents/${id}/kickout`
  })
}
