/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import request from '@/utils/http'

/**
 * 获取流量统计数据
 * @param proxyId 代理ID
 * @returns 流量统计数据
 */
export function fetchGetMetrics(proxyId: string) {
  return request.get({
    url: `/api/metrics/${proxyId}`
  })
}

/**
 * 获取所有代理的流量统计数据
 * @param page 页码
 * @param size 每页条数
 * @returns 流量统计数据列表
 */
export function fetchGetMetricsList(page: number = 0, size: number = 10) {
  return request.get({
    url: '/api/metrics/list',
    params: {
      page,
      size
    }
  })
}

/**
 * 获取24小时流量统计数据
 * @returns 24小时流量统计数据
 */
export function fetchGet24hMetrics() {
  return request.get<Api.Metrics.Metrics24LineDTO>({
    url: '/api/metrics/24h'
  })
}
