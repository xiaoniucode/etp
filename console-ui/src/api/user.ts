import request from '@/utils/http'

/**
 * 更新密码
 * @param data 密码更新参数
 * @returns 更新结果
 */
export function fetchUpdatePassword(data: any) {
  return request.put({
    url: '/api/user/update-password',
    data,
    showSuccessMessage: true
  })
}
