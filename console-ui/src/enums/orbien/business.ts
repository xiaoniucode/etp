import {$t} from '@/locales'

export enum ProtocolType {
    TCP = 1,
    HTTP = 2,
    HTTPS = 3,
    UDP = 4,
    SOCKS5 = 5,
    FILE = 6
}

export enum PortPoolType {
    TCP = 1,
    UDP = 2
}

export enum ProxyStatus {
    CLOSED = 0,
    OPEN = 1
}

export enum DomainType {
    AUTO = 0,
    SUBDOMAIN = 1,
    CUSTOM_DOMAIN = 2
}

export enum AccessControl {
    DENY = 0,
    ALLOW = 1
}

export enum HeaderDirection {
    REQUEST = 1,
    RESPONSE = 2
}

export enum HeaderAction {
    SET = 1,
    ADD = 2,
    REMOVE = 3
}

export enum HealthCheckType {
    TCP = 0,
    HTTP = 1
}

export enum LoadBalanceType {
    ROUND_ROBIN = 1,
    WEIGHT = 2,
    RANDOM = 3,
    LEAST_CONN = 4
}

export enum AgentType {
    SESSION = 0,
    STANDALONE = 1
}

export enum BandwidthUnit {
    BPS = 'bps',
    KBPS = 'Kbps',
    MBPS = 'Mbps',
    GBPS = 'Gbps'
}

export enum TunnelType {
    MULTIPLEX = 0,
    DIRECT = 1
}

/** 传输协议 */
export enum TransportProtocol {
    TCP = 1,
    WEBSOCKET = 2,
    QUIC = 3
}

export enum TargetHealthStatus {
    DOWN = 0,
    UP = 1
}

export const PORT_POOL_TYPE_OPTIONS = [
    {label: 'TCP', value: PortPoolType.TCP},
    {label: 'UDP', value: PortPoolType.UDP}
] as const

export const HEADER_ACTION_OPTIONS = [
    {label: 'SET', value: HeaderAction.SET},
    {label: 'ADD', value: HeaderAction.ADD},
    {label: 'REMOVE', value: HeaderAction.REMOVE}
] as const

export function getHeaderActionLabel(action?: number) {
    switch (action) {
        case HeaderAction.SET:
            return 'SET'
        case HeaderAction.ADD:
            return 'ADD'
        case HeaderAction.REMOVE:
            return 'REMOVE'
        default:
            return action == null ? '' : String(action)
    }
}

export const BANDWIDTH_UNIT_OPTIONS = [
    BandwidthUnit.KBPS,
    BandwidthUnit.MBPS,
    BandwidthUnit.GBPS
] as const

export const BANDWIDTH_UNIT_TO_BPS: Record<BandwidthUnit, number> = {
    [BandwidthUnit.BPS]: 1,
    [BandwidthUnit.KBPS]: 1_000,
    [BandwidthUnit.MBPS]: 1_000_000,
    [BandwidthUnit.GBPS]: 1_000_000_000
}

export function getProtocolLabel(protocol?: number) {
    switch (protocol) {
        case ProtocolType.TCP:
            return 'TCP'
        case ProtocolType.HTTP:
            return 'HTTP'
        case ProtocolType.HTTPS:
            return 'HTTPS'
        case ProtocolType.UDP:
            return 'UDP'
        case ProtocolType.SOCKS5:
            return 'SOCKS5'
        case ProtocolType.FILE:
            return 'FILE'
        default:
            return ''
    }
}

export function getDomainTypeLabel(domainType: number) {
    switch (domainType) {
        case DomainType.CUSTOM_DOMAIN:
            return {type: 'warning' as const, text: $t('orbien.enum.domain.custom')}
        case DomainType.SUBDOMAIN:
            return {type: 'primary' as const, text: $t('orbien.enum.domain.subdomain')}
        case DomainType.AUTO:
            return {type: 'primary' as const, text: $t('orbien.enum.domain.auto')}
        default:
            return {type: 'info' as const, text: $t('orbien.enum.unknown')}
    }
}

export function getAgentTypeTag(agentType?: number) {
    if (agentType === AgentType.STANDALONE) {
        return {type: 'primary' as const, text: $t('orbien.enum.agent.standard')}
    }
    if (agentType === AgentType.SESSION) {
        return {type: 'warning' as const, text: $t('orbien.enum.agent.session')}
    }
    return {type: 'info' as const, text: $t('orbien.enum.unknown')}
}

export function getPortPoolTypeLabel(type: number) {
    switch (type) {
        case PortPoolType.TCP:
            return {type: 'primary' as const, text: 'TCP'}
        case PortPoolType.UDP:
            return {type: 'warning' as const, text: 'UDP'}
        default:
            return {type: 'info' as const, text: $t('orbien.enum.unknown')}
    }
}

export function getTransportProtocolLabel(protocol?: number) {
    switch (protocol) {
        case TransportProtocol.WEBSOCKET:
            return 'WebSocket'
        case TransportProtocol.QUIC:
            return 'QUIC'
        case TransportProtocol.TCP:
        default:
            return 'TCP'
    }
}

export function getTransportProtocolTag(transportProtocol?: number) {
    switch (transportProtocol) {
        case TransportProtocol.WEBSOCKET:
            return {type: 'success' as const, text: 'WebSocket'}
        case TransportProtocol.QUIC:
            return {type: 'warning' as const, text: 'QUIC'}
        case TransportProtocol.TCP:
        default:
            return {type: 'primary' as const, text: 'TCP'}
    }
}
