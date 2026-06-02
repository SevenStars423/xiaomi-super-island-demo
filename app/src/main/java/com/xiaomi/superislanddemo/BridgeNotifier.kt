package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONObject

/**
 * 通知桥接发射器
 *
 * 将桥接到的第三方通知（如微信消息）以超级岛形式重新发出。
 * 基于小米超级岛语义规范，将普通聊天消息重塑为"待处理任务"通知。
 */
object BridgeNotifier {

    private const val TAG = "BridgeNotifier"

    /**
     * 将第三方通知桥接为超级岛通知
     *
     * @param context 上下文
     * @param sender 发送者名称
     * @param message 消息内容
     * @param sourcePackage 来源包名，如 com.tencent.mm
     */
    fun show(
        context: Context,
        sender: String,
        message: String,
        sourcePackage: String = "com.tencent.mm"
    ) {
        // 确保通道已初始化
        SuperIslandNotifier.initChannel(context)

        // 追踪信息：发送者 + 摘要
        val shortSender = if (sender.length > 4) sender.take(4) else sender
        val preview = if (message.length > 10) message.take(10) + "…" else message

        val params = SuperIslandNotifier.IslandParams().apply {
            business = "message"
            timeout = 60       // 1 小时后自动消失
            updatable = false  // 非持续性通知
            islandFirstFloat = true
            islandTimeout = 300 // 岛 5 分钟后自动消失

            // 状态栏
            ticker = "$sender: $message"

            // 息屏显示
            aodTitle = "新消息"

            // 焦点通知展开态
            focusType = 2
            focusTitle = sender
            focusContent = message

            // 大岛摘要态：图文组件1 + 文本组件（模板2）
            bigIsland = JSONObject().apply {
                put("imageTextInfoLeft", JSONObject().apply {
                    put("type", 1)
                    put("picInfo", JSONObject().apply {
                        put("type", 1)
                        put("pic", "miui.focus.pic_message")
                    })
                    put("textInfo", JSONObject().apply {
                        put("title", shortSender)
                        put("content", preview)
                    })
                })
                put("textInfo", JSONObject().apply {
                    put("title", message.take(4))
                    put("content", sender)
                })
            }

            // 小岛
            smallIsland = JSONObject().apply {
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_message")
                })
            }

            // 内置两个 Action：打开应用 + 标记已读
            val openIntent = SuperIslandActionReceiver.buildPendingIntent(
                context,
                SuperIslandActionReceiver.ACTION_OPEN_APP
            )
            val confirmIntent = SuperIslandActionReceiver.buildPendingIntent(
                context,
                SuperIslandActionReceiver.ACTION_CONFIRM
            )

            actions["miui.focus.action_open"] = Triple(
                android.R.drawable.ic_menu_view, "打开", openIntent
            )
            actions["miui.focus.action_confirm"] = Triple(
                android.R.drawable.ic_menu_close_clear_cancel, "已读", confirmIntent
            )
        }

        SuperIslandNotifier.send(context, params)
    }
}
