/**
 * useHeaderBar - 顶部栏功能管理
 *
 * 统一管理顶部栏各个功能模块的显示状态和配置信息。
 *
 * @module useHeaderBar
 * @author Art Design Pro Team
 */

import {computed} from 'vue'
import {storeToRefs} from 'pinia'
import {useSettingStore} from '@/store/modules/setting'
import {headerBarConfig} from '@/config/modules/headerBar'
import {HeaderBarFeatureConfig} from '@/types'

/**
 * 顶部栏功能管理
 */
export function useHeaderBar() {
    const settingStore = useSettingStore()
    const headerBarConfigRef = computed<HeaderBarFeatureConfig>(() => headerBarConfig)

    const {showMenuButton, showFastEnter, showRefreshButton, showCrumbs, showLanguage} =
        storeToRefs(settingStore)

    const isFeatureEnabled = (feature: keyof HeaderBarFeatureConfig): boolean => {
        return headerBarConfigRef.value[feature]?.enabled ?? false
    }

    const getFeatureConfig = (feature: keyof HeaderBarFeatureConfig) => {
        return headerBarConfigRef.value[feature]
    }

    const shouldShowMenuButton = computed(() => {
        return isFeatureEnabled('menuButton') && showMenuButton.value
    })

    const shouldShowRefreshButton = computed(() => {
        return isFeatureEnabled('refreshButton') && showRefreshButton.value
    })

    const shouldShowFastEnter = computed(() => {
        return isFeatureEnabled('fastEnter') && showFastEnter.value
    })

    const shouldShowBreadcrumb = computed(() => {
        return isFeatureEnabled('breadcrumb') && showCrumbs.value
    })

    const shouldShowFullscreen = computed(() => {
        return isFeatureEnabled('fullscreen')
    })

    const shouldShowLanguage = computed(() => {
        return isFeatureEnabled('language') && showLanguage.value
    })

    const shouldShowSettings = computed(() => {
        return isFeatureEnabled('settings')
    })

    const shouldShowThemeToggle = computed(() => {
        return isFeatureEnabled('themeToggle')
    })

    const fastEnterMinWidth = computed(() => {
        return getFeatureConfig('fastEnter')?.minWidth || 1200
    })

    return {
        headerBarConfig: headerBarConfigRef,
        shouldShowMenuButton,
        shouldShowRefreshButton,
        shouldShowFastEnter,
        shouldShowBreadcrumb,
        shouldShowFullscreen,
        shouldShowLanguage,
        shouldShowSettings,
        shouldShowThemeToggle,
        fastEnterMinWidth,
        isFeatureEnabled,
        getFeatureConfig
    }
}
