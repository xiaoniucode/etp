import { AppRouteRecord } from '@/types/router'

export const penetrationRoutes: AppRouteRecord[] = [
  {
    name: 'HTTP',
    path: '/http',
    component: '/penetration/http',
    meta: {
      title: 'HTTP',
      icon: 'ri:global-line',
      roles: ['R_SUPER', 'R_ADMIN']
    }
  },
  {
    name: 'TCP',
    path: '/tcp',
    component: '/penetration/tcp',
    meta: {
      title: 'TCP',
      icon: 'ri:server-line',
      roles: ['R_SUPER', 'R_ADMIN']
    }
  },
  {
    name: 'Client',
    path: '/client',
    component: '/penetration/client',
    meta: {
      title: '客户端',
      icon: 'ri:computer-line',
      roles: ['R_SUPER', 'R_ADMIN']
    }
  },
  {
    name: 'Token',
    path: '/token',
    component: '/penetration/token',
    meta: {
      title: '访问令牌',
      icon: 'ri:key-line',
      roles: ['R_SUPER', 'R_ADMIN']
    }
  }
]
