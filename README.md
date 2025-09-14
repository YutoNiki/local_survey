# Local Survey App

![Local Survey Banner](opma_banner.png)

このアプリは、イベントや展示会などオフライン環境で利用できる、タブレット向けのシンプルな満足度アンケートアプリです。

## 主な特徴

- **ユーザー種別選択**:
    - 回答の前に「日本人」または「Foreigner」を選択します。
- **動的な多言語対応**:
    - ユーザー種別で「日本人」を選択するとUIが日本語に、「Foreigner」を選択すると英語に自動で切り替わります。
- **5段階の満足度評価**:
    - 😊 大変満足
    - 🙂 満足
    - 😐 普通
    - 😕 不満
    - 😠 大変不満
- **ワンタップで記録**: ボタンを押すだけで即時に記録。
- **クールダウン機能**: 回答後、数秒間は次の操作ができなくなり、自動で最初のユーザー選択画面に戻ります。
- **評価データは日本語で記録**: UI言語に関わらず、ログ（CSV）に残る評価は「大変満足」「満足」などの日本語テキストで統一されます。
- **オフライン動作**: インターネット接続不要。
- **パスワード保護されたログ**: 右上のメニューからパスワード（`1988`）を入力することで、ログ画面にアクセスできます。
- **ログ閲覧・削除・共有**: アプリ内で記録一覧の閲覧、全削除、CSVファイルの共有が可能です。
- **グラフによる可視化**:
    - **週間回答数**: 直近1週間の日別回答数を棒グラフで表示します。
    - **満足度分布**: 「日本人」「Foreigner」それぞれの満足度を円グラフで表示します。各グラフにはグループの合計人数も表示されます。
- **バナー画像表示**: 画面下部に常にバナー画像を表示します。

## 画面イメージ

- **ユーザー種別選択画面**: 「日本人」「Foreigner」の選択ボタン。
- **アンケート画面**: 5つの絵文字ボタン＋バナー画像。
- **ログ画面**:
    - 週間回答数の棒グラフ。
    - 「日本人」「Foreigner」別の円グラフ（合計人数付き）。
    - 生ログ一覧。
    - ログの共有・削除ボタン。

## 使い方

1. アプリを起動し、「日本人」または「Foreigner」を選択します。
2. 選択した言語でアンケート画面が表示されたら、絵文字ボタンをタップして満足度を記録します。
3. 「ご意見ありがとうございます！」というメッセージが表示され、数秒後に自動で最初の選択画面に戻ります。
4. ログを閲覧するには、画面右上のメニューアイコンをタップし、パスワード（`1988`）を入力します。
5. ログ画面でグラフや生ログを確認できます。
6. ログ画面右上のボタンから、全ログの削除やCSVファイルのエクスポートができます。

## データ仕様

- **保存先**: アプリ内ストレージ `survey_log.csv`
- **CSVフォーマット**:
  `yyyy-MM-dd HH:mm:ss,ユーザー種別,評価` のように、日時・ユーザー種別・評価（日本語）で記録されます。
- **例**:
  `2025-09-14 10:30:00,日本人,大変満足`
  `2025-09-14 10:31:15,Foreigner,満足`

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

コマンドラインからビルド・インストールする場合：
```bash
./gradlew installDebug
```
デバッグ用APKファイルを生成する場合：
```bash
./gradlew assembleDebug
```

## ライセンス

MIT License

---

### English Summary

This is a simple offline survey app for Android tablets.

**Features**:
- **User Type Selection**: Users first select "Japanese" or "Foreigner".
- **Dynamic UI Language**: The UI switches to Japanese or English based on the user type selection.
- **5-Level Satisfaction Rating**: Users can record their satisfaction level with a single tap.
- **Cooldown & Auto-Reset**: After providing feedback, the screen resets to the initial user selection screen.
- **Consistent Logging**: The rating value in the log file (CSV) is always recorded in Japanese for data consistency, regardless of the UI language.
- **Offline Functionality**: No internet connection required.
- **Password-Protected Logs**: Log screen is accessible via a password (`1988`).
- **Log Management**: View, delete, and share logs as a CSV file from within the app.
- **Data Visualization**: The log screen displays a bar chart for weekly responses and separate pie charts for "Japanese" and "Foreigner" satisfaction rates, including the total count for each group.
- **CSV Format**: `yyyy-MM-dd HH:mm:ss,UserType,RatingInJapanese`
