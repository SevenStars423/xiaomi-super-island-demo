package com.xiaomi.superislanddemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        // 标题
        layout.addView(TextView(this).apply {
            text = "💬 微信上岛"
            textSize = 24f
        })

        // 状态
        statusText = TextView(this).apply {
            textSize = 13f
            setPadding(20, 20, 20, 20)
        }
        layout.addView(statusText)

        // 按钮
        fun btn(label: String, action: () -> Unit) {
            layout.addView(Button(this).apply {
                text = label; setOnClickListener { action() }
            })
        }

        btn("⚙️ 去开启通知监听权限") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btn("🧪 模拟一条微信消息测试") {
            IslandBridge.init(this)
            IslandBridge.showMessage(this, "小明", "今晚一起吃饭吗？", 100)
            Toast.makeText(this, "已发送测试通知", Toast.LENGTH_SHORT).show()
        }

        btn("🩺 刷新状态") { refresh() }

        // 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val b = StringBuilder()
        b.appendLine("═══ 状态 ═══")

        val hasNotif = (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        b.appendLine("通知权限: ${if (hasNotif) "✅" else "❌ 去系统设置打开"}")

        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        val isEnabled = flat.contains("WeChatIslandBridge")
        b.appendLine("监听服务: ${if (isEnabled) "✅ 已授权" else "❌ 点上方按钮开启"}")

        // 焦点权限
        val hasFocus = try {
            val uri = Uri.parse("content://miui.statusbar.notification.public")
            val extras = Bundle().apply { putString("package", packageName) }
            contentResolver.call(uri, "canShowFocus", null, extras)
                ?.getBoolean("canShowFocus", false) ?: false
        } catch (e: Exception) { false }
        b.appendLine("焦点权限: ${if (hasFocus) "✅" else "❌ 需小米平台审批"}")

        val protocol = try {
            Settings.System.getInt(contentResolver, "notification_focus_protocol", 0)
        } catch (e: Exception) { 0 }
        b.appendLine("协议版本: v$protocol (需 ≥3)")

        b.appendLine("═══ ═══")
        if (!hasFocus) {
            b.appendLine("\n⚠️ 焦点通知权限未开通")
            b.appendLine("完成小米企业开发者审批")
            b.appendLine("+ 开通超级岛服务后即可使用")
        } else if (isEnabled) {
            b.appendLine("\n✅ 已就绪！")
            b.appendLine("让微信好友发条消息试试")
        }

        statusText.text = b.toString()
    }
}
