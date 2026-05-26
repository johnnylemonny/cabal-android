# Cabal Final Brand Package — v2A Optical Stronger

Final brand package for Cabal, centered around the **Secure Minimal** design direction.

## Brand Identity Decision

Selected variant specifications:

- **Wordmark**: `v2A clean bold`, centered layout.
- **Icon**: `v2A balanced fill`.
- **Horizontal Lockup**: `optical_center_stronger` — featuring enhanced optical alignment for improved visual balance.
- **Primary Background**: `#0B0D12`.

## Horizontal Lockup Specifications

The horizontal lockup utilizes the following precise parameters:

- **Artboard**: `2400 × 720`.
- **Composition Offset**: `-36 px`.
- **Icon Dimension**: `375`.
- **Icon-to-Wordmark Spacing**: `88`.

### Recommended Assets

```text
svg/cabal_lockup_horizontal_final_optical_stronger_white.svg
svg/cabal_lockup_horizontal_final_optical_stronger_black.svg
svg/cabal_lockup_horizontal_final_optical_stronger_dark.svg
```

## Package Structure

```text
svg/      Scalable Vector Graphics (Source of Truth)
png/      High-resolution PNG exports
android/  Android resources: VectorDrawables, Adaptive Icons, Splash assets, and Color XMLs
```

## Android Implementation

### Launcher & Adaptive Icons

```text
android/ic_cabal_mark_v2a_foreground.xml
android/ic_launcher.xml
android/colors_cabal.xml
```

### Splash Screen

```text
android/ic_cabal_splash_icon.xml
```

## Color Palette

- **Private Dark**: `#0B0D12`
- **Private Black**: `#05070A`
- **Surface Dark**: `#10131A`
- **Text Light**: `#F7F8FA`
- **Cipher Blue**: `#6EA8FF`
- **Peer Teal**: `#00D1B2`
- **Muted**: `#9AA4B2`

*Note: SVG files are the primary sources. PNG files should be treated as auxiliary exports.*
