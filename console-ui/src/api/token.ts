import request from '@/utils/http'

/**
 * 获取访问令牌列表
 * @param params 搜索参数
 * @returns 访问令牌列表
 */
export function fetchGetTokenList(params: { keyword?: string; page: number; size: number }) {
  return request.get<Api.AccessToken.AccessTokenDTO[]>({
    url: '/api/access-tokens',
    params: {
      keyword: params.keyword,
      page: params.page,
      size: params.size
    }
  })
}

/**
 * 获取访问令牌详情
 * @param id 访问令牌ID
 * @returns 访问令牌详情
 */
export function fetchGetTokenById(id: number) {
  return request.get<Api.AccessToken.AccessTokenDTO>({
    url: `/api/access-tokens/${id}`
  })
}

/**
 * 删除访问令牌
 * @param id 访问令牌ID
 * @returns 删除结果
 */
export function fetchDeleteToken(id: number) {
  return request.del({
    url: `/api/access-tokens/${id}`
  })
}

/**
 * 批量删除访问令牌
 * @param ids 访问令牌ID列表
 * @returns 删除结果
 */
export function fetchDeleteBatchTokens(ids: number[]) {
  return request.del({
    url: '/api/access-tokens',
    data: { ids }
  })
}

/**
 * 创建访问令牌
 * @param params 创建参数
 * @returns 创建结果
 */
export function fetchCreateToken(params: {
  name: string
  maxDevice: number
  maxConnection: number
}) {
  return request.post({
    url: '/api/access-tokens',
    params
  })
}

/**
 * 更新访问令牌
 * @param id 访问令牌ID
 * @param params 更新参数
 * @returns 更新结果
 */
export function fetchUpdateToken(
  id: number,
  params: {
    name: string
    maxDevice: number
    maxConnection: number
  }
) {
  return request.put({
    url: '/api/access-tokens',
    params: {
      id,
      ...params
    }
  })
}
