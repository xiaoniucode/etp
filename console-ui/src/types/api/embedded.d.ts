declare namespace Api.Embedded {
  interface TunnelDTO {
    proxyId: string
    protocol: number
    name: string
    status: number
  }

  interface HttpTunnelListDTO extends TunnelDTO {
    domains: string[]
  }

  interface TcpTunnelListDTO extends TunnelDTO {
    listenPort: number
  }

  interface TunnelListDTO {
    tunnel: TunnelDTO
    httpProxyPort: number
  }

  interface TunnelDetailDTO {
    id: string
    name: string
    protocol: string
    remoteAddress: string
    localService: string
    status: number
    createdAt: string
    updatedAt: string
  }
}
