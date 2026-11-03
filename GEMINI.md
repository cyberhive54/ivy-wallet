# Development Guidelines & Critical Project Rules

## 1. CRITICAL SAFETY: Protect Official Play Store App & User Data
- **Official App Installed**: The user's physical Android phone has the official Ivy Wallet installed from the Google Play Store (`com.ivy.wallet`) containing critical, sensitive, and irreplaceable personal financial data.
- **NEVER Overwrite or Collide**:
  - The custom development version MUST ALWAYS use a distinct Application ID: `com.cyberhive.wallet` (or `com.cyberhive.wallet.debug` for debug builds).
  - NEVER change `applicationId` back to `com.ivy.wallet`.
  - NEVER execute commands that touch `com.ivy.wallet` (e.g., `adb uninstall com.ivy.wallet`, `adb shell pm clear com.ivy.wallet`).
  - Before running any `adb install`, `adb push`, or package-manager command on the connected device, ALWAYS verify the target package is `com.cyberhive.wallet` / `com.cyberhive.wallet.debug`.

## 2. LOW-SPEC HARDWARE CONSTRAINTS: RAM / CPU Protection
- **Low-Spec Machine**: The host machine has limited RAM and CPU resources, and the user runs other concurrent workflows.
- **NEVER Build Autonomously**:
  - DO NOT run Gradle build/assemble commands (`./gradlew assemble`, `./gradlew build`, etc.) automatically or speculatively.
  - ONLY compile or build if the user explicitly asks to build an APK.
- **Strict Resource Caps When Building (Only When Explicitly Requested)**:
  - Keep heap capped: `-Xmx1536m -XX:MaxMetaspaceSize=512m`.
  - Restrict workers: `--max-workers=2` and `org.gradle.parallel=false`.
  - Avoid launching long-running daemon processes or stop them after completion (`./gradlew --stop`).
  - Prefer checking syntax, lint, or single-file/single-module tasks instead of full multi-module compilation whenever possible.

## 3. ENVIRONMENT & TOOLING
- **IDE**: Android Studio is NOT installed. Work is done via code edits and CLI tools.
- **Java**: OpenJDK 17 LTS is installed.
- **Target Device**: Physical Android phone connected via USB with USB debugging enabled.
- **Keystore**: Local debug keystore is at `debug.jks`.

## 4. GIT & BRANCHING WORKFLOW
- **Default Work Branch**: Always work, commit, and push on branch `cyberhive54/update`.
- **Commit Messages**: Write clear, descriptive, professional commit messages following conventional commit standards.
