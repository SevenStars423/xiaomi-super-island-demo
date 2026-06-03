package com.xiaomi.superislanddemo

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * 微信通知 → 超级岛 桥接服务
 *
 * 监听微信（com.tencent.mm）新消息通知，
 * 提取发送者和内容，以 OS3 超级岛形式重新展示。
 *
 * 使用前需要：
 * 1. 系统设置 → 通知使用权 → 授权本应用
 * 2. 微信设置 → 新消息通知 → 通知显示消息详情
 */
class WeChatIslandBridge : NotificationListenerService() {

    companion object {
        private const val TAG = "WeChatIsland"
        private const val WECHAT_PKG = "com.tencent.mm"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        IslandBridge.init(this)
        Log.i(TAG, "微信上岛桥接已启动")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != WECHAT_PKG) return

        val extras = sbn.notification.extras ?: return

        try {
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

            val sender = title?.takeIf { it.isNotBlank() } ?: "微信"
            val message = bigText?.takeIf { it.isNotBlank() }
                ?: text?.takeIf { it.isNotBlank() }
                ?: "收到一条消息"

            // 用通知 id 区分不同联系人，这样同一个人连续发消息会覆盖而非堆叠
            val notifId = sender.hashCode()

            Log.i(TAG, "上岛: $sender — ${message.take(20)}")
            IslandBridge.showMessage(this, sender, message, notifId)

        } catch (e: Exception) {
            Log.e(TAG, "桥接失败: ${e.message}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 微信通知被清除时，同时清除岛的展示
        if (sbn.packageName == WECHAT_PKG) {
            val title = sbn.notification.extras
                ?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: ""
            val notifId = title.hashCode()
            IslandBridge.clearMessage(this, notifId)
        }
    }
}
