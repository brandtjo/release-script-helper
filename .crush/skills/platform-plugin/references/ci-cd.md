# CI/CD Reference

## GitHub Actions Workflows

The template provides three workflows in `.github/workflows/`:

### build.yml — Build & Test

Triggered on push to `main` and all pull requests.

**Jobs**:
1. **build** — Sets up Java 21 + Gradle, runs `buildPlugin`, uploads ZIP as artifact
2. **test** — Runs `check` (tests + coverage)
3. **verify** — Runs `verifyPlugin` (Plugin Verifier against target IDE versions)
4. **releaseDraft** — Creates a draft GitHub release with changelog (only on push, not PR)

**Key steps**:
```yaml
- uses: actions/setup-java@v5
  with:
    distribution: zulu
    java-version: 21

- uses: gradle/actions/setup-gradle@v5

- run: ./gradlew buildPlugin

- uses: actions/upload-artifact@v6
  with:
    name: my-plugin
    path: ./build/distributions/content/*/*
```

### release.yml — Publish & Release

Triggered when a GitHub release is published (prereleased or released events).

**Jobs**:
1. **release** — Patches changelog, publishes to Marketplace, uploads release asset, creates changelog PR

**Required secrets**:
| Secret | Description |
|--------|-------------|
| `PUBLISH_TOKEN` | JetBrains Marketplace publishing token |
| `PRIVATE_KEY` | RSA private key (PEM format) |
| `PRIVATE_KEY_PASSWORD` | Key password |
| `CERTIFICATE_CHAIN` | Certificate chain (PEM format) |
| `GITHUB_TOKEN` | Auto-provided by GitHub Actions |

**Key steps**:
```yaml
- name: Patch Changelog
  if: ${{ github.event.release.body != '' }}
  env:
    CHANGELOG: ${{ github.event.release.body }}
  run: ./gradlew patchChangelog --release-note-file=./build/tmp/release_note.txt

- name: Publish Plugin
  env:
    PUBLISH_TOKEN: ${{ secrets.PUBLISH_TOKEN }}
    CERTIFICATE_CHAIN: ${{ secrets.CERTIFICATE_CHAIN }}
    PRIVATE_KEY: ${{ secrets.PRIVATE_KEY }}
    PRIVATE_KEY_PASSWORD: ${{ secrets.PRIVATE_KEY_PASSWORD }}
  run: ./gradlew publishPlugin

- name: Upload Release Asset
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  run: gh release upload ${{ github.event.release.tag_name }} ./build/distributions/*
```

### run-ui-tests.yml — UI Tests

Triggered manually. Runs UI tests in a matrix (macOS, Windows, Linux).

Adapt this workflow for your plugin's UI test setup using [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot).

## Plugin Signing

### Generating Certificates

```bash
# Generate RSA key pair
openssl genrsa -out private_key.pem 4096

# Create self-signed certificate
openssl req -new -x509 -key private_key.pem -out certificate_chain.pem \
    -days 3650 -subj "/CN=YourName/O=YourOrg/L=City/ST=State/C=US"
```

### Signing Configuration

**Via environment variables** (recommended for CI):
```bash
export PRIVATE_KEY="-----BEGIN RSA PRIVATE KEY-----\n...\n-----END RSA PRIVATE KEY-----"
export PRIVATE_KEY_PASSWORD="yourpassword"
export CERTIFICATE_CHAIN="-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
```

**Via build.gradle.kts**:
```kotlin
intellijPlatform {
    signing {
        certificateChain = providers.file("certificate_chain.pem")
        privateKey = providers.file("private_key.pem")
        password = providers.provider { "yourpassword" }
    }
}
```

## Marketplace Publishing

### Creating a Marketplace Account

1. Go to [JetBrains Marketplace](https://plugins.jetbrains.com)
2. Sign in or create account
3. Go to **Profile → Publish a Plugin**
4. Manually create the plugin listing first (name, description, category, etc.)
5. Generate a publishing token at **Profile → My Tokens**

### Publishing Token

Add to GitHub secrets:
```
Settings → Secrets and variables → Actions → New repository secret
Name: PUBLISH_TOKEN
Value: <token from JetBrains profile>
```

### Publishing Locally

```bash
export PUBLISH_TOKEN="your-token"
./gradlew publishPlugin
```

### Marketplace Channels

Publish to beta or early access channels:
```kotlin
intellijPlatform {
    publishing {
        tokens = providers.environmentVariable("PUBLISH_TOKEN")
        channels = listOf("beta", "eap")
    }
}
```

## Release Process

1. Update `version` in `gradle.properties`
2. Add entries to `[Unreleased]` section in `CHANGELOG.md`
3. Commit and push
4. Build workflow creates draft release
5. Review draft release on GitHub Releases page
6. Click "Publish release"
7. Release workflow triggers automatically

## Dependabot

The template includes `.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
```

Note: Dependabot does NOT support Gradle wrapper version updates. Check [Gradle Releases](https://gradle.org/releases) manually:
```bash
./gradlew wrapper --gradle-version <new-version>
```

## Qodana

Qodana is JetBrains' static code analysis tool.

```yaml
# .github/workflows/qodana.yml (optional)
name: Qodana
on:
  push:
    branches: [main]
jobs:
  qodana:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
      - uses: JetBrains/qodana-action@v2024.3
        env:
          QODANA_TOKEN: ${{ secrets.QODANA_TOKEN }}
```

Run locally: `./gradlew qodana`
