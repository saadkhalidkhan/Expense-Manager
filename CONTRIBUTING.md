# Contributing to Expenzilla (Expense Manager)

Thank you for helping improve this project.

## How to contribute

1. **Search existing issues** — someone may already be working on the same idea.
2. **Open an issue** — describe the bug or feature before large changes.
3. **Fork** the repository and create a branch from `master`.
4. **Make your changes** — keep PRs focused and reasonably small.
5. **Run checks** locally:
   ```bash
   ./gradlew spotlessCheck
   ./gradlew assembleDebug
   ./gradlew test
   ```
6. **Open a pull request** against `master` with:
   - A clear title and description
   - Steps to test
   - Screenshots or screen recordings for UI changes

## Code guidelines

- Follow existing Kotlin style; run `./gradlew spotlessApply` before committing.
- Use meaningful commit messages.
- Do not commit secrets (`google-services.json` with private keys, release keystores, AdMob production IDs unless intentional).
- Prefer small, reviewable commits over one large dump.

## Reporting bugs

Use the [bug report template](.github/ISSUE_TEMPLATE/bug_report.md) and include:

- Android version and device
- Steps to reproduce
- Expected vs actual behavior
- Logs or screenshots if possible

## Feature requests

Use the [feature request template](.github/ISSUE_TEMPLATE/feature_request.md) and explain the use case and proposed behavior.

## Code of conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). Be respectful and constructive.

## Questions

Open a [GitHub Discussion](https://github.com/saadkhalidkhan/Expense-Manager/discussions) or an issue labeled `question` if discussions are enabled.
