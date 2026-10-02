# 女性の街ときめいてナビ

「女性の街ときめいてナビ」は、日本語のネイティブ Android アプリです。行きたい場所やお店を検索したり、カフェ・雑貨・公園・美術館のカテゴリやおすすめから行き先を見つけて、Google マップなどの対応地図アプリで開けます。検索には Android の `ACTION_VIEW` と `geo:` URI を使うため、Maps API キーや追加の地図 SDK は不要です。

## 開発環境

- Android Studio
- JDK 17
- Android SDK Platform 34（compileSdk / targetSdk）
- Gradle Wrapper（Gradle 8.7 を自動ダウンロード）

Android Studio でこのリポジトリを開き、必要な Android SDK をインストールして同期してください。コマンドラインでは Gradle を別途インストールせず、リポジトリの Wrapper を実行します。

```shell
bash ./gradlew :app:assembleDebug
bash ./gradlew :app:bundleRelease
```

Windows では `.\gradlew.bat :app:bundleRelease` を実行します。リリース用 AAB は `app/build/outputs/bundle/release/` に生成されます。

## GitHub Actions で AAB をダウンロード

`.github/workflows/android-aab.yml` は任意のブランチへの push、または Actions の **Android AAB** workflow の **Run workflow** から起動できます。成功した実行を開き、ページ下部の **Artifacts** から `tokimeite-nabi-release-aab` をダウンロードしてください（14 日間保存）。

## Google Play への公開

アプリ ID は `com.kenjkenta0813sketch.tokimeitenabi` です。現在、release 署名設定と署名鍵は用意されていません。Actions が生成する AAB は未署名のため、ビルド成果物の確認・ダウンロード用であり、そのまま Google Play に提出できません。Play 公開時にはリリース担当者が署名鍵を安全に用意し、GitHub Secrets などを使った署名設定と Play App Signing の登録を別途行ってください。このリポジトリには署名資格情報を含めていません。
