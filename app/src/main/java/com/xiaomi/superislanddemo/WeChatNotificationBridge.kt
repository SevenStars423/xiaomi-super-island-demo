package com.xiaomi.superislanddemo

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * 微信通知桥接服务
 *
 * 监听系统通知，当检测到微信（com.tencent.mm）有新消息时，
 * 提取通知内容并通过 BridgeNotifier 以小米超级岛形式重新展示。
 *
 * 使用步骤：
 * 1. 打开系统设置 → 通知使用权 → 授予本应用
 * 2. 微信需开启"通知显示消息详情"
 */
class WeChatNotificationBridge : NotificationListenerService() {

    companion object {
        private const val TAG = "WeChatBridge"
        
        // 需要监听的包名列表
        private val TARGET_PACKAGES = setOf(
            "com.tencent.mm"       // 微信
        //  "com.tencent.mobileqq", // QQ（按需添加）
        //  "com.ss.android.ugc.aweme", // 抖音（按需添加）
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 只处理目标应用的普通通知
        if (sbn.packageName !in TARGET_PACKAGES) return

        val notification = sbn.notification
        val extras = notification.extras ?: return

        try {
            // 提取通知内容
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

            // 组装消息
            val sender = title.ifBlank { subText.ifBlank { "微信消息" } }
            val message = bigText.ifBlank { text }.ifBlank { "收到一条微信消息" }

            Log.i(TAG, "桥接通知: sender=$sender, message=$message")

            // 以超级岛形式发出
            BridgeNotifier.show(
                context = this,
                sender = sender,
                message = message,
                sourcePackage = sbn.packageName
            )

        } catch (e: Exception) {
            Log.e(TAG, "处理通知失败: ${e.message}", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 可在此处理通知被移除的情况
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "通知监听服务已连接")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "通知监听服务已断开，尝试重新绑定...")
        requestRebind(android.content.ComponentName(this, WeChatNotificationBridge::class.java))
    }
}
