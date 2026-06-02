package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

/**
 * 小米超级岛通知发送工具
 *
 * 两种发送方式：
 * 1. 客户端发送 — 应用需保持活跃（后台运行），直接构建 Notification
 * 2. MiPush 发送 — 云端下发，应用无需保活
 *
 * 本工具实现客户端方式。
 */
object SuperIslandNotifier {

    private const val CHANNEL_ID = "super_island_channel"
    private const val CHANNEL_NAME = "小米超级岛通知"
    private const val KEY_FOCUS_PARAM = "miui.focus.param"
    private const val KEY_FOCUS_PICS = "miui.focus.pics"
    private const val KEY_FOCUS_ACTIONS = "miui.focus.actions"

    /** 通知 ID 计数器 */
    private var notificationId = 1000

    /**
     * 初始化通知通道（在 Application.onCreate 或首次发通知前调用）
     */
    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "小米超级岛通知通道"
                setShowBadge(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /**
     * 发送超级岛通知
     * @param context 上下文
     * @param params 岛参数构建器
     */
    fun send(context: Context, params: IslandParams) {
        val nm = context.getSystemService(NotificationManager::class.java)

        // 1. 构建 JSON 参数
        val json = params.toJson()

        // 2. 构建图片 Bundle
        val picsBundle = Bundle()
        params.pics.forEach { (key, resId) ->
            picsBundle.putParcelable(
                key,
                Icon.createWithResource(context, resId)
            )
        }

        // 3. 构建 Action Bundle
        val actionsBundle = Bundle()
        params.actions.forEach { (key, triple) ->
            val (resId, title, pendingIntent) = triple
            val action = Notification.Action.Builder(
                Icon.createWithResource(context, resId), title, pendingIntent
            ).build()
            actionsBundle.putParcelable(key, action)
        }

        // 4. 创建通知
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(params.defaultTitle)
            .setContentText(params.defaultText)
            .setSmallIcon(params.smallIconResId)
            .setOngoing(params.updatable)
            .addExtras(Bundle().apply {
                putBundle(KEY_FOCUS_PICS, picsBundle)
                putBundle(KEY_FOCUS_ACTIONS, actionsBundle)
            })
            .build()

        // 5. 注入超级岛参数
        notification.extras.putString(KEY_FOCUS_PARAM, json)

        // 6. 发送
        val id = ++notificationId
        nm.notify(id, notification)
    }

    /**
     * 取消某个超级岛通知
     */
    fun cancel(context: Context, id: Int) {
        context.getSystemService(NotificationManager::class.java).cancel(id)
    }

    /**
     * 超级岛通知参数构建器
     */
    class IslandParams {
        // === 通知基础属性 ===
        var business: String = "demo"           // 运营场景
        var timeout: Int = 720                  // 通知消失时间（分钟），默认 720
        var updatable: Boolean = false          // 是否为持续性通知
        var islandFirstFloat: Boolean = true    // 首次出现是否自动展开
        var enableFloat: Boolean = false        // 更新时是否自动展开

        // === 状态栏（OS2）===
        var ticker: String = ""                 // 状态栏文案
        var tickerPic: String = ""              // 状态栏图标 key

        // === 息屏 AOD ===
        var aodTitle: String = ""               // 息屏文案
        var aodPic: String = ""                 // 息屏图标

        // === 岛属性 ===
        var islandProperty: Int = 1             // 1=信息展示 / 2=操作为主
        var islandTimeout: Int = 3600           // 岛消失时间（秒）

        // === 焦点通知（展开态）内容 ===
        var focusType: Int = 2                  // baseInfo type: 1 或 2
        var focusTitle: String = ""             // 焦点通知标题
        var focusContent: String = ""           // 焦点通知内容
        var focusColorTitle: String = ""        // 标题颜色

        // === 大岛（摘要态）===
        var bigIsland: JSONObject? = null       // 大岛 A+B 区数据
        var smallIsland: JSONObject? = null     // 小岛数据

        // === 分享数据 ===
        var shareTitle: String = ""             // 拖拽分享标题
        var shareContent: String = ""           // 拖拽分享内容
        var sharePic: String = ""               // 拖拽分享图片

        // === 通知默认显示 ===
        var defaultTitle: String = "超级岛通知"
        var defaultText: String = ""
        var smallIconResId: Int = android.R.drawable.ic_dialog_info

        // === 图片映射（key → resId）===
        val pics: MutableMap<String, Int> = mutableMapOf()

        // === Action 映射（key → Triple(resId, title, intent)）===
        val actions: MutableMap<String, Triple<Int, String, PendingIntent>> = mutableMapOf()

        fun toJson(): String {
            val root = JSONObject()
            val paramV2 = JSONObject()

            // 基础属性
            paramV2.put("protocol", 1)
            paramV2.put("business", business)
            paramV2.put("timeout", timeout)
            paramV2.put("updatable", updatable)
            paramV2.put("islandFirstFloat", islandFirstFloat)
            paramV2.put("enableFloat", enableFloat)

            // 状态栏
            if (ticker.isNotEmpty()) paramV2.put("ticker", ticker)
            if (tickerPic.isNotEmpty()) paramV2.put("tickerPic", tickerPic)

            // 息屏
            if (aodTitle.isNotEmpty()) paramV2.put("aodTitle", aodTitle)
            if (aodPic.isNotEmpty()) paramV2.put("aodPic", aodPic)

            // 焦点通知
            if (focusTitle.isNotEmpty()) {
                val baseInfo = JSONObject().apply {
                    put("type", focusType)
                    put("title", focusTitle)
                    if (focusContent.isNotEmpty()) put("content", focusContent)
                    if (focusColorTitle.isNotEmpty()) put("colorTitle", focusColorTitle)
                }
                paramV2.put("baseInfo", baseInfo)
            }

            // 岛数据
            val paramIsland = JSONObject()
            paramIsland.put("islandProperty", islandProperty)
            paramIsland.put("islandTimeout", islandTimeout)

            if (bigIsland != null) paramIsland.put("bigIslandArea", bigIsland)
            if (smallIsland != null) paramIsland.put("smallIslandArea", smallIsland)

            // 分享
            if (shareTitle.isNotEmpty()) {
                val share = JSONObject().apply {
                    put("title", shareTitle)
                    if (shareContent.isNotEmpty()) put("content", shareContent)
                    if (sharePic.isNotEmpty()) put("pic", sharePic)
                }
                paramIsland.put("shareData", share)
            }

            paramV2.put("param_island", paramIsland)
            root.put("param_v2", paramV2)

            return root.toString()
        }
    }
}

// ─── 常见场景的快捷工厂方法 ───

/** 打车场景 */
fun buildTaxiParams(): SuperIslandNotifier.IslandParams {
    return SuperIslandNotifier.IslandParams().apply {
        business = "taxi"
        timeout = 30
        updatable = true
        islandFirstFloat = true
        ticker = "行程进行中"
        aodTitle = "行程中"
        focusTitle = "司机已接单"
        focusContent = "京ABZ422 · 白色 · 3分钟后到达"
        focusType = 2

        // 大岛：图文组件1（A区）+ 图文组件2（B区）
        bigIsland = JSONObject().apply {
            put("imageTextInfoLeft", JSONObject().apply {
                put("type", 1)
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_taxi")
                })
                put("textInfo", JSONObject().apply {
                    put("title", "接驾中")
                    put("content", "3分钟")
                })
            })
            put("imageTextInfoRight", JSONObject().apply {
                put("type", 2)
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_car")
                })
                put("textInfo", JSONObject().apply {
                    put("frontTitle", "京ABZ422")
                    put("title", "白色")
                })
            })
        }

        // 小岛：图标组件
        smallIsland = JSONObject().apply {
            put("picInfo", JSONObject().apply {
                put("type", 1)
                put("pic", "miui.focus.pic_taxi")
            })
        }

        shareTitle = "我正在打车"
        shareContent = "我正在使用XX打车，3分钟后到达"
    }
}

