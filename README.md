# Local Survey App

![Local Survey Banner](opma_banner.png)

このアプリは、イベントや展示会などオフライン環境で利用できる、タブレット向けのシンプルな満足度アンケートアプリです。

## 主な特徴

- **4段階の満足度評価**：
    - 😊 大変満足
    - 🙂 満足
    - 😐 不満
    - 😠 大変不満
- **ワンタップで記録**：ボタンを押すだけで即時に記録。
- **クールダウン機能**：連打や誤操作防止のため、5秒間ボタンが無効化されます。
- **多言語対応**：日本語・英語の両方に対応。UIは端末の言語設定に自動で切り替わります。
- **データは常に日本語で記録**：どちらの言語モードでも、ログ（CSV）は日本語で統一されます。
- **オフライン動作**：インターネット接続不要。
- **ログ閲覧・削除・共有**：アプリ内で記録一覧の閲覧・削除・CSVファイルの共有が可能。
- **グラフ表示**：
    - 直近1週間の日別回答数を棒グラフで可視化
    - 満足度分布を円グラフで表示
- **バナー画像表示**：画面下部にカスタムバナーを表示可能

## 画面イメージ

- **アンケート画面**：4つの絵文字ボタン＋バナー画像
- **ログ画面**：
    - 直近1週間の棒グラフ
    - 満足度分布の円グラフ
    - 生ログ一覧
    - ログの共有・削除ボタン

## 使い方

1. アプリを起動し、絵文字ボタンをタップして満足度を記録。
2. 「ありがとう」メッセージが表示され、5秒後に再度入力可能。
3. ログ閲覧は右上のメニュー（三本線）からパスワード入力でアクセス。
4. ログ画面で棒グラフ・円グラフ・生ログを確認。
5. 右上の共有ボタンでCSVを他アプリへ送信可能。

## データ仕様

- **保存先**：アプリ内ストレージ `survey_log.csv`
- **CSVフォーマット**：
  `yyyy-MM-dd HH:mm:ss,大変満足` のように、日時と評価（日本語）で記録
- **例**：
  `2025-08-18 10:30:00,大変満足`

## ビルド方法

### 必要環境
- Android Studio
- JDK 17

### 手順
1. このリポジトリをクローン
    ```bash
    git clone <repository-url>
    ```
2. Android Studioで開く
3. 実機またはエミュレータを接続
4. ビルド＆実行

コマンドラインからビルドする場合：
```bash
./gradlew installDebug
```

## ライセンス

MIT License

---

### English Summary

This is a simple offline survey app for Android tablets. It supports both Japanese and English UI, but all logs are always recorded in Japanese for consistency. The app features a 4-level satisfaction rating, cooldown to prevent spamming, in-app log viewer, CSV export, and visualizes recent responses with bar and pie charts.