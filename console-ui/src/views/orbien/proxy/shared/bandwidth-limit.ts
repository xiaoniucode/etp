import type {FormItemRule} from 'element-plus'
import {$t} from '@/locales/index'

export type BandwidthMbps = number | null | undefined

export const BANDWIDTH_RULES: FormItemRule[] = [
    {
        validator: (_rule, value: BandwidthMbps, callback) => {
            if (value == null) {
                callback()
                return
            }
            if (typeof value !== 'number' || Number.isNaN(value) || value < 1 || !Number.isInteger(value)) {
                callback(new Error($t('orbien.proxy.bandwidthInteger')))
                return
            }
            callback()
        },
        trigger: 'blur'
    }
]

export function toBandwidthPayload(value: BandwidthMbps): number | null {
    if (value == null || Number.isNaN(value)) {
        return null
    }
    return value
}
