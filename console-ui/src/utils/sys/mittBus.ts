/**
 * 全局事件总线模块
 *
 * 基于 mitt 库实现的类型安全的事件总线
 *
 * ## 主要功能
 *
 * - 跨组件通信（发布/订阅模式）
 * - 类型安全的事件定义和调用
 * - 全局事件管理（设置面板、锁屏等）
 * - 解耦组件间的直接依赖
 *
 * @module utils/sys/mittBus
 * @author Art Design Pro Team
 */
import mitt, { type Emitter } from 'mitt'

// 定义事件类型映射
type Events = {
  /** 打开设置面板 */
  openSetting: void
  /** 打开锁屏 */
  openLockScreen: void
}

// 创建类型安全的事件总线实例
const mittBus: Emitter<Events> = mitt<Events>()

export default mittBus
