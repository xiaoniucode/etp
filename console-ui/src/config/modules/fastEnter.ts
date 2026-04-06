/**
 * 快速入口配置
 * 包含：应用列表、快速链接等配置
 */
import { WEB_LINKS } from '@/utils/constants'
import type { FastEnterConfig } from '@/types/config'

const fastEnterConfig: FastEnterConfig = {
  // 显示条件（屏幕宽度）
  minWidth: 1200,
  // 应用列表
  applications: [
    {
      name: '工作台',
      description: '系统概览与数据统计',
      icon: 'ri:pie-chart-line',
      iconColor: '#377dff',
      enabled: true,
      order: 1,
      routeName: 'Console'
    },
    {
      name: '分析页',
      description: '数据分析与可视化',
      icon: 'ri:game-line',
      iconColor: '#ff3b30',
      enabled: true,
      order: 2,
      routeName: 'Analysis'
    },
    {
      name: '聊天',
      description: '即时通讯功能',
      icon: 'ri:user-line',
      iconColor: '#13DEB9',
      enabled: true,
      order: 4,
      routeName: 'Chat'
    },
    {
      name: '官方文档',
      description: '使用指南与开发文档',
      icon: 'ri:bill-line',
      iconColor: '#ffb100',
      enabled: true,
      order: 5,
      link: WEB_LINKS.DOCS
    },

    {
      name: '更新日志',
      description: '版本更新与变更记录',
      icon: 'ri:gamepad-line',
      iconColor: '#38C0FC',
      enabled: true,
      order: 7,
      routeName: 'ChangeLog'
    }
  ],
  // 快速链接
  quickLinks: [
    {
      name: '登录',
      enabled: true,
      order: 1,
      routeName: 'Login'
    },
    {
      name: '忘记密码',
      enabled: true,
      order: 3,
      routeName: 'ForgetPassword'
    },
    {
      name: '个人中心',
      enabled: true,
      order: 5,
      routeName: 'UserCenter'
    }
  ]
}

export default Object.freeze(fastEnterConfig)
