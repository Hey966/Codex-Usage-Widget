<p align="center">
  <img src="app/src/main/res/drawable-nodpi/usage_app_icon.png" width="120" alt="Codex Usage Widget 圖示">
</p>

# Codex Usage Widget

在 Android 桌面查看 Codex 的 **5 小時與每週剩餘額度**，支援手動刷新與定期背景更新。

> 個人開發的非官方實驗專案，與 OpenAI 無隸屬或背書關係。手機整合使用官方 Codex App Server，但 Android 執行方式由本專案自行整合，並非官方 Android SDK。

[下載與版本紀錄](https://github.com/Hey966/Codex-Usage-Widget/releases) · [回報問題](https://github.com/Hey966/Codex-Usage-Widget/issues)

## 下載與安裝

目前尚未發布可下載的 Release APK。原始碼 ZIP 不能直接安裝；發布後，請依下列步驟下載：

1. 用 Android 手機開啟上方「下載與版本紀錄」。
2. 選擇要安裝的版本，展開 **Assets**。
3. 下載副檔名為 **.apk** 的檔案，例如 `Codex-Usage-Widget-v0.2.0-arm64.apk`。不要選 Source code ZIP / tar.gz。
4. 開啟下載的 APK；如果 Android 提示，為正在使用的瀏覽器或檔案管理員允許「安裝未知應用程式」。
5. 完成安裝後開啟 App。

公開 Release 的附件一般不需要 GitHub 帳號即可下載。APK 的可用性以 Releases 頁面為準。

### 支援範圍

- 專案最低版本：Android 8.0 / API 26。
- 內建執行環境只有 **ARM64 / arm64-v8a**；不支援 32 位元或 x86 手機／模擬器。
- 已在 **ASUS ROG Phone 6D、Android 14** 驗證登入、額度讀取及 Widget。
- 其他手機與 Android 版本尚未完整驗證；最低版本宣告不代表所有裝置都已測試。
- 需要網路、可登入 Codex 且提供相關額度資訊的帳號，以及支援小工具的桌面。
- 安裝後不需要電腦常駐，不需要購買或填入 OpenAI API Key。

## 登入與加入桌面

1. 在 App 按「登入 ChatGPT / Codex」。
2. 按「複製代碼並開啟官方登入頁」。
3. 確認頁面為 `https://auth.openai.com/codex/device`，登入自己的帳號並輸入代碼。
4. 回到 App，按「立即刷新」。
5. 按「加入桌面 Widget」，或長按桌面空白處 → 小工具 → Codex Usage Widget。
6. 長按 Widget，依桌面提供的控制點調整大小。

請使用自己的帳號登入；不要將登入碼、憑證或帳號私有資料貼到 Issues。

## 功能

- 5 小時與每週**剩餘百分比**及進度條。
- 依伺服器回傳時間顯示距離重置的時間。
- App 內「立即刷新」、桌面 Widget「更新」。
- 約每 15 分鐘嘗試背景更新，實際排程由 Android 決定。
- 顯示更新狀態與最後成功取得資料的時間。
- 網路錯誤時保留上次成功的額度；未提供的欄位顯示「—」。
- 同步更新同一 App 的所有桌面小工具。
- 深藍底色、薄荷綠／淡藍額度區塊，比例排版與自動調整字級。

這裡顯示的是 Codex 額度百分比，不是剩餘 token 數、API 帳單餘額或所有 ChatGPT 模型共用的餘額。

## 更新行為與限制

- 15 分鐘是排程目標，不是精準計時；省電、休眠、網路與各廠牌背景限制都可能延後。
- 桌面更新按鈕會提交背景工作，並非保證點擊後立刻完成；需要確認時可開啟 App 刷新。
- 倒數文字在重新繪製時更新，不是每秒倒數。到了重置時間仍要讀取伺服器結果，不能自行推定額度已恢復。
- 桌面格數與拖曳步幅由 Launcher 控制。更新 App 不一定會改變舊 Widget 的占格。
- 登入期間若 App 程序或內建服務被系統終止，可能需要關閉再開啟 App，重新登入。
- 目前缺少完整的登入中斷恢復、背景工作取消與多機型測試，建議先以測試版分享。

## 資料與權限

- 使用 `account/rateLimits/read` 取得額度，依週期長度辨識 5 小時與每週資料。
- 登入由內建 Codex App Server 處理。憑證存於 App 私有的 `noBackupFilesDir`，不會打包進 APK；目前沒有額外的 Android Keystore 加密層。
- 本機額度快取使用 SharedPreferences。
- 網路轉接器僅綁定手機 loopback，使用隨機代理驗證資訊，限制目的地為 OpenAI／ChatGPT 網域的 HTTPS；TLS 由官方執行環境與遠端服務建立。
- 專案自身沒有中繼伺服器。內建官方執行環境可能依其行為連接 OpenAI 服務。
- 權限：網際網路、網路狀態及開機後恢復排程。
- 可使用 App 的「登出並清除額度資料」。登出失敗時會顯示提示，不能視為已清除。

## 從原始碼建置

需要 Android Studio、Android SDK Platform 37、JDK 21 與 Git LFS。Gradle daemon 設定要求 Java 21；專案 Java 原始碼相容性設定為 11，兩者用途不同。

```sh
git lfs install
git clone https://github.com/Hey966/Codex-Usage-Widget.git
cd Codex-Usage-Widget
git lfs pull
```

用 Android Studio 開啟專案根目錄，等待同步並設定 SDK。大型原生執行檔由 Git LFS 管理，若只取得文字指標檔，App 無法啟動額度服務。

Windows：

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

macOS / Linux：

```sh
bash ./gradlew :app:assembleDebug :app:lintDebug
```

測試 APK 產物：`app/build/outputs/apk/debug/app-debug.apk`。

手機整合測試：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.codexusagewidget.PhoneIntegrationTest"
```

測試需要已授權的 ARM64 Android 裝置與網路。未登入時會取得並取消官方裝置登入碼；不會替使用者完成登入。

### 主要程式

| 檔案 | 用途 |
| --- | --- |
| `MainActivity.kt` | 登入、資料查看、刷新及加入 Widget |
| `CodexPhoneRuntime.kt` | 手機內建服務與登入協定 |
| `PhoneNetworkProxy.kt` | 手機本機 HTTPS CONNECT 轉接 |
| `UsageDataRepository.kt` | 額度解析與儲存 |
| `UsageRefresh.kt` | 手動／定期工作排程 |
| `UsageWidgetProvider.kt` | 更新桌面小工具 |

## 維護者：發布 APK 給其他人

### 1. 產生正式簽署的 APK

目前儲存庫沒有配置正式 signingConfig。不要直接把未簽署的 Release APK 上傳給使用者。

在 Android Studio 選 **Build → Generate Signed App Bundle or APK → APK**：

1. 建立或選取自己的正式 keystore。
2. 選擇 `release` 建置並產生 APK。
3. 妥善備份 keystore、別名與密碼。後續版本必須使用相同簽章才能覆蓋更新。
4. 每次更新增加 `app/build.gradle.kts` 的 `versionCode`，並更新 `versionName`。
5. 不要將 keystore、密碼、`auth.json` 或登入憑證提交到 GitHub。
6. 發布前完成第三方授權文件整理與真機安裝測試。

若手機原先安裝的是 debug 簽章版本，正式簽章版本通常無法直接覆蓋。先登出並卸載測試版，再安裝正式版；這會清除本機設定，需要重新登入與加入 Widget。

### 2. 建立 GitHub Release

1. 開啟 [建立 Release](https://github.com/Hey966/Codex-Usage-Widget/releases/new)。
2. 建立對應標籤，例如 `v0.2.0`，確認 Target 是準備發布的程式版本。
3. 填入版本名稱、功能、支援 ARM64 與已知限制。
4. 將**已簽署 APK**拖到附件區；建議命名 `Codex-Usage-Widget-v0.2.0-arm64.apk`。
5. 初期選 **This is a pre-release**，確認附件後按 **Publish release**。
6. 更新本 README 的「尚未發布」狀態，分享該版本 Release 網址。

不要把 APK 放進原始碼資料夾。Release 附件才是供一般使用者安裝的檔案；測試版請分享 Releases 或指定版本頁，`releases/latest` 不適合作為只有 pre-release 時的下載入口。

可另外附上 APK 的 SHA-256：

```powershell
Get-FileHash -Algorithm SHA256 .\Codex-Usage-Widget-v0.2.0-arm64.apk
```

## 回報問題

請附上手機型號、Android 版本、App 版本、錯誤文字與重現步驟。截圖前遮住個人帳號資訊；不要上傳私有憑證目錄。

## 第三方來源與授權狀態

內建執行環境來源：[OpenAI Codex 0.157.0](https://github.com/openai/codex/releases/tag/rust-v0.157.0)，檔案為 `codex-app-server-aarch64-unknown-linux-musl.tar.gz`，包裝後置於 `app/src/main/jniLibs/arm64-v8a/libcodex_runtime.so`。

目前尚未為本專案原創程式選定 LICENSE，也尚未完整整理隨附執行環境及相依套件的授權／NOTICE 文件。公開提供原始碼不等於已授予任意重散布授權；發布 APK 前應補齊相關文件。

參考：[Android APK 簽署](https://developer.android.com/studio/publish/app-signing) · [GitHub Release 操作](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)

