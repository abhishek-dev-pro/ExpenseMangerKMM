# SF Pro Font Files - Where to Add Them

## 📁 **Complete Directory Structure**

I've created all the necessary directories for you. Here's exactly where to add SF Pro font files:

### **1. Android Platform** 📱
```
composeApp/src/androidMain/res/font/
├── sf_pro_display_regular.ttf
├── sf_pro_display_medium.ttf
├── sf_pro_display_semibold.ttf
├── sf_pro_display_bold.ttf
├── sf_pro_text_regular.ttf
├── sf_pro_text_medium.ttf
├── sf_pro_text_semibold.ttf
└── sf_pro_text_bold.ttf
```

### **2. Desktop (JVM) Platform** 🖥️
```
composeApp/src/jvmMain/resources/fonts/
├── SF-Pro-Display-Regular.otf
├── SF-Pro-Display-Medium.otf
├── SF-Pro-Display-Semibold.otf
├── SF-Pro-Display-Bold.otf
├── SF-Pro-Text-Regular.otf
├── SF-Pro-Text-Medium.otf
├── SF-Pro-Text-Semibold.otf
└── SF-Pro-Text-Bold.otf
```

### **3. Web (Wasm) Platform** 🌐
```
composeApp/src/wasmJsMain/resources/fonts/
├── SF-Pro-Display-Regular.otf
├── SF-Pro-Display-Medium.otf
├── SF-Pro-Display-Semibold.otf
├── SF-Pro-Display-Bold.otf
├── SF-Pro-Text-Regular.otf
├── SF-Pro-Text-Medium.otf
├── SF-Pro-Text-Semibold.otf
└── SF-Pro-Text-Bold.otf
```

### **4. iOS Platform** 📱
```
iosApp/iosApp/fonts/
├── SF-Pro-Display-Regular.otf
├── SF-Pro-Display-Medium.otf
├── SF-Pro-Display-Semibold.otf
├── SF-Pro-Display-Bold.otf
├── SF-Pro-Text-Regular.otf
├── SF-Pro-Text-Medium.otf
├── SF-Pro-Text-Semibold.otf
└── SF-Pro-Text-Bold.otf
```

## 📋 **Steps to Add Font Files**

### **Step 1: Obtain SF Pro Font Files**
1. **From Apple Developer Resources**: Download SF Pro fonts from Apple's developer resources
2. **Ensure Licensing**: Make sure you have proper licensing for cross-platform use
3. **Font Formats**: Use `.ttf` for Android, `.otf` for other platforms

### **Step 2: Add Files to Directories**
1. **Copy font files** to the appropriate directories listed above
2. **Use correct naming** as shown in the directory structure
3. **Ensure file extensions** match the platform requirements

### **Step 3: Update Code Implementations**
After adding the font files, you'll need to update the platform-specific implementations:

#### **Android** (`FontFamily.android.kt`):
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

#### **Desktop** (`FontFamily.jvm.kt`):
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

#### **Web** (`FontFamily.wasmJs.kt`):
```kotlin
@Composable
actual fun getPlatformFontFamily(): FontFamily {
    return FontFamily(
        Font(
            identity = "SF Pro Display Regular",
            getData = { loadFontData("fonts/SF-Pro-Display-Regular.otf") },
            weight = FontWeight.Normal
        ),
        // ... add other weights
    )
}

private fun loadFontData(path: String): ByteArray {
    // Implementation to load font data from resources
    return byteArrayOf() // Replace with actual implementation
}
```

#### **iOS** (`FontFamily.ios.kt`):
```kotlin
@Composable
actual fun getPlatformFontFamily(): FontFamily {
    return FontFamily(
        Font(
            identity = "SF Pro Display Regular",
            getData = { loadFontData("fonts/SF-Pro-Display-Regular.otf") },
            weight = FontWeight.Normal
        ),
        // ... add other weights
    )
}
```

### **Step 4: Update Build Configuration**

#### **Android** (`composeApp/build.gradle.kts`):
```kotlin
android {
    // ... existing configuration
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/composeResources")
}
```

#### **iOS** (`iosApp/iosApp/Info.plist`):
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

## ✅ **Current Status**

- ✅ **Directories Created**: All font directories are ready
- ✅ **Architecture Ready**: Font loading system is in place
- ⏳ **Font Files**: Need to be added by you
- ⏳ **Code Updates**: Need to be implemented after adding font files

## 🚀 **Next Steps**

1. **Add SF Pro font files** to the directories listed above
2. **Update the platform implementations** with the code examples above
3. **Test the build** on each platform
4. **Verify font rendering** in your app

The foundation is complete - you just need to add the actual font files and update the implementations!
