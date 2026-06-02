# 小米超级岛 Demo

基于 Xiaomi HyperOS 超级岛通知系统开发的 Android 示例项目。

## 功能

1. **客户端发送超级岛通知** — 支持打车、外卖、倒计时三种场景
2. **微信通知桥接** — 监听微信新消息，以超级岛形式重新展示
3. **系统状态检测** — 检测岛支持、焦点协议版本、权限状态

## 环境要求

- Android 9.0+ (API 28+)
- 小米设备 HyperOS 2.0+（OS2 支持焦点通知，OS3 支持完整超级岛）
- Android Studio Hedgehog+

## 快速开始

### 1. 克隆项目

```bash
cd xiaomi-super-island-demo
```

### 2. 配置 AppId

在 `app/src/main/AndroidManifest.xml` 中替换：

```xml
<meta-data
    android:name="com.xiaomi.xms.APP_ID"
    android:value="YOUR_APP_ID_HERE" />
```

AppId 在小米开放平台开通超级岛服务后获取。

### 3. 授予通知监听权限（桥接功能）

1. 安装应用后，点击「开启通知监听权限」
2. 在系统设置中勾选本应用
3. 微信需开启「通知显示消息详情」

### 4. 微信桥接原理

```
微信发通知 → NotificationListenerService 拦截
  → 提取 title + text + bigText
  → BridgeNotifier 重塑为"待处理任务"通知
  → SuperIslandNotifier 以超级岛格式重新发出
```

## 项目结构

```
app/src/main/java/com/xiaomi/superislanddemo/
├── MainActivity.kt              # 主界面（发送按钮 + 状态检测）
├── SuperIslandNotifier.kt       # 超级岛通知发送工具
│   ├── IslandParams             # 参数构建器
│   ├── buildTaxiParams()        # 打车场景快捷工厂
│   ├── buildDeliveryParams()    # 外卖场景快捷工厂
│   └── buildTimerParams()       # 倒计时场景快捷工厂
├── BridgeNotifier.kt            # 通知桥接发射器
├── WeChatNotificationBridge.kt  # 微信通知监听服务
├── SuperIslandActionReceiver.kt # 超级岛 Action 点击处理
└── SuperIslandForegroundService.kt # 前台保活服务
```

## 接入流程

完整接入小米超级岛需要以下步骤：

1. **注册小米开发者账号**
   - 访问 https://dev.mi.com
   - 企业认证（1-3 工作日审核）

2. **创建应用**
   - 完善应用资料，上传包体

3. **开通超级岛服务**
   - 进入开放服务 → 小米超级岛 → 启用
   - 配置指纹证书，获取 AppId

4. **场景方案提报**
   - 预审（描述业务场景）
   - 正式审核（完整 UI 方案 + 所有状态节点）

5. **联调测试**
   - 设备白名单管理（OAID，最多 10 台，30 天有效）
   - AndroidManifest.xml 配置 AppId + Debug 标识

6. **上线验证**
   - 提交正式 APK + 验证方法
   - 灰度 7-15 天全量

## 图片来源规范

| 位置 | 尺寸 | 格式 |
|------|------|------|
| 摘要态图标 | ≥88×88px | 正方形 |
| 展开态应用图标 | ≥96×96px | 正方形 |
| 展开态功能图标 | ≥80×80px | 正方形 |
| 大图组件 | ≥224×224px | 正方形 |
| 进度组件图形 | ≥240×188px | — |
| MiPush 图片 | ≤100KB | HTTPS |

## 关键技术参数

| 参数 | 限制 |
|------|------|
| miui.focus.param | ≤3072 字节 |
| MiPush 图片 | 单张 ≤100KB，宽高比 1:1 ~ 16:9 |
| 单个通知图片数量 | ≤10 张 |
| 岛默认消失时间 | 60 分钟 |
| 通知默认消失时间 | 720 分钟 |
| 焦点通知权限 | 默认随通知权限开启 |

## 调试

```bash
# 查看焦点通知日志（小米设备）
*#*#284#*#*
# 日志位置：sdcard/MIUI/debug_log/

# adb 查看焦点通知协议版本
adb shell settings get system notification_focus_protocol

# 检查岛功能支持
adb shell getprop persist.sys.feature.island
```

## License

MIT
