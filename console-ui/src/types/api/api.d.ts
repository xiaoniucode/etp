/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Art Design Pro Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      refreshToken: string
    }

    /** 用户信息 */
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: number
      username: string
      email: string
      avatar?: string
    }
  }

  /** 访问令牌类型 */
  namespace AccessToken {
    /** 访问令牌列表项 */
    interface AccessTokenDTO {
      id: number
      name: string
      token: string
      maxDevices: number
      maxConnections: number
      createdAt: string
      updatedAt: string
    }
  }

  /** HTTP 代理类型 */
  namespace HttpProxy {
    /** 目标地址DTO */
    interface TargetDTO {
      id: string
      targetHost: string
      targetPort: number
      targetPath: string
    }

    /** 带宽DTO */
    interface BandwidthDTO {
      uploadLimit: number
      downloadLimit: number
    }

    /** HTTP 代理列表项 */
    interface HttpProxyDTO {
      id: string
      agentId: string
      name: string
      protocol: number
      status: number
      domainType: number
      agentType: number
      encrypt: boolean
      createdAt: string
      updatedAt: string
      domains: string[]
      targets: TargetDTO[]
      bandwidth: BandwidthDTO
      httpProxyPort: number
    }
  }

  /** 客户端类型 */
  namespace Agent {
    /** 客户端列表项 */
    interface AgentDTO {
      id: string
      name: string
      token: string
      isOnline: boolean
      os: string
      arch: string
      version: string
      agentType: number
      createdAt: string
      updatedAt: string
      lastActiveTime: string
    }

    /** 客户端搜索参数 */
    interface AgentSearchParams {
      keyword?: string
      page: number
      size: number
    }
  }

  /** 流量统计类型 */
  namespace Metrics {
    /** 流量统计数据 */
    interface MetricsDTO {
      proxyId: string
      activeChannels: number
      readBytes: number
      writeBytes: number
      readMessages: number
      writeMessages: number
      readRate: number
      writeRate: number
      lastActiveTime: string
    }
  }

  /** 应用配置类型 */
  namespace App {
    /** 应用配置信息 */
    interface AppConfigInfo {
      serverAddr: string
      serverPort: number
      httpProxyPort: number
      baseDomain: string
      portStart: number
      portEnd: number
    }
  }

  /** 监控类型 */
  namespace Monitor {
    /** CPU 信息 */
    interface CpuInfo {
      total: number
      used: number
      usage: number
    }

    /** JVM 内存信息 */
    interface JvmMemoryInfo {
      total: string
      used: string
      usage: number
    }

    /** 操作系统内存信息 */
    interface OsMemoryInfo {
      total: string
      used: string
      usage: number
    }

    /** 服务器信息 */
    interface ServerInfo {
      cpu: CpuInfo
      jvmMem: JvmMemoryInfo
      osMem: OsMemoryInfo
    }

    /** 仪表盘摘要信息 */
    interface DashboardSummary {
      totalAgents: number
      onlineAgents: number
      totalProxies: number
      startedProxies: number
    }
  }
}
