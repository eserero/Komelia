# Theming

## Theme enum

Themes are defined as an enum where each value embeds its own `ColorScheme`. Do NOT create a separate theme function per theme.

```kotlin
enum class Theme(
    val colorScheme: ColorScheme,
    val type: ThemeType,
) {
    DARK(darkColorScheme(primary = Color.White, ...), ThemeType.DARK),
    LIGHT(lightColorScheme(...), ThemeType.LIGHT),
    DARKER(darkColorScheme(...), ThemeType.DARK);

    enum class ThemeType { LIGHT, DARK }
}
```

## Applying the theme

```kotlin
MaterialTheme(colorScheme = theme.colorScheme) {
    // content
}
```

## Converting between Theme and AppTheme (settings model)

Use the companion object helpers — do not call `valueOf` directly at call sites:

```kotlin
val theme = appTheme.toTheme()       // AppTheme → Theme
val appTheme = theme.toAppTheme()    // Theme → AppTheme
```

- `Theme` lives in the UI layer; `AppTheme` is the domain/settings model
- Adding a new theme requires adding an entry to both enums with matching names
