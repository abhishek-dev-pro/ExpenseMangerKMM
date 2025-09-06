# SF Pro Font Implementation Guide

This guide explains how to implement Apple's SF Pro font across all platforms in your KMM project.

## ⚠️ Important Legal Notice

**SF Pro is Apple's proprietary font.** Before using SF Pro fonts in your application:

1. **Check Apple's licensing terms** - SF Pro is primarily licensed for use within Apple's ecosystem
2. **Ensure compliance** - Using SF Pro on non-Apple platforms may require special licensing
3. **Consider alternatives** - Inter font is a free, open-source alternative that closely resembles SF Pro

## Current Implementation

The project is currently set up with a platform-specific font system that:

- **iOS**: Uses SF Pro (system font)
- **Android**: Uses system default (Roboto)
- **Desktop**: Uses system default
- **Web**: Uses system default

## How to Add Actual SF Pro Fonts

### Step 1: Obtain SF Pro Font Files

1. **From Apple Developer Resources**:
   - Download SF Pro fonts from Apple's developer resources
   - Ensure you have proper licensing for cross-platform use

2. **Font Files Needed**:
   ```
   SF-Pro-Display-Regular.otf
   SF-Pro-Display-Medium.otf
   SF-Pro-Display-Semibold.otf
   SF-Pro-Display-Bold.otf
   SF-Pro-Text-Regular.otf
   SF-Pro-Text-Medium.otf
   SF-Pro-Text-Semibold.otf
   SF-Pro-Text-Bold.otf
   ```

### Step 2: Add Font Files to Project

#### For Android:
1. Create directory: `composeApp/src/androidMain/res/font/`
2. Add SF Pro font files to this directory
3. Update `FontFamily.android.kt`:

```kotlin
@Composable
actual fun getPlatformFontFamily(): FontFamily {
    return FontFamily(
        Font(resId = R.font.sf_pro_display_regular, weight = FontWeight.Normal),
        Font(resId = R.font.sf_pro_display_medium, weight = FontWeight.Medium),
        Font(resId = R.font.sf_pro_display_semibold, weight = FontWeight.SemiBold),
        Font(resId = R.font.sf_pro_display_bold, weight = FontWeight.Bold),
        Font(resId = R.font.sf_pro_text_regular, weight = FontWeight.Normal),
        Font(resId = R.font.sf_pro_text_medium, weight = FontWeight.Medium),
        Font(resId = R.font.sf_pro_text_semibold, weight = FontWeight.SemiBold),
        Font(resId = R.font.sf_pro_text_bold, weight = FontWeight.Bold)
    )
}
```

#### For Desktop (JVM):
1. Create directory: `composeApp/src/jvmMain/resources/fonts/`
2. Add SF Pro font files to this directory
3. Update `FontFamily.jvm.kt`:

```kotlin
@Composable
actual fun getPlatformFontFamily(): FontFamily {
    return FontFamily(
        Font(file = File("fonts/SF-Pro-Display-Regular.otf"), weight = FontWeight.Normal),
        Font(file = File("fonts/SF-Pro-Display-Medium.otf"), weight = FontWeight.Medium),
        Font(file = File("fonts/SF-Pro-Display-Semibold.otf"), weight = FontWeight.SemiBold),
        Font(file = File("fonts/SF-Pro-Display-Bold.otf"), weight = FontWeight.Bold),
        Font(file = File("fonts/SF-Pro-Text-Regular.otf"), weight = FontWeight.Normal),
        Font(file = File("fonts/SF-Pro-Text-Medium.otf"), weight = FontWeight.Medium),
        Font(file = File("fonts/SF-Pro-Text-Semibold.otf"), weight = FontWeight.SemiBold),
        Font(file = File("fonts/SF-Pro-Text-Bold.otf"), weight = FontWeight.Bold)
    )
}
```

#### For Web (Wasm):
1. Create directory: `composeApp/src/wasmJsMain/resources/fonts/`
2. Add SF Pro font files to this directory
3. Update `FontFamily.wasmJs.kt`:

```kotlin
@Composable
actual fun getPlatformFontFamily(): FontFamily {
    return FontFamily(
        Font(
            identity = "SF Pro Display Regular",
            getData = { loadFontData("fonts/SF-Pro-Display-Regular.otf") },
            weight = FontWeight.Normal
        ),
        Font(
            identity = "SF Pro Display Medium",
            getData = { loadFontData("fonts/SF-Pro-Display-Medium.otf") },
            weight = FontWeight.Medium
        ),
        // ... add other weights
    )
}

private fun loadFontData(path: String): ByteArray {
    // Implementation to load font data from resources
    return byteArrayOf() // Replace with actual implementation
}
```

#### For iOS:
The current implementation already uses SF Pro as it's the system font. No changes needed.

### Step 3: Update Build Configuration

#### Android:
Add to `composeApp/build.gradle.kts`:
```kotlin
android {
    // ... existing configuration
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/composeResources")
}
```

#### iOS:
Add to `iosApp/iosApp/Info.plist`:
```xml
<key>UIAppFonts</key>
<array>
    <string>SF-Pro-Display-Regular.otf</string>
    <string>SF-Pro-Display-Medium.otf</string>
    <string>SF-Pro-Display-Semibold.otf</string>
    <string>SF-Pro-Display-Bold.otf</string>
    <string>SF-Pro-Text-Regular.otf</string>
    <string>SF-Pro-Text-Medium.otf</string>
    <string>SF-Pro-Text-Semibold.otf</string>
    <string>SF-Pro-Text-Bold.otf</string>
</array>
```

## Alternative: Using Inter Font (Recommended)

If you want a similar look without licensing concerns, consider using Inter font:

1. **Download Inter font** from [rsms.me/inter](https://rsms.me/inter/)
2. **Follow the same steps** as above but with Inter font files
3. **Update font names** in the implementations

## Testing

After implementing:

1. **Build all platforms**:
   ```bash
   ./gradlew :composeApp:compileDebugKotlinAndroid
   ./gradlew :composeApp:compileKotlinIosArm64
   ./gradlew :composeApp:compileKotlinJvm
   ./gradlew :composeApp:compileKotlinWasmJs
   ```

2. **Run on each platform** to verify font rendering

3. **Check font fallbacks** if SF Pro files are missing

## Current Status

✅ **Font system architecture** - Complete
✅ **Platform-specific implementations** - Complete  
✅ **Theme integration** - Complete
⏳ **Actual SF Pro font files** - Needs to be added by you
⏳ **Font loading implementations** - Needs to be completed with actual files

The foundation is ready - you just need to add the actual SF Pro font files and update the implementations accordingly.
