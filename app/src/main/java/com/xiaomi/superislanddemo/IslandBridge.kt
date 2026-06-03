package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONObject

/**
 * 微信 → 超级岛 桥接发送器
 *
 * 把监听到的微信消息以 OS3 超级岛格式重新发出。
 * 参照系统热点通知的已验证格式。
 */
object IslandBridge {

    private const val CHANNEL = "wechat_island"
    private const val CHANNEL_NAME = "微信上岛"
    private const val KEY = "miui.focus.param"

    /**
     * 初始化通知通道（Application.onCreate 或首次发送前调用）
     */
    fun init(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "微信消息超级岛通知"
                    setShowBadge(true)
                }
            )
        }
    }

    /**
     * 以超级岛形式展示一条微信消息
     *
     * @param ctx     Context
     * @param sender  发送者（如"小明"）
     * @param message 消息内容
     * @param notifyId 通知 id（用于更新/覆盖）
     */
    fun showMessage(ctx: Context, sender: String, message: String, notifyId: Int = 1) {
        val shortSender = if (sender.length > 4) sender.take(4) else sender
        val preview = if (message.length > 6) message.take(6) else message
        val islandText = "$shortSender:$preview"

        // 构建 OS3 超级岛 JSON（参照系统热点通知的已验证格式）
        val json = JSONObject().apply {
            put("param_v2", JSONObject().apply {
                put("protocol", 1)
                put("business", "wechat")
                put("timeout", 30)          // 通知 30 分钟后消失
                put("updatable", false)
                put("islandFirstFloat", true)  // 首次出现自动展开
                put("enableFloat", false)
                put("ticker", "$sender: $message")
                put("aodTitle", sender)

                // ── 岛数据 ──
                put("param_island", JSONObject().apply {
                    put("islandProperty", 1)   // 信息展示为主
                    put("islandTimeout", 300)   // 岛 5 分钟后消失

                    // 大岛 A区：图文组件1 — 发送者名
                    put("bigIslandArea", JSONObject().apply {
                        put("imageTextInfoLeft", JSONObject().apply {
                            put("type", 1)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)   // appIcon
                            })
                            put("textInfo", JSONObject().apply {
                                put("title", shortSender)
                                put("content", preview)
                                put("showHighlightColor", true)
                            })
                        })
                        // B区：文本组件 — 消息简写
                        put("textInfo", JSONObject().apply {
                            put("title", islandText)
                            put("useHighLight", false)
                        })
                    })

                    // 小岛：图标组件
                    put("smallIslandArea", JSONObject().apply {
                        put("picInfo", JSONObject().apply {
                            put("type", 1)   // appIcon
                        })
                    })

                    // 分享数据
                    put("shareData", JSONObject().apply {
                        put("title", "$sender 发来消息")
                        put("content", message)
                    })
                })

                // ── 展开态内容 ──
                put("iconTextInfo", JSONObject().apply {
                    put("title", sender)
                    put("content", message)
                })
            })
        }

        // 创建通知
        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pi = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val n = Notification.Builder(ctx, CHANNEL)
            .setContentTitle(sender)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()

        n.extras.putString(KEY, json.toString())

        ctx.getSystemService(NotificationManager::class.java).notify(notifyId, n)
    }

    /**
     * 清除上岛通知
     */
    fun clearMessage(ctx: Context, notifyId: Int = 1) {
        ctx.getSystemService(NotificationManager::class.java).cancel(notifyId)
    }
}
