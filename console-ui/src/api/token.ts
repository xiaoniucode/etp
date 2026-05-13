import request from '@/utils/http'

/**
 * 获取访问令牌列表（分页）
 * @param params 搜索参数
 * @returns 访问令牌分页列表
 */
export function fetchGetTokenList(params: Api.Common.CommonSearchParams) {
  return request.get<Api.Common.PaginatedResponse<Api.AccessToken.AccessTokenDTO>>({
    url: '/api/access-tokens',
    params
  })
}

/**
 * 根据ID获取访问令牌详情
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
export function fetchCreateToken(params: Api.AccessToken.AccessTokenCreateParam) {
  return request.post<Api.AccessToken.AccessTokenDTO>({
    url: '/api/access-tokens',
    data: params
  })
}

/**
 * 更新访问令牌
 * @param params 更新参数
 * @returns 更新结果
 */
export function fetchUpdateToken(params: Api.AccessToken.AccessTokenUpdateParam) {
  return request.put({
    url: '/api/access-tokens',
    data: params
  })
}
