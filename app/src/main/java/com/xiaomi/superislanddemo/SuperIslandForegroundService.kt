package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 超级岛前台服务
 *
 * 客户端方式发送超级岛通知需要应用保持后台活跃。
 * 通过前台服务确保应用进程不会被系统杀死。
 */
class SuperIslandForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 999
        private const val CHANNEL_ID = "foreground_service"
    }

    override fun onCreate() {
        super.onCreate()
        
        // 前台服务通知（低调，不可见为佳）
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("服务运行中")
            .setContentText("正在提供岛通知服务")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
