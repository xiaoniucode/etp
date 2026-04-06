import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { systemRoutes } from './system'
import { exceptionRoutes } from './exception'
import { penetrationRoutes } from './penetration'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  ...penetrationRoutes,
  systemRoutes,
  exceptionRoutes
]
