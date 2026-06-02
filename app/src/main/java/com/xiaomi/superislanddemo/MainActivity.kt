package com.xiaomi.superislanddemo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // 标题
        layout.addView(TextView(this).apply {
            text = "🏝️ 超级岛诊断"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
        })

        // 状态面板
        tvStatus = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFFAAAAAA.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(20, 16, 20, 16)
            setBackgroundColor(0xFF1A1A2E.toInt())
        }
        layout.addView(tvStatus)

        // 按钮
        fun addBtn(text: String, color: Int = 0xFFE94560.toInt(), onClick: () -> Unit) {
            layout.addView(Button(this).apply {
                this.text = text
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(color)
                setOnClickListener { onClick() }
            })
        }

        addBtn("🩺 刷新诊断") { updateDiagnostics() }
        addBtn("🚗 发送 OS3 超级岛 — 打车") { sendTaxi() }
        addBtn("🛵 发送 OS3 超级岛 — 外卖") { sendDelivery() }
        addBtn("⏱ 发送 OS3 超级岛 — 倒计时") { sendTimer() }
        addBtn("📱 发送最简 OS3 岛通知") { sendMinimal() }
        addBtn("❌ 清除所有通知") { clearAll() }

        scroll.addView(layout)
        setContentView(scroll)

        // 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateDiagnostics()
    }

    private fun updateDiagnostics() {
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════")
        sb.appendLine("      设备诊断报告")
        sb.appendLine("═══════════════════════")
        sb.append("Android: ").appendLine(Build.VERSION.RELEASE)
        sb.append("SDK: ").appendLine(Build.VERSION.SDK_INT.toString())
        sb.append("厂商: ").appendLine(Build.MANUFACTURER)
        sb.append("型号: ").appendLine(Build.MODEL)
        sb.append("HyperOS: ").appendLine(isHyperOS().toString())
        sb.appendLine("---")
        sb.append("岛支持: ").appendLine(if (checkIslandSupport()) "✅ OS3" else "❌")
        sb.append("焦点协议: v").appendLine(getFocusProtocol().toString())
        sb.append("焦点权限: ").appendLine(if (checkFocusPermission()) "✅" else "❌ 需小米平台审批")
        sb.appendLine("---")
        sb.append("通知权限: ").appendLine(if (hasNotifyPermission()) "✅" else "❌")
        sb.append("通知监听: ").appendLine(if (isListenerEnabled()) "✅" else "❌")
        sb.appendLine("═══════════════════════")

        if (!checkIslandSupport()) {
            sb.appendLine("⚠️ 此设备不支持超级岛")
            sb.appendLine("需要 HyperOS 3 + 岛功能")
        } else if (!checkFocusPermission()) {
            sb.appendLine("⚠️ 无焦点通知权限")
            sb.appendLine("即使设备支持，没权限也出不来")
            sb.appendLine("需在小米开放平台审批通过")
        } else {
            sb.appendLine("✅ 条件满足，点击下方按钮测试")
        }
        tvStatus.text = sb.toString()
    }

    private fun isHyperOS(): Boolean {
        return try {
            Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java, String::class.java)
                .invoke(null, "ro.miui.ui.version.name", "")?.toString()?.isNotEmpty() == true
        } catch (e: Exception) { false }
    }

    private fun checkIslandSupport(): Boolean {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val m = c.getMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
            m.invoke(null, "persist.sys.feature.island", false) as? Boolean ?: false
        } catch (e: Exception) { false }
    }

    private fun getFocusProtocol(): Int {
        return try {
            Settings.System.getInt(contentResolver, "notification_focus_protocol", 0)
        } catch (e: Exception) { 0 }
    }

    private fun checkFocusPermission(): Boolean {
        return try {
            val uri = Uri.parse("content://miui.statusbar.notification.public")
            val b = Bundle().apply { putString("package", packageName) }
            contentResolver.call(uri, "canShowFocus", null, b)
                ?.getBoolean("canShowFocus", false) ?: false
        } catch (e: Exception) { false }
    }

    private fun hasNotifyPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else true
    }

    private fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains("WeChatNotificationBridge")
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel("island") == null) {
                nm.createNotificationChannel(NotificationChannel(
                    "island", "超级岛通知",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "OS3 超级岛通知通道"
                    setShowBadge(true)
                    enableVibration(false)
                })
            }
        }
    }

    private fun sendTaxi() {
        ensureChannel()
        toast("已发送 OS3 超级岛 — 打车")
        Os3Notifier.sendTaxi(this)
    }

    private fun sendDelivery() {
        ensureChannel()
        toast("已发送 OS3 超级岛 — 外卖")
        Os3Notifier.sendDelivery(this)
    }

    private fun sendTimer() {
        ensureChannel()
        toast("已发送 OS3 超级岛 — 倒计时")
        Os3Notifier.sendTimer(this)
    }

    private fun sendMinimal() {
        ensureChannel()
        toast("已发送最简 OS3 岛通知")
        Os3Notifier.sendMinimal(this)
    }

    private fun clearAll() {
        getSystemService(NotificationManager::class.java).cancelAll()
        toast("已清除")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
