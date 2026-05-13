import { AppRouteRecord } from '@/types/router'

export const penetrationRoutes: AppRouteRecord[] = [
  {
    name: 'Embedded',
    path: '/embedded',
    component: '/etp/embedded',
    meta: {
      title: 'menus.etp.embedded',
      icon: 'ri:network-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'HTTP',
    path: '/http',
    component: '/etp/http',
    meta: {
      title: 'menus.etp.http',
      icon: 'ri:global-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'TCP',
    path: '/tcp',
    component: '/etp/tcp',
    meta: {
      title: 'menus.etp.tcp',
      icon: 'ri:server-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Agent',
    path: '/agent',
    component: '/etp/agent',
    meta: {
      title: 'menus.etp.agent',
      icon: 'ri:computer-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Domain',
    path: '/domain',
    component: '/etp/domain',
    meta: {
      title: 'menus.etp.domain',
      icon: 'ri:link',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Token',
    path: '/token',
    component: '/etp/token',
    meta: {
      title: 'menus.etp.token',
      icon: 'ri:key-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Stats',
    path: '/stats',
    component: '/etp/stats',
    meta: {
      title: 'menus.etp.stats',
      icon: 'ri:bar-chart-line',
      roles: ['R_SUPER']
    }
  },
  {
    name: 'Monitor',
    path: '/monitor',
    component: '/etp/monitor',
    meta: {
      title: 'menus.etp.monitor',
      icon: 'ri:server-line',
      roles: ['R_SUPER']
    }
  }
]
