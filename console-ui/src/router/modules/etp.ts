import { AppRouteRecord } from '@/types/router'

export const penetrationRoutes: AppRouteRecord[] = [
  {
    name: 'Embedded',
    path: '/embedded',
    component: '/etp/embedded',
    meta: {
      title: '会话隧道',
      icon: 'ri:network-line',
      roles: ['R_SUPER']
    }
  },
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
      title: '流量统计',
      icon: 'ri:bar-chart-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Monitor',
    path: '/monitor',
    component: '/etp/monitor',
    meta: {
      title: '系统监控',
      icon: 'ri:server-line',
      roles: ['R_SUPER']
    }
  }
]
