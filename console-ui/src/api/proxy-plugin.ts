import {ProtocolType, getProtocolLabel} from '@/enums/orbien/business'
import type {ProxyConfigProtocol} from '@/views/orbien/plugin/menus'
import {
    fetchGetHttpProxyById,
    fetchGetHttpsProxyById,
    fetchGetTcpProxyById,
    fetchGetUdpProxyById,
    fetchSaveProxyClusterConfig
} from './proxy'
import {fetchGetFileShareById} from './file-share'

export type ProxyDetail =
    | Api.Proxy.HttpProxyDetailDTO
    | Api.Proxy.HttpsProxyDetailDTO
    | Api.Proxy.TcpProxyDetailDTO
    | Api.Proxy.UdpProxyDetailDTO
    | Api.FileShare.FileShareDetailDTO

const GET_API = {
    [ProtocolType.HTTP]: fetchGetHttpProxyById,
    [ProtocolType.HTTPS]: fetchGetHttpsProxyById,
    [ProtocolType.TCP]: fetchGetTcpProxyById,
    [ProtocolType.UDP]: fetchGetUdpProxyById,
    [ProtocolType.FILE]: fetchGetFileShareById
} as const

export function fetchProxyDetail(protocol: ProxyConfigProtocol, id: string) {
    const fetcher = GET_API[protocol as keyof typeof GET_API]
    if (!fetcher) {
        return Promise.reject(new Error(`${getProtocolLabel(protocol)} 暂不支持读取详情`))
    }
    return fetcher(id) as Promise<ProxyDetail>
}

export function saveProxyClusterConfig(
    _protocol: ProxyConfigProtocol,
    detail: ProxyDetail,
    targets: Api.Proxy.ProxyTargetAddParam[],
    loadBalance: Api.Proxy.LoadBalanceParam
) {
    return fetchSaveProxyClusterConfig(detail.id, {targets, loadBalance})
}
