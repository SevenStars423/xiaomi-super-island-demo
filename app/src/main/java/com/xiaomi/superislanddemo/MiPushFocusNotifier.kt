package com.xiaomi.superislanddemo

import android.content.Context
import android.util.Log
import org.json.JSONObject

/**
 * MiPush 云端发送焦点通知工具
 *
 * 通过小米推送服务（MiPush）云端下发焦点通知/超级岛通知。
 * 优势：应用无需保活，进程被杀后仍能收到并展示。
 *
 * 前置条件：
 * 1. 在小米开放平台开通 MiPush 推送服务
 * 2. 获取 AppId、AppKey、AppSecret
 * 3. 获取目标设备的 regId
 * 4. 已申请焦点通知权限
 *
 * MiPush SDK 接入：
 * implementation 'com.xiaomi.mipush.sdk:mipush-sdk:6.0.0'
 *
 * 服务端发送（本工具演示的是客户端通过 HTTP API 调用 MiPush 服务端）：
 * POST https://api.xmpush.xiaomi.com/v3/message/regid
 * Authorization: key=<APP_SECRET>
 */
object MiPushFocusNotifier {

    private const val TAG = "MiPushFocus"
    
    // MiPush 服务端地址
    private const val MIPUSH_API = "https://api.xmpush.xiaomi.com"
    // 生产环境（申请焦点通知权限后使用）
    // private const val MIPUSH_API_VIP = "https://vip.api.xmpush.xiaomi.com"

    /**
     * 构建焦点通知的 extra 参数
     *
     * 这些参数在 MiPush Message.Builder 中通过 .extra() 方法传入
     */
    fun buildFocusExtras(params: FocusNotificationParams): Map<String, String> {
        val extras = mutableMapOf<String, String>()

        // 核心焦点参数
        extras["miui.focus.param"] = params.toJson()

        // 图片参数（每个图片≤100KB，宽高比 1:1 ~ 16:9）
        params.pics.forEach { (key, url) ->
            extras["miui.focus.pic_$key"] = url
        }

        return extras
    }

    /**
     * 焦点通知参数
     */
    class FocusNotificationParams {
        // === 基础属性 ===
        var business: String = "demo"
        var timeout: Int = 720       // 分钟
        var updatable: Boolean = false
        var cancel: Boolean = false
        var sequence: Long = 0       // 更新序号（实时更新类必传）

        // === 状态栏 ===
        var ticker: String = ""

        // === 息屏 ===
        var aodTitle: String = ""

        // === 焦点通知内容 ===
        var focusType: Int = 2       // baseInfo type
        var focusTitle: String = ""
        var focusContent: String = ""
        var focusColorTitle: String = ""

        // === 大岛摘要态 ===
        var bigIslandJson: JSONObject? = null
        var smallIslandJson: JSONObject? = null

        // === 分享 ===
        var shareTitle: String = ""
        var shareContent: String = ""

        // === 图片映射 ===
        val pics: MutableMap<String, String> = mutableMapOf()

        fun toJson(): String {
            val root = JSONObject()
            val paramV2 = JSONObject().apply {
                put("protocol", 1)
                put("business", business)
                put("timeout", timeout)
                put("updatable", updatable)

                if (ticker.isNotEmpty()) put("ticker", ticker)
                if (aodTitle.isNotEmpty()) put("aodTitle", aodTitle)
                if (sequence > 0) put("sequence", sequence)
            }

            // 焦点通知内容
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
            if (bigIslandJson != null) paramIsland.put("bigIslandArea", bigIslandJson)
            if (smallIslandJson != null) paramIsland.put("smallIslandArea", smallIslandJson)
            if (shareTitle.isNotEmpty()) {
                paramIsland.put("shareData", JSONObject().apply {
                    put("title", shareTitle)
                    put("content", shareContent)
                })
            }
            if (paramIsland.length() > 0) paramV2.put("param_island", paramIsland)

            root.put("param_v2", paramV2)
            return root.toString()
        }
    }

    // ─── MiPush HTTP API 发送示例（在服务端执行）───

    /**
     * 构建 MiPush 服务端发送请求体
     *
     * 实际项目中这段代码跑在**你自己的服务端**上，不跑在 Android 客户端。
     * 这里给出 JSON 格式参考。
     */
    fun buildServerPayload(
        regId: String,
        title: String,
        description: String,
        notifyId: Int,
        params: FocusNotificationParams,
    ): JSONObject {
        val payload = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("notify_id", notifyId)
            put("pass_through", 0)  // 0=通知栏消息，1=透传消息
            put("extra", JSONObject().apply {
                put("miui.focus.param", params.toJson())
                params.pics.forEach { (k, v) ->
                    put("miui.focus.pic_$k", v)
                }
            })
        }
        return payload
    }

    /**
     * Java SDK 发送示例代码（在你的服务端 Java 项目中使用）
     *
     * ```java
     * // 1. 构造 Sender
     * Sender sender = new Sender("你的APP_SECRET");
     *
     * // 2. 构造 Message
     * String params = "{...miui.focus.param JSON...}";
     * Message message = new Message.Builder()
     *     .title("通知标题")
     *     .description("通知描述")
     *     .notifyId(123456)
     *     .extra("miui.focus.param", params)
     *     .extra("miui.focus.pic_large", "https://your-cdn.com/pic.jpg")
     *     .build();
     *
     * // 3. 发送（regId 模式）
     * Result result = sender.send(message, "目标设备regId", 3);
     * System.out.println("MessageId: " + result.getMessageId());
     * ```
     */
    fun logJavaSdkExample(appSecret: String) {
        Log.i(TAG, """
            |MiPush Java SDK 发送示例：
            |Sender sender = new Sender("$appSecret");
            |Message message = new Message.Builder()
            |    .title("通知标题")
            |    .description("通知描述")
            |    .notifyId(123456)
            |    .extra("miui.focus.param", miuiFocusParamJson)
            |    .extra("miui.focus.pic_large", "图片URL")
            |    .build();
            |Result result = sender.send(message, regId, 3);
        """.trimMargin())
    }

    /**
     * HTTP API 发送示例（curl）
     */
    fun logCurlExample(appSecret: String, regId: String, jsonPayload: String) {
        Log.i(TAG, """
            |curl -X POST "$MIPUSH_API/v3/message/regid" \
            |  -H "Authorization: key=$appSecret" \
            |  -H "Content-Type: application/json" \
            |  -d '$jsonPayload'
        """.trimMargin())
    }
}
