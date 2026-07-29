import {h} from 'vue'
import {ElTag} from 'element-plus'
import {$t} from '@/locales/index'

export type TlsCertSummaryTagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

export interface TlsCertSummaryDisplay {
    text: string
    type: TlsCertSummaryTagType
}

export function resolveTlsCertSummaryDisplay(
    summary?: Api.Proxy.TlsCertSummary | null
): TlsCertSummaryDisplay {
    if (!summary || summary.totalDomains === 0) {
        return {text: '—', type: 'info'}
    }

    const {totalDomains, deployedCount, warningCount} = summary

    if (deployedCount === totalDomains) {
        return {text: $t('orbien.proxy.tlsDeployed'), type: 'primary'}
    }

    if (deployedCount === 0) {
        return warningCount > 0
            ? {text: $t('orbien.proxy.tlsDeployError'), type: 'danger'}
            : {text: $t('orbien.proxy.tlsNotDeployed'), type: 'info'}
    }

    const partialText = $t('orbien.proxy.tlsPartialDeployed', {
        deployed: deployedCount,
        total: totalDomains
    })

    if (warningCount > 0) {
        return {
            text: partialText,
            type: 'warning'
        }
    }

    return {
        text: partialText,
        type: 'warning'
    }
}

export function renderTlsCertSummaryTag(
    summary?: Api.Proxy.TlsCertSummary | null,
    onClick?: () => void
) {
    const {text, type} = resolveTlsCertSummaryDisplay(summary)
    const clickable = Boolean(onClick) && text !== '—'

    return h(
        ElTag,
        {
            type,
            size: 'small',
            style: clickable ? 'cursor: pointer;' : undefined,
            onClick: clickable ? onClick : undefined
        },
        () => text
    )
}
