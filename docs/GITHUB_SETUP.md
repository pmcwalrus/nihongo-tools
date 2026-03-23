# GitHub Repository Setup

## Goal

The recommended public repository setup for this project is:

- a clear product-oriented `README`
- screenshots in the repository
- automatic builds on GitHub Actions
- downloadable installers in `Releases`

This document explains how to create that setup.

## Recommended Repository Name

Good options:

- `nihongo-tools`
- `nihongo-tools-desktop`

Prefer a short and clear name. `nihongo-tools` is the best default.

## Recommended Visibility

- `Public` if you want anyone to download builds and discover the project
- `Private` only if you are not ready to publish yet

For downloadable releases and portfolio value, `Public` is strongly preferred.

## Recommended Repository Description

Use a short description in GitHub `About`, for example:

`Desktop utilities for learning Japanese: kanji counter and Marugoto audio downloader.`

## Recommended Topics

Add these topics in the GitHub sidebar:

- `kotlin`
- `compose-desktop`
- `desktop-app`
- `japanese`
- `kanji`
- `language-learning`

## Recommended Homepage

If you do not have a website, set the repository homepage to:

- your latest releases page

Example:

`https://github.com/<your-user>/<your-repo>/releases/latest`

## Files to Keep in the Repository

Important files:

- `README.md`
- `architecture.md`
- `.github/workflows/release.yml`
- `docs/RELEASE_TEMPLATE.md`

Recommended extra files:

- `LICENSE`
- screenshots in `docs/images/`

## Suggested Screenshot Set

Create a folder:

- `docs/images/`

Recommended images:

1. `main-window.png`
2. `kanji-counter.png`
3. `marugoto-audio.png`

Keep screenshots:

- clear
- cropped to the app window
- in PNG format

## Recommended Release Flow

1. Push your code to GitHub.
2. Make sure GitHub Actions is enabled.
3. Create and push a version tag.

Example:

```bash
git tag v1.0.0
git push origin v1.0.0
```

4. Wait for the workflow to finish.
5. Open `Releases` and verify that `DMG`, `MSI`, and `EXE` are attached.
6. Edit the release text using the template from `docs/RELEASE_TEMPLATE.md`.

## How to Create a Good GitHub Repository

### 1. Create the repository

On GitHub:

1. Click `New repository`.
2. Name it `nihongo-tools`.
3. Set visibility to `Public`.
4. Do not initialize it with another README if this project already has one.

### 2. Push the local project

Example commands:

```bash
git init
git add .
git commit -m "Initial release"
git branch -M main
git remote add origin git@github.com:<your-user>/nihongo-tools.git
git push -u origin main
```

If you prefer HTTPS:

```bash
git remote add origin https://github.com/<your-user>/nihongo-tools.git
git push -u origin main
```

### 3. Configure the repository page

In GitHub:

1. Add a repository description.
2. Add topics.
3. Set homepage to the releases page.
4. Pin the repository on your profile if relevant.

### 4. Add screenshots

Put screenshots in:

- `docs/images/`

Then reference them from `README.md`.

### 5. Publish releases

Create semantic version tags:

- `v1.0.0`
- `v1.0.1`
- `v1.1.0`

Tagging is what triggers the release workflow in this project.

## Suggested Versioning

Use semantic versioning:

- `v1.0.0` for first public release
- `v1.0.1` for fixes
- `v1.1.0` for new features
- `v2.0.0` for breaking changes

## Recommended License

If you want others to use or contribute to the project, add a license.

Good default:

- `MIT`

If you want, a license file can be added later.

## Release Page Tips

For each GitHub Release:

- add a short changelog
- clearly list which file is for which OS
- mention if macOS Gatekeeper may show an "unknown developer" warning

Example:

- `Nihongo Tools-1.0.0.dmg` — macOS
- `Nihongo Tools-1.0.0.msi` — Windows installer
- `Nihongo Tools-1.0.0.exe` — Windows executable installer

## macOS Note

Unsigned macOS apps may show a system warning on first launch.

That is expected for hobby or internal distribution unless you later add:

- Apple Developer signing
- notarization

## Minimum Practical Checklist

Before sharing the repository publicly, make sure you have:

- a good `README`
- at least one screenshot
- a successful GitHub Actions run
- a tagged release
- downloadable assets in `Releases`

