import { PortPoolType } from '@/enums/orbien/business'

/** 端口池应用类型模板 */
export const CUSTOM_PRESET_ID = 'custom'

export type PortPoolPresetGroup =
  | 'remoteLogin'
  | 'webService'
  | 'database'
  | 'fileTransfer'
  | 'networkService'

export interface PortPoolPreset {
  id: string
  type: PortPoolType
  port: string
  group: PortPoolPresetGroup
}

export const PORT_POOL_PRESETS: PortPoolPreset[] = [
  { id: 'ssh', type: PortPoolType.TCP, port: '22', group: 'remoteLogin' },
  { id: 'rdp', type: PortPoolType.TCP, port: '3389', group: 'remoteLogin' },
  { id: 'http', type: PortPoolType.TCP, port: '80', group: 'webService' },
  { id: 'https', type: PortPoolType.TCP, port: '443', group: 'webService' },
  { id: 'mysql', type: PortPoolType.TCP, port: '3306', group: 'database' },
  { id: 'postgresql', type: PortPoolType.TCP, port: '5432', group: 'database' },
  { id: 'sqlserver', type: PortPoolType.TCP, port: '1433', group: 'database' },
  { id: 'redis', type: PortPoolType.TCP, port: '6379', group: 'database' },
  { id: 'mongodb', type: PortPoolType.TCP, port: '27017', group: 'database' },
  { id: 'ftp', type: PortPoolType.TCP, port: '21', group: 'fileTransfer' },
  { id: 'dns', type: PortPoolType.UDP, port: '53', group: 'networkService' },
  { id: 'ntp', type: PortPoolType.UDP, port: '123', group: 'networkService' },
  { id: 'snmp', type: PortPoolType.UDP, port: '161', group: 'networkService' }
]

export const PORT_POOL_PRESET_GROUPS = [...new Set(PORT_POOL_PRESETS.map((item) => item.group))]
