# 女性の街ときめいてナビ

「女性の街ときめいてナビ」は、日本語のネイティブ Android アプリです。都道府県、市区町村、ジャンルを順番に選んで、キーボードを使わずに行き先を探せます。検索結果と車での道案内は Google マップで開き、お気に入りは端末内に保存できます。任意の場所・お店の名前を追加して検索することもできます。

市区町村データは [nojimage/local-gov-code-jp](https://github.com/nojimage/local-gov-code-jp) が総務省の全国地方公共団体コードを基に公開しているデータ（令和6年1月1日現在）を使用しています。47都道府県、1,747市区町村をアプリ内に同梱し、オフラインで選択できます。データは同リポジトリの MIT License に基づいて利用し、ライセンス本文を `app/src/main/assets/LICENSE-local-gov-code-jp.txt` に収録しています。

## 開発環境

- Android Studio
- JDK 17
- Android SDK Platform 36（compileSdk / targetSdk）
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
