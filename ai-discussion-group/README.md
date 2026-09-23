# AI 讨论群（Android APK）

把通义千问、腾讯元宝、豆包、DeepSeek 等多家国产 AI 聚合成一个微信群聊风格的手机 App，就你的问题让它们自动讨论并给出结论。

## 功能

- 仿微信聊天气泡、讨论列表式主界面
- 开放式角色管理：自由添加任意 OpenAI 兼容的 AI（API 地址 + Key + 模型名）
- 群聊：提问 → 多个 AI 轮流作答 → 自动辩论收敛 → 主讲输出结论（可手动「提前总结」）
- 单聊：点头像进入与单个 AI 的私聊
- 状态点 + 个性签名
- 系统消息窗（额度不足、网络错误等自动提示）
- 全部数据本地存储（SharedPreferences + Gson），不上传任何服务器

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- OkHttp 调用各 AI 的 `/v1/chat/completions`（OpenAI 兼容协议）
- GitHub Actions 云端打包 APK

## 免费 AI 参考（OpenAI 兼容）

| AI | 接口地址 | 模型示例 |
|---|---|---|
| 智谱 GLM | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash`（永久免费） |
| 硅基流动 | `https://api.siliconflow.cn/v1` | `Qwen/Qwen2.5-7B-Instruct` 等免费层 |
| DeepSeek | `https://api.deepseek.com/v1` | `deepseek-chat` |
| 通义千问 | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-plus` |
| 腾讯元宝（混元） | `https://api.hunyuan.cloud.tencent.com/v1` | `hunyuan-turbos-latest` |
| 豆包（火山方舟） | `https://ark.cn-beijing.volces.com/api/v3` | `doubao-pro-32k`（填入推理接入点 ID） |

> API Key 需要到各平台控制台申请。填写格式以各家 OpenAI 兼容接口为准（多数为 `Bearer <key>`）。

## 云端打包步骤（本地无需装任何环境）

1. 打开 GitHub（https://github.com）→ New repository → 仓库名随意（如 `ai-discussion-group`）→ 创建（不要勾选 README，避免冲突）。
2. 把本项目所有文件上传到仓库（网页端 Upload files 或 Git 客户端 push，推荐把 `ai-discussion-group` 文件夹内全部内容放到仓库根目录）。
3. 打开仓库 → Actions 页签 → 左侧 `Build APK` → 右侧 `Run workflow` → 绿色按钮触发。
4. 等 3-8 分钟构建完成，点击最新一次运行 → 底部 Artifacts 区下载 `ai-discussion-group-apk`。
5. 解压得到 `app-debug.apk`，传到手机安装即可（需允许"安装未知来源应用"）。

> 后续每次推送代码到 main 分支会自动触发构建，无需手动操作。

## 使用说明

1. 打开 App → 右上角设置图标 → 角色管理 → 添加角色：填名称、API 地址、Key、模型名（可自定义颜色和签名）。
2. 返回首页 → 右下角 + → 输入讨论主题、勾选参与 AI、选一个主讲 → 开始讨论。
3. 讨论自动进行多轮，AI 依次发言，最后主讲输出「结论」气泡。
4. 想提前结束：讨论中点击顶部「提前总结」。
5. 点头像可进入与单个 AI 的私聊。

## 项目结构

```
app/src/main/java/com/marvis/aigroup/
├── MainActivity.kt        # 入口 + 全局状态
├── model/                 # Role / Conversation / Message / SystemAlert 数据类
├── data/AppRepository.kt  # 本地存储（SharedPreferences + Gson）
├── network/LlmClient.kt   # OpenAI 兼容 API 客户端
├── logic/DiscussionEngine.kt  # 群聊 / 单聊讨论引擎
└── ui/                    # Compose 界面（列表 / 群聊 / 单聊 / 角色 / 设置 / 系统消息）
```
