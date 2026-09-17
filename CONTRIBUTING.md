# Contributing to Cabal Android

Thank you for your interest in contributing to **Cabal Android**! This project is a serverless, peer-to-peer, encrypted communication client built natively for Android using the **Cable Protocol (draft-8)**.

---

## 📜 Code of Conduct & Values

- **Privacy by Default**: We strictly reject third-party tracking, analytics, cloud phone-home endpoints, and proprietary closed dependencies.
- **Mutual Respect**: Maintain a welcoming, inclusive, and constructive environment. Harassment, spam, or hostile behavior will not be tolerated.
- **Strict Copyleft (AGPL-3.0)**: All contributions must remain free software.

---

## ⚖️ Licensing & Contributor Agreement

Cabal Android is licensed under the **GNU Affero General Public License v3 (AGPL-3.0-only)**.

By submitting a Pull Request or contributing code, documentation, or assets, you explicitly agree that:
1. Your contribution is licensed under the terms of the **GNU AGPLv3**.
2. Any downstream use, network relay, or fork must provide full source code under the same terms.
3. You have the legal right and necessary permissions to submit the contribution.

---

## 🛠️ Development Setup

### Prerequisites
- **Android Studio Ladybug (2024.2+)** or newer
- **JDK 25** (e.g. Eclipse Temurin 25)
- Android SDK Platform 37
- **Node.js v20+** (if working on the `bridge/` service)

### Workflow
1. **Fork and Clone**:
   ```bash
   git clone https://github.com/<your-username>/cabal-android.git
   cd cabal-android
   ```
2. **Create a Topic Branch**:
   ```bash
   git checkout -b feature/my-enhancement
   # or
   git checkout -b fix/issue-description
   ```
3. **Run Verification Before Committing**:
   ```bash
   # Run all Android and protocol unit tests
   ./gradlew test

   # Run Android lint
   ./gradlew lintDebug

   # Run bridge tests (if modifying bridge/)
   cd bridge && npm test
   ```

---

## 📐 Coding Standards

- **Kotlin & Compose**:
  - Follow official Android architecture and Material 3 design guidelines.
  - Keep Compose screens responsive, accessible, and compliant with edge-to-edge system insets.
  - Maintain offline-first behavior: no dependencies on Google Play Services or network fonts.
- **Cable Protocol (`cable-protocol`)**:
  - Strictly adhere to the binary Cable draft-8 specification.
  - Use Varint framing and deterministic byte serialization.
- **Documentation & Commits**:
  - Write concise, descriptive commit messages in English using the Conventional Commits format (e.g., `feat: ...`, `fix: ...`, `docs: ...`, `ci: ...`).

---

## 🚀 Submitting a Pull Request

1. Push your branch to your fork.
2. Open a Pull Request targeting the `master` branch.
3. Fill out the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md) completely.
4. Our automated CI suite will run:
   - **CodeQL Analysis v4**: Security vulnerability scanning.
   - **Super-Linter v7**: Code style, Kotlin ktlint, JavaScript, and format checks.
   - **AI Community Moderator**: Automated triage, label assignment, and AGPL-3.0 compliance confirmation.
