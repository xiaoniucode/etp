/**
 * 国际化配置
 *
 * 基于 vue-i18n 实现的多语言国际化解决方案。
 * 支持中文、英文、日文切换，自动从本地存储恢复用户的语言偏好。
 *
 * ## 主要功能
 *
 * - 多语言支持 - 支持简体中文、English、日本語
 * - 语言切换 - 运行时动态切换语言，无需刷新页面
 * - 持久化存储 - 自动保存和恢复用户的语言偏好
 * - 全局注入 - 在任何组件中都可以使用 $t 函数进行翻译
 *
 * @module locales
 */

import { createI18n } from 'vue-i18n'
import type { Composer, I18nOptions } from 'vue-i18n'
import { LanguageEnum } from '@/enums/appEnum'
import { getSystemStorage } from '@/utils/storage'
import { StorageKeyManager } from '@/utils/storage/storage-key-manager'

import enMessages from './langs/en.json'
import zhMessages from './langs/zh.json'
import jaMessages from './langs/ja.json'

const storageKeyManager = new StorageKeyManager()

const messages = {
  [LanguageEnum.EN]: enMessages,
  [LanguageEnum.ZH]: zhMessages,
  [LanguageEnum.JA]: jaMessages
}

export const languageOptions = [
  { value: LanguageEnum.ZH, label: '简体中文' },
  { value: LanguageEnum.EN, label: 'English' },
  { value: LanguageEnum.JA, label: '日本語' }
]

const getDefaultLanguage = (): LanguageEnum => {
  try {
    const storageKey = storageKeyManager.getStorageKey('user')
    const userStore = localStorage.getItem(storageKey)

    if (userStore) {
      const { language } = JSON.parse(userStore)
      if (language && Object.values(LanguageEnum).includes(language)) {
        return language
      }
    }
  } catch (error) {
    console.warn('[i18n] 从版本化存储获取语言设置失败:', error)
  }

  try {
    const sys = getSystemStorage()
    if (sys) {
      const { user } = JSON.parse(sys)
      if (user?.language && Object.values(LanguageEnum).includes(user.language)) {
        return user.language
      }
    }
  } catch (error) {
    console.warn('[i18n] 从系统存储获取语言设置失败:', error)
  }

  console.debug('[i18n] 使用默认语言:', LanguageEnum.ZH)
  return LanguageEnum.ZH
}

const i18nOptions: I18nOptions = {
  locale: getDefaultLanguage(),
  legacy: false,
  globalInjection: true,
  fallbackLocale: LanguageEnum.ZH,
  messages
}

const i18n = createI18n(i18nOptions)

/**
 * 全局翻译函数，可在非 setup / 非组件环境使用。
 * 项目启用 `legacy: false`，运行时 global 为 Composer；经 Composer 收窄后可安全调用。
 */
export function $t(key: string, named?: Record<string, unknown>): string {
  const { t } = i18n.global as unknown as Composer
  return named === undefined ? t(key) : t(key, named)
}

export default i18n