/** 外卖/配送场景 */
fun buildDeliveryParams(): SuperIslandNotifier.IslandParams {
    return SuperIslandNotifier.IslandParams().apply {
        business = "delivery"
        timeout = 60
        updatable = true
        islandFirstFloat = true
        ticker = "骑手已取货"
        aodTitle = "配送中"
        focusTitle = "骑手正在赶来"
        focusContent = "预计12:30送达"
        focusType = 2

        bigIsland = JSONObject().apply {
            put("imageTextInfoLeft", JSONObject().apply {
                put("type", 1)
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_delivery")
                })
                put("textInfo", JSONObject().apply {
                    put("title", "配送中")
                    put("content", "12:30")
                })
            })
            put("textInfo", JSONObject().apply {
                put("title", "张师傅")
                put("content", "距离1.2km")
            })
        }
    }
}

/** 倒计时提醒场景 */
fun buildTimerParams(): SuperIslandNotifier.IslandParams {
    return SuperIslandNotifier.IslandParams().apply {
        business = "timer"
        timeout = 120
        islandFirstFloat = true
        ticker = "计时运行中"
        aodTitle = "计时中"
        focusTitle = "倒计时"
        focusContent = "还剩15:00"

        bigIsland = JSONObject().apply {
            put("imageTextInfoLeft", JSONObject().apply {
                put("type", 1)
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_timer")
                })
                put("textInfo", JSONObject().apply {
                    put("title", "计时中")
                })
            })
            put("sameWidthDigitInfo", JSONObject().apply {
                put("digit", "15:00")
                put("content", "剩余")
            })
        }
    }
}
