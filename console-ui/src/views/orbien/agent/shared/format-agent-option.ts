import { $t } from '@/locales'

export function formatAgentOptionLabel(agent: Api.Agent.AgentDTO): string {
  const shortId = agent.id.length > 8 ? agent.id.slice(-8) : agent.id
  const statusLabel = agent.isOnline ? $t('common.online') : $t('common.offline')
  return `${agent.name} · ${statusLabel} · ${shortId}`
}
