package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * 接收超级岛通知的 Action 点击
 *
 * 当用户在超级岛卡片上点击按钮（如"开始导航""取消订单"等），
 * 系统通过 PendingIntent 触发此 Receiver。
 *
 * 需要在 AndroidManifest.xml 中声明 exported="true"
 */
class SuperIslandActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SuperIslandAction"
        
        // 自定义 Action 常量
        const val ACTION_OPEN_APP = "com.xiaomi.superislanddemo.OPEN_APP"
        const val ACTION_CANCEL = "com.xiaomi.superislanddemo.CANCEL_ORDER"
        const val ACTION_CONFIRM = "com.xiaomi.superislanddemo.CONFIRM"
        const val ACTION_NAVIGATE = "com.xiaomi.superislanddemo.NAVIGATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "收到超级岛操作: $action")

        when (action) {
            ACTION_CANCEL -> {
                // 取消订单逻辑
                val orderId = intent.getStringExtra("order_id") ?: ""
                Log.i(TAG, "取消订单: $orderId")
                
                // 取消超级岛通知（这里简化处理，实际需要记录通知 ID）
                // 并发送一个新的"已取消"通知更新岛状态
                SuperIslandNotifier.send(context, SuperIslandNotifier.IslandParams().apply {
                    business = "order_cancel"
                    timeout = -1  // 5 秒后消失
                    islandTimeout = 5
                    focusTitle = "订单已取消"
                    focusContent = "已为你取消该订单"
                })
            }

            ACTION_CONFIRM -> {
                Log.i(TAG, "确认操作")
                // 跳转到应用主界面
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }

            ACTION_NAVIGATE -> {
                Log.i(TAG, "开始导航")
                // 可启动导航 Intent
                val navIntent = Intent(Intent.ACTION_VIEW).apply {
                    // data = Uri.parse("...")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                // context.startActivity(navIntent)
            }

            ACTION_OPEN_APP -> {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }

    /**
     * 构建用于超级岛的 PendingIntent
     */
    fun buildPendingIntent(context: Context, action: String, extras: Bundle? = null): PendingIntent {
        val intent = Intent(context, SuperIslandActionReceiver::class.java).apply {
            this.action = action
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            extras?.let { putExtras(it) }
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
