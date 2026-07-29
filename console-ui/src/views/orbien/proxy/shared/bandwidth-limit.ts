import type {FormItemRule} from 'element-plus'
import {$t} from '@/locales/index'

export type LimitTotalMbps = number | null | undefined

export const LIMIT_TOTAL_RULES: FormItemRule[] = [
    {
        validator: (_rule, value: LimitTotalMbps, callback) => {
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

export function toLimitTotalPayload(value: LimitTotalMbps): number | null {
    if (value == null || Number.isNaN(value)) {
        return null
    }
    return value
}
