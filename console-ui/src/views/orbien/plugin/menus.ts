/**
 * 代理配置弹窗 - 协议类型与菜单配置
 */
import {ProtocolType} from '@/enums/orbien/business'
import {$t} from '@/locales'

/** 弹窗支持的协议类型 */
export type ProxyConfigProtocol =
    | ProtocolType.TCP
    | ProtocolType.UDP
    | ProtocolType.HTTP
    | ProtocolType.HTTPS
    | ProtocolType.SOCKS5
    | ProtocolType.FILE

export interface ProxyConfigMenuItem {
    key: string
    label: string
    icon: string
}

const menuLabelKeys = {
    access: 'orbien.plugin.menus.accessControl',
    time: 'orbien.plugin.menus.timeAccess',
    auth: 'orbien.plugin.menus.basicAuth',
    load: 'orbien.plugin.menus.cluster',
    health: 'orbien.plugin.menus.healthCheck',
    tls: 'orbien.plugin.menus.tls',
    trans: 'orbien.plugin.menus.transport',
    headers: 'orbien.plugin.menus.headerRewrite'
} as const

const menuIcons = {
    access: 'ri:shield-line',
    time: 'ri:time-line',
    auth: 'ri:key-line',
    load: 'ri:server-line',
    health: 'ri:heart-pulse-line',
    tls: 'ri:shield-check-line',
    trans: 'ri:lock-line',
    headers: 'ri:edit-box-line'
} as const

type MenuKey = keyof typeof menuLabelKeys

function buildMenuItem(key: MenuKey): ProxyConfigMenuItem {
    return {
        key,
        label: $t(menuLabelKeys[key]),
        icon: menuIcons[key]
    }
}

const protocolMenuKeys: Record<ProxyConfigProtocol, MenuKey[]> = {
    [ProtocolType.TCP]: ['access', 'time', 'load', 'trans', 'health'],
    [ProtocolType.UDP]: ['access', 'time', 'load', 'trans'],
    [ProtocolType.HTTP]: ['access', 'time', 'auth', 'load', 'trans', 'health', 'headers'],
    [ProtocolType.HTTPS]: ['access', 'time', 'auth', 'load', 'tls', 'health', 'trans', 'headers'],
    [ProtocolType.SOCKS5]: ['access', 'time', 'trans'],
    [ProtocolType.FILE]: ['access', 'time', 'tls', 'trans']
}

/** 各协议对应的侧边栏菜单 */
export function getProtocolMenus(protocol: ProxyConfigProtocol): ProxyConfigMenuItem[] {
    const keys = protocolMenuKeys[protocol] ?? protocolMenuKeys[ProtocolType.HTTP]
    return keys.map(buildMenuItem)
}
