import request from '@/utils/http'

/**
 * 获取应用配置信息
 * @returns 应用配置信息
 */
export function fetchGetAppConfig() {
  return request.get({
    url: '/api/app/config'
  })
}