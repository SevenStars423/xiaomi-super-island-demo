package com.xiaomi.superislanddemo

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.json.JSONObject

/**
 * 纯 OS3 格式超级岛通知发送器
 *
 * OS3 超级岛完整格式：
 * - param_v2.protocol = 1
 * - param_island.bigIslandArea (大岛 A+B 区)
 * - param_island.smallIslandArea (小岛)
 * - baseInfo (焦点通知展开态)
 * - ticker (状态栏)
 * - aodTitle (息屏)
 */
object Os3Notifier {

    private const val CHANNEL_ID = "island"
    private const val KEY_PARAM = "miui.focus.param"

    private var idCounter = 5000

    private fun send(ctx: Context, json: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val n = Notification.Builder(ctx, CHANNEL_ID)
            .setContentTitle("超级岛")
            .setContentText("OS3 测试")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setOngoing(false)
            .build()

        n.extras.putString(KEY_PARAM, json)
        ctx.getSystemService(android.app.NotificationManager::class.java)
            .notify(++idCounter, n)
    }

    // ═══════════════════════════════════════
    // 最简 OS3 岛通知 — 只测试岛形态是否出现
    // ═══════════════════════════════════════
    fun sendMinimal(ctx: Context) {
        val json = JSONObject().apply {
            put("param_v2", JSONObject().apply {
                put("protocol", 1)
                put("business", "test")
                put("timeout", 120)
                put("updatable", true)
                put("islandFirstFloat", true)
                put("ticker", "岛通知测试")
                put("aodTitle", "测试")

                // 岛数据
                put("param_island", JSONObject().apply {
                    put("islandProperty", 1)
                    put("islandTimeout", 300)
                    // 大岛：图文组件1 + 空（最简）
                    put("bigIslandArea", JSONObject().apply {
                        put("imageTextInfoLeft", JSONObject().apply {
                            put("type", 1)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)
                            })
                            put("textInfo", JSONObject().apply {
                                put("title", "测试")
                                put("content", "OS3")
                            })
                        })
                    })
                    // 小岛：图标
                    put("smallIslandArea", JSONObject().apply {
                        put("picInfo", JSONObject().apply {
                            put("type", 1)
                        })
                    })
                })

                // 焦点通知内容
                put("baseInfo", JSONObject().apply {
                    put("type", 2)
                    put("title", "超级岛测试")
                    put("content", "这是一条 OS3 岛通知")
                })
            })
        }
        send(ctx, json.toString())
    }

    // ═══════════════════════════════════════
    // 打车场景
    // ═══════════════════════════════════════
    fun sendTaxi(ctx: Context) {
        val json = JSONObject().apply {
            put("param_v2", JSONObject().apply {
                put("protocol", 1)
                put("business", "taxi")
                put("timeout", 30)
                put("updatable", true)
                put("islandFirstFloat", true)
                put("enableFloat", true)
                put("ticker", "行程进行中")
                put("aodTitle", "接驾中")

                put("param_island", JSONObject().apply {
                    put("islandProperty", 1)
                    put("islandTimeout", 600)

                    // 大岛 A区：图文组件1 — "接驾中 / 3分钟"
                    put("bigIslandArea", JSONObject().apply {
                        put("imageTextInfoLeft", JSONObject().apply {
                            put("type", 1)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)
                            })
                            put("textInfo", JSONObject().apply {
                                put("title", "接驾中")
                                put("content", "3分钟")
                                put("showHighlightColor", true)
                            })
                        })
                        // B区：图文组件2 — "京A·BZ422 / 白色"
                        put("imageTextInfoRight", JSONObject().apply {
                            put("type", 2)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)
                            })
                            put("textInfo", JSONObject().apply {
                                put("frontTitle", "京ABZ422")
                                put("title", "白色")
                                put("showHighlightColor", false)
                            })
                        })
                    })

                    // 小岛
                    put("smallIslandArea", JSONObject().apply {
                        put("picInfo", JSONObject().apply {
                            put("type", 1)
                        })
                    })

                    put("shareData", JSONObject().apply {
                        put("title", "我正在使用XX打车")
                        put("content", "3分钟后到达")
                    })
                })

                put("baseInfo", JSONObject().apply {
                    put("type", 2)
                    put("title", "司机已接单")
                    put("content", "京ABZ422 · 白色 · 3分钟到达")
                    put("colorTitle", "#006EFF")
                })
            })
        }
        send(ctx, json.toString())
    }

    // ═══════════════════════════════════════
    // 外卖配送场景
    // ═══════════════════════════════════════
    fun sendDelivery(ctx: Context) {
        val json = JSONObject().apply {
            put("param_v2", JSONObject().apply {
                put("protocol", 1)
                put("business", "delivery")
                put("timeout", 60)
                put("updatable", true)
                put("islandFirstFloat", true)
                put("ticker", "骑手已取餐")
                put("aodTitle", "配送中")

                put("param_island", JSONObject().apply {
                    put("islandProperty", 1)
                    put("islandTimeout", 600)

                    put("bigIslandArea", JSONObject().apply {
                        put("imageTextInfoLeft", JSONObject().apply {
                            put("type", 1)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)
                            })
                            put("textInfo", JSONObject().apply {
                                put("title", "配送中")
                                put("content", "12:30")
                            })
                        })
                        put("textInfo", JSONObject().apply {
                            put("title", "张师傅")
                            put("content", "1.2km")
                        })
                    })

                    put("smallIslandArea", JSONObject().apply {
                        put("picInfo", JSONObject().apply {
                            put("type", 1)
                        })
                    })
                })

                put("baseInfo", JSONObject().apply {
                    put("type", 2)
                    put("title", "骑手正在赶来")
                    put("content", "预计12:30送达 · 张师傅")
                    put("colorTitle", "#FF6600")
                })
            })
        }
        send(ctx, json.toString())
    }

    // ═══════════════════════════════════════
    // 倒计时场景
    // ═══════════════════════════════════════
    fun sendTimer(ctx: Context) {
        val json = JSONObject().apply {
            put("param_v2", JSONObject().apply {
                put("protocol", 1)
                put("business", "timer")
                put("timeout", 120)
                put("updatable", true)
                put("islandFirstFloat", true)
                put("ticker", "计时中")
                put("aodTitle", "倒计时")

                put("param_island", JSONObject().apply {
                    put("islandProperty", 1)
                    put("islandTimeout", 3600)

                    put("bigIslandArea", JSONObject().apply {
                        put("imageTextInfoLeft", JSONObject().apply {
                            put("type", 1)
                            put("picInfo", JSONObject().apply {
                                put("type", 1)
                            })
                            put("textInfo", JSONObject().apply {
                                put("title", "倒计时")
                            })
                        })
                        // 等宽数字组件
                        put("sameWidthDigitInfo", JSONObject().apply {
                            put("digit", "15:00")
                            put("content", "剩余")
                        })
                    })

                    put("smallIslandArea", JSONObject().apply {
                        put("picInfo", JSONObject().apply {
                            put("type", 1)
                        })
                    })
                })

                put("baseInfo", JSONObject().apply {
                    put("type", 1)
                    put("title", "倒计时")
                    put("content", "剩余 15:00")
                })
            })
        }
        send(ctx, json.toString())
    }
}
