<p align="center">
  <img src="app/src/main/res/drawable-nodpi/usage_app_icon.png" width="120" alt="Codex Usage Widget 圖示">
</p>

# Codex Usage Widget

在 Android 手機桌面查看 Codex 的 **5 小時與每週剩餘額度**，支援手動刷新與定期背景更新。所有額度讀取與更新都在手機執行，不需要電腦常駐。

> 個人開發的非官方實驗專案，與 OpenAI 無隸屬或背書關係。使用官方 Codex App Server，但 Android 執行方式由本專案自行整合，並非官方 Android SDK。

[下載 APK](https://github.com/Hey966/Codex-Usage-Widget/releases/download/v0.2.0/app-release.apk) · [版本紀錄](https://github.com/Hey966/Codex-Usage-Widget/releases) · [回報問題](https://github.com/Hey966/Codex-Usage-Widget/issues)

## 下載與安裝

目前公開版本為 **v0.2.0 公開測試版**，安裝檔名稱為 `app-release.apk`。

1. 使用 Android 手機開啟 [v0.2.0 下載頁](https://github.com/Hey966/Codex-Usage-Widget/releases/tag/v0.2.0)。
2. 展開 **Assets**，下載 **app-release.apk**。也可使用上方「下載 APK」連結。
3. 開啟下載的 APK；若 Android 提示，為正在使用的瀏覽器或檔案管理員允許「安裝未知應用程式」。
4. 完成安裝後開啟 App，依下方步驟登入並加入桌面小工具。

**Source code ZIP / tar.gz 是原始碼，不能直接安裝。**

若原先安裝的是 debug 測試版，可能因簽章不同而無法覆蓋安裝。請先登出並卸載舊版，再安裝此版本；卸載會清除本機資料，需要重新登入與加入 Widget。

### 支援範圍

- 專案最低版本：Android 8.0 / API 26。
- 內建執行環境僅支援 **ARM64 / arm64-v8a**；不支援 32 位元或 x86 裝置／模擬器。
- 已在 **ASUS ROG Phone 6D、Android 14** 驗證登入、額度讀取及 Widget。
- 其他手機與 Android 版本尚未完整驗證；最低版本宣告不代表所有裝置都已測試。
- 需要網路、可使用 Codex 且提供相關額度資訊的帳號，以及支援小工具的桌面。
- 安裝後不需要電腦常駐，也不需要購買或填入 OpenAI API Key。

## 登入與加入桌面

1. 在 App 按「登入 ChatGPT / Codex」。
2. 按「複製代碼並開啟官方登入頁」。
3. 確認頁面為 `https://auth.openai.com/codex/device`，登入自己的帳號並輸入代碼。
4. 回到 App，等待登入完成，再按「立即刷新」。
5. 按「加入桌面 Widget」，或長按桌面空白處 → 小工具 → Codex Usage Widget。
6. 長按 Widget，依桌面提供的控制點調整大小。

請使用自己的帳號登入。不要將登入碼、密碼、憑證或帳號私有資料貼到 Issues。

## 功能

- 顯示 5 小時與每週**剩餘百分比**及進度條。
- 依伺服器回傳時間顯示距離重置的時間。
- 支援 App 內「立即刷新」與 Widget 更新按鈕。
- 約每 15 分鐘嘗試背景更新，實際排程由 Android 決定。
- 顯示更新狀態與最後成功取得資料的時間。
- 更新失敗時保留上次成功的額度；未提供的欄位顯示「—」。
- 同步更新同一 App 的所有桌面小工具。
- 使用深藍底色、薄荷綠／淡藍額度區塊，搭配比例排版與自動調整字級。

顯示的數字是 **Codex 額度百分比**，不是剩餘 token 數、API 帳單餘額或所有 ChatGPT 模型共用的餘額。

## 更新行為與已知限制

- 15 分鐘是排程目標，不是精準計時；省電、休眠、網路與各廠牌背景限制都可能延後更新。
- Widget 更新按鈕會提交背景工作，不保證點擊後立即完成；需要確認時可開啟 App 刷新。
- 倒數文字在重新繪製時更新，不是每秒倒數。到達重置時間後，仍須讀取伺服器結果，不能自行推定額度已恢復。
- 桌面格數與拖曳步幅由 Launcher 控制。更新 App 不一定會改變既有 Widget 的占格。
- 登入期間若 App 程序或內建服務被系統終止，可能需要關閉再開啟 App，重新登入。
- 登入中斷恢復、背景工作取消與多機型相容性仍待改善。
- 額度服務與登入流程依賴上游 Codex 行為；上游變更可能需要更新本 App。

## 資料與權限

- 使用 Codex App Server 的 `account/rateLimits/read` 取得額度，依週期長度辨識 5 小時與每週資料。
- 登入由內建 Codex App Server 處理。憑證存於 App 私有的 `noBackupFilesDir`，不會打包進 APK；目前沒有額外的 Android Keystore 加密層。
- 本機額度快取使用 SharedPreferences。
- 網路轉接器僅綁定手機 loopback，使用隨機代理驗證資訊，限制目的地為 OpenAI／ChatGPT 網域的 HTTPS；TLS 由內建執行環境與遠端服務建立。
- 本專案沒有自建的額度中繼伺服器。內建執行環境會依其行為連接 OpenAI 服務。
- 使用的權限包括網際網路、網路狀態，以及開機後恢復排程。
- 可使用 App 的「登出並清除額度資料」。若登出失敗，App 會顯示提示，此時不能視為已完成清除。

## 從原始碼建置

需要 Android Studio、Android SDK Platform 37、JDK 21 與 Git LFS。

Gradle daemon 設定要求 Java 21；專案 Java 原始碼相容性設定為 11，兩者用途不同。

```sh
git lfs install
git clone https://github.com/Hey966/Codex-Usage-Widget.git
cd Codex-Usage-Widget
git lfs pull
```

使用 Android Studio 開啟專案根目錄，等待 Gradle 同步並設定 Android SDK。

大型原生執行檔由 Git LFS 管理。若只取得文字指標檔，App 無法啟動額度服務。

### 建置與靜態檢查

Windows：

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

macOS / Linux：

```sh
bash ./gradlew :app:assembleDebug :app:lintDebug
```

測試 APK 產物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 手機整合測試

Windows：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.codexusagewidget.PhoneIntegrationTest"
```

測試需要已授權的 ARM64 Android 裝置與網路。未登入時會取得並取消官方裝置登入碼，不會替使用者完成登入。

### 主要程式

| 檔案 | 用途 |
| --- | --- |
| `MainActivity.kt` | 登入、資料查看、刷新及加入 Widget |
| `CodexPhoneRuntime.kt` | 手機內建服務與登入協定 |
| `PhoneNetworkProxy.kt` | 手機本機 HTTPS CONNECT 轉接 |
| `UsageDataRepository.kt` | 額度解析與儲存 |
| `UsageRefresh.kt` | 手動／定期工作排程 |
| `UsageWidgetProvider.kt` | 更新桌面小工具 |

## 維護者：發布新版本

### 產生簽署的 Release APK

儲存庫未配置正式簽章的 `signingConfig`；請使用 Android Studio 的簽署流程產生可供安裝的 Release APK。

在 Android Studio 選 **Build → Generate Signed App Bundle or APK → APK**：

1. 選取自己的正式 keystore。後續版本應沿用相同 keystore 與 key。
2. 選擇 `release` 建置並產生 APK。
3. 妥善備份 keystore、別名與密碼；相同簽章是覆蓋更新的必要條件。
4. 每次更新增加 `app/build.gradle.kts` 的 `versionCode`，並更新 `versionName`。
5. 不要將 keystore、密碼、`auth.json` 或登入憑證提交到 GitHub。
6. 發布前確認第三方授權文件，並完成真機安裝、登入、額度讀取與 Widget 更新測試。

不同簽章的 APK 通常無法覆蓋安裝。不要要求使用者卸載正式版來完成一般更新，因為卸載會清除本機資料。

### 建立 GitHub Release

1. 開啟 [建立 Release](https://github.com/Hey966/Codex-Usage-Widget/releases/new)。
2. 建立新版本標籤，並確認 Target 對應本次發布的程式版本。
3. 填入版本名稱、變更內容、支援範圍與已知限制。
4. 將**已簽署 APK**上傳到 **Attach binaries** 附件區。
5. 測試版本選 **Pre-release**；確認附件上傳完成後發布。
6. 更新 README 的版本與 APK 下載連結。

目前 v0.2.0 的附件名稱為 `app-release.apk`。未來可以使用包含版本與架構的檔名，但修改檔名後，也要同步更新下載連結。

一般使用者應下載 Release 的 APK 附件。只有 pre-release 時，請分享指定版本頁或 Releases 列表，避免使用 `releases/latest` 作為下載入口。

可另外計算 APK 的 SHA-256：

```powershell
Get-FileHash -Algorithm SHA256 .\app-release.apk
```

## 回報問題與參與開發

歡迎透過 [Issues](https://github.com/Hey966/Codex-Usage-Widget/issues) 回報錯誤或提出建議。

請提供：

- 手機型號與 Android 版本。
- App 版本。
- 錯誤文字與重現步驟。
- 預期結果與實際結果。

截圖前請遮住個人帳號資訊。不要上傳密碼、登入碼、`auth.json` 或私有憑證目錄。

歡迎提交 Pull Request。較大的功能或架構調整，建議先建立 Issue 討論。

目前優先改善的方向包括登入中斷恢復、背景工作取消、多機型測試，以及第三方授權文件整理。

## 授權與第三方來源

本專案原創程式碼採用 **MIT License**，詳見 [LICENSE](LICENSE)。

第三方元件保留各自的授權，不因本專案使用 MIT 而變更。

內建 Codex 執行環境來源：

- 上游：[OpenAI Codex 0.157.0](https://github.com/openai/codex/releases/tag/rust-v0.157.0)。
- 來源檔案：`codex-app-server-aarch64-unknown-linux-musl.tar.gz`。
- 專案內位置：`app/src/main/jniLibs/arm64-v8a/libcodex_runtime.so`。
- 上游授權：[Apache License 2.0](https://github.com/openai/codex/blob/rust-v0.157.0/LICENSE)。

**待完成：**整理隨附執行環境及相依元件的授權文字、適用的 NOTICE 與其他第三方聲明，並確認它們隨發行版本提供。此 README 的來源連結不能代替應隨附的授權文件。

## 參考文件

- [Android APK 簽署](https://developer.android.com/studio/publish/app-signing)
- [GitHub Release 操作](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
