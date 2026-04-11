import { AppRouteRecord } from '@/types/router'

export const penetrationRoutes: AppRouteRecord[] = [
  {
    name: 'HTTP',
    path: '/http',
    component: '/etp/http',
    meta: {
      title: 'HTTP',
      icon: 'ri:global-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'TCP',
    path: '/tcp',
    component: '/etp/tcp',
    meta: {
      title: 'TCP',
      icon: 'ri:server-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Agent',
    path: '/agent',
    component: '/etp/agent',
    meta: {
      title: '客户端',
      icon: 'ri:computer-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Token',
    path: '/token',
    component: '/etp/token',
    meta: {
      title: '访问令牌',
      icon: 'ri:key-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Stats',
    path: '/stats',
    component: '/etp/stats',
    meta: {
      title: '统计',
      icon: 'ri:bar-chart-line',
      roles: ['R_SUPER']
    }
  }
]
