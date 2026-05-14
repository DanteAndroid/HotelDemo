# HotelDemo（悦享酒店）

一个基于 Android Jetpack Compose 的酒店预订 Demo，后端使用 Supabase（Postgres + PostgREST）提供房间列表、预订、订单查询、办理入住等能力。

## 功能概览

- 房间列表：按房型筛选，展示价格/面积/人数/楼层
- 房间详情：设施、介绍，底部快捷预订入口
- 预订：选择入住/退房日期，填写入住人信息，提交生成订单号
- 订单查询：按手机号或订单号查询，展示状态与时间信息，支持一键跳转办理入住
- 办理入住：输入/带入订单号查询，确认后更新订单状态为已入住

## 技术栈

**前端**

- Android：Kotlin + Jetpack Compose + Material 3
- 网络：Supabase Kotlin（`postgrest-kt`）+ Ktor OkHttp
- 图片：Coil 3（`coil-compose`）
- 导航：`androidx.navigation.compose`

**后端**

- Supabase：Postgres 数据库 + PostgREST（自动 REST API）
- 数据表/SQL 初始化：`SUPABASE_SCHEMA.sql`

## 项目结构

- `app/src/main/java/com/danteandroid/hoteldemo/HotelApp.kt`：应用壳（导航 + 底部栏）
- `app/src/main/java/com/danteandroid/hoteldemo/ui/**`：各页面与 ViewModel（首页/详情/预订/订单/入住）
- `app/src/main/java/com/danteandroid/hoteldemo/data/model/**`：数据模型（`Room`/`Booking`）
- `app/src/main/java/com/danteandroid/hoteldemo/data/repository/**`：数据访问（Room/Booking 的 PostgREST 调用）
- `app/src/main/java/com/danteandroid/hoteldemo/SupabaseModule.kt`：Supabase Client 初始化
- `SUPABASE_SCHEMA.sql`：数据库初始化脚本

## 后端（Supabase）配置

1. 创建 Supabase 项目
2. 打开 Supabase 控制台的 SQL Editor
3. 将 `SUPABASE_SCHEMA.sql` 内容粘贴并执行（会创建 `rooms`、`bookings` 等表与必要约束/默认值）
4. 在 Supabase 控制台找到 Project URL 和 anon key

当前工程将 Supabase 配置写在 `app/build.gradle.kts` 的 `BuildConfig` 字段里：

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`

你需要把它们替换为你自己的项目配置后再运行。

说明：anon key 不是私钥，但仍建议在真实项目中使用更合理的配置注入方式（例如本地 `local.properties`/CI 变量），避免把项目配置硬编码到仓库历史中。

## 前端（Android）运行

### Android Studio

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle Sync 完成
3. 选择一个模拟器或真机
4. 运行 `app` 配置

### 命令行

构建 Debug APK：

```bash
./gradlew :app:assembleDebug
```

安装到已启动的模拟器/真机（需要 `adb`）：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

启动应用：

```bash
adb shell am start -n com.danteandroid.hoteldemo/.MainActivity
```

## 数据与接口说明

本项目未实现自建后端服务，客户端直接通过 Supabase PostgREST 访问表：

- `rooms`
  - 列表：只取 `is_available = true`
  - 详情：按 `id` 查询
- `bookings`
  - 创建：插入预订信息并返回订单（含 `booking_code`）
  - 查询：按手机号（`guest_phone`）或订单号（`booking_code`）查询
  - 入住：更新订单 `status` 为 `checked_in`

对应代码位置：

- `app/src/main/java/com/danteandroid/hoteldemo/data/repository/RoomRepository.kt`
- `app/src/main/java/com/danteandroid/hoteldemo/data/repository/BookingRepository.kt`

## 常见问题

- 详情页加载慢：当前实现会按 `roomId` 走一次 PostgREST 网络请求（以及图片网络加载），网络质量会直接影响体验。
- 查询不到数据：请确认已执行 `SUPABASE_SCHEMA.sql`，并且在 Supabase 表里已有样例 `rooms` 数据。
- 无法连接 Supabase：检查 `SUPABASE_URL` 与 `SUPABASE_ANON_KEY` 是否配置正确。

