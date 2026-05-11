declare namespace Api.Embedded {
  interface TargetDTO {
    id?: number
    proxyId?: string
    host: string
    port: number
    weight?: number
    name?: string
  }

  interface AgentDTO {
    id: string
    name: string
    token: string
    os: string
    arch: string
    version: string
    lastActiveTime: string
  }

  interface BandwidthDTO {
    limitIn?: string
    limitOut?: string
    limitTotal?: string
  }

  interface TransportDTO {
    encrypt?: boolean
    tunnelType?: number
  }

  interface ProxyDTO {
    proxyId: string
    name: string
    protocol: number
    status: number
    targets: TargetDTO[]
    deploymentMode: number
    transport: TransportDTO
    bandwidth: BandwidthDTO
  }

  interface HttpProxyDTO extends ProxyDTO {
    domains: string[]
    domainType?: number
  }

  interface TcpProxyDTO extends ProxyDTO {
    listenPort: number
  }

  interface TunnelDetailDTO {
    agent: AgentDTO
    proxy: ProxyDTO | HttpProxyDTO | TcpProxyDTO
    httpProxyPort: number
  }

  interface TunnelDTO {
    agentId: string
    proxyId: string
    protocol: number
    name: string
    status: number
    targets: TargetDTO[]
  }

  interface HttpTunnelListDTO extends TunnelDTO {
    domains: string[]
  }

  interface TcpTunnelListDTO extends TunnelDTO {
    listenPort: number
  }

  interface TunnelListDTO {
    tunnel: HttpTunnelListDTO | TcpTunnelListDTO
    httpProxyPort: number
  }
}
