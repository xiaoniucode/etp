import request from '@/utils/http'

export function fetchGetAppConfig() {
  return request.get<Api.App.AppConfigInfoDTO>({
    url: '/api/app/config'
  })
}