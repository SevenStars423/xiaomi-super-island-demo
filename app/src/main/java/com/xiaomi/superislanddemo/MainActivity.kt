package com.xiaomi.superislanddemo

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 小米超级岛 Demo 主界面
 *
 * 功能：
 * - 发送各种场景的超级岛通知（打车、外卖、倒计时）
 * - 开启通知监听服务（微信消息桥接）
 * - 查看系统焦点通知支持状态
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        private const val REQUEST_LISTENER_PERMISSION = 1002
    }

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)

        // 初始化通知通道
        SuperIslandNotifier.initChannel(this)

        // 检查系统支持状态
        updateStatus()

        // 按钮绑定
        findViewById<Button>(R.id.btn_taxi).setOnClickListener {
            checkAndSend { SuperIslandNotifier.send(this, buildTaxiParams()) }
        }

        findViewById<Button>(R.id.btn_delivery).setOnClickListener {
            checkAndSend { SuperIslandNotifier.send(this, buildDeliveryParams()) }
        }

        findViewById<Button>(R.id.btn_timer).setOnClickListener {
            checkAndSend { SuperIslandNotifier.send(this, buildTimerParams()) }
        }

        findViewById<Button>(R.id.btn_cancel_all).setOnClickListener {
            // 取消方法：发送 cancel=true 的通知
            val params = SuperIslandNotifier.IslandParams().apply {
                business = "cancel"
                timeout = -1  // 5秒消失
                islandTimeout = 3
                focusTitle = "通知已清除"
            }
            SuperIslandNotifier.send(this, params)
            Toast.makeText(this, "已发送清除通知", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_open_listener).setOnClickListener {
            openNotificationListenerSettings()
        }

        findViewById<Button>(R.id.btn_test_bridge).setOnClickListener {
            // 模拟一条微信通知桥接
            BridgeNotifier.show(
                context = this,
                sender = "小明",
                message = "今晚一起吃饭吗？",
                sourcePackage = "com.tencent.mm"
            )
            Toast.makeText(this, "已模拟桥接通知", Toast.LENGTH_SHORT).show()
        }

        // Android 13+ 请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun checkAndSend(send: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "请先授予通知权限", Toast.LENGTH_SHORT).show()
            return
        }

        // 检查焦点通知权限
        if (!hasFocusPermission()) {
            Toast.makeText(this, "焦点通知权限未开启", Toast.LENGTH_SHORT).show()
        }

        send()
        Toast.makeText(this, "超级岛通知已发送", Toast.LENGTH_SHORT).show()
    }

    /**
     * 检测焦点通知权限
     */
    private fun hasFocusPermission(): Boolean {
        return try {
            val uri = android.net.Uri.parse("content://miui.statusbar.notification.public")
            val extras = Bundle().apply {
                putString("package", packageName)
            }
            val bundle = contentResolver.call(uri, "canShowFocus", null, extras)
            bundle?.getBoolean("canShowFocus", false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "检测焦点通知权限失败", e)
            false
        }
    }

    /**
     * 检测岛支持状态
     */
    private fun isSupportIsland(): Boolean {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
            val result = method.invoke(null, "persist.sys.feature.island", false)
            result as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取焦点通知协议版本
     */
    private fun getFocusProtocolVersion(): Int {
        return try {
            android.provider.Settings.System.getInt(
                contentResolver,
                "notification_focus_protocol",
                0
            )
        } catch (e: Exception) {
            0
        }
    }

    private fun updateStatus() {
        val sb = StringBuilder()
        sb.appendLine("=== 系统状态检查 ===")
        sb.append("支持岛（OS3）: ").appendLine(if (isSupportIsland()) "✅" else "❌")
        sb.append("焦点协议版本: ").appendLine(getFocusProtocolVersion())
            .appendLine("  (1=OS1, 2=OS2, 3=OS3)")
        sb.append("焦点通知权限: ").appendLine(if (hasFocusPermission()) "✅ 已开启" else "❌ 未开启")
        sb.append("通知监听服务: ").appendLine(
            if (isNotificationListenerEnabled()) "✅ 已授权" else "❌ 未授权"
        )
        sb.appendLine("==========================")
        if (!isSupportIsland()) {
            sb.appendLine("⚠️ 当前设备不支持超级岛（需要 HyperOS 3+）")
            sb.appendLine("   焦点通知可在 OS2/OS3 上显示")
        }
        tvStatus.text = sb.toString()
    }

    /**
     * 检查通知监听服务是否已授权
     */
    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        if (flat.isNullOrBlank()) return false
        val componentName = ComponentName(this, WeChatNotificationBridge::class.java).flattenToString()
        return flat.split(":").any { it == componentName || it == componentName }
    }

    /**
     * 打开通知使用权设置页面
     */
    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}
