---
# Komelia-ecr6
title: 2026-03-17-epub3-reader
status: in-progress
type: epic
priority: normal
created_at: 2026-03-17T21:36:27Z
updated_at: 2026-03-17T22:47:55Z
---

EPUB3 Reader integration using Readium Kotlin Toolkit v3.1.2 with synchronized audio overlay (SMIL) support. Android-only native reader.

## Completed Status (2026-03-18)

All code changes implemented. Build verified passing:

### Last known error (before context clear)
 unresolved in  because  is  (not ) in .

### Fix applied (needs build verification)
Changed :
-  → 
-  →   
-  → 

### Next step in new chat
Run Calculating task graph as configuration cache cannot be reused because file 'epub-reader/build.gradle' has changed.
Type-safe project accessors is an incubating feature.
Project accessors enabled, but root project name not explicitly set for 'hkdf'. Checking out the project in different folders will impact the generated code and implicitly the buildscript classpath, breaking caching.

> Configure project :epub-reader
w: [33m[1m⚠️ Usage of Internal Kotlin Gradle Plugin Properties Detected[0m[0m
ATTENTION! This build uses the following Kotlin Gradle Plugin properties:

kotlin.internal.incremental.enableUnsafeOptimizationsForMultiplatform

Internal properties are not recommended for production use.
Stability and future compatibility of the build is not guaranteed.
[32m[1mSolution:[0m[0m
[32m[3mPlease consider using the public API instead of internal properties.[0m[0m


> Configure project :komelia-app
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.application'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.application' starting with Android Gradle Plugin 9.0.0.

Please change the structure of the your project and move the usage of 'com.android.application' into a separate subproject. The new subproject should add a dependency on this KMP subproject.

Read more: https://kotl.in/kmp-project-structure-migration
[32m[1mSolution:[0m[0m
[32m[3mPlease change the structure of your project and move the usage of 'com.android.application' into a separate subproject.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-ui
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m

w: [33m[1m⚠️ Unsupported API dependency types in test source sets[0m[0m
API dependency types are used in test source sets
Dependencies:
    - org.jetbrains.kotlinx:atomicfu:0.29.0 (source sets: wasmJsTest)

Adding API dependency types to test source sets is not supported and will removed in a future version of Kotlin.

API dependencies are transitively exposed to consumers, but test source sets should not be consumable.
[32m[1mSolutions:[0m[0m
[32m • [3mReplace API dependency types in test source sets with implementation dependencies.[0m[0m
[32m • [3mFor Kotlin/JVM projects, consider using Test Fixtures https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures[0m[0m
[32m • [3mFor Test Fixtures support in non-Kotlin/JVM projects, please add your use-case to https://youtrack.jetbrains.com/issue/KT-63142[0m[0m


> Configure project :komelia-domain:core
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m

w: [33m[1m⚠️ Unsupported API dependency types in test source sets[0m[0m
API dependency types are used in test source sets
Dependencies:
    - org.jetbrains.kotlinx:atomicfu:0.29.0 (source sets: wasmJsTest)

Adding API dependency types to test source sets is not supported and will removed in a future version of Kotlin.

API dependencies are transitively exposed to consumers, but test source sets should not be consumable.
[32m[1mSolutions:[0m[0m
[32m • [3mReplace API dependency types in test source sets with implementation dependencies.[0m[0m
[32m • [3mFor Kotlin/JVM projects, consider using Test Fixtures https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures[0m[0m
[32m • [3mFor Test Fixtures support in non-Kotlin/JVM projects, please add your use-case to https://youtrack.jetbrains.com/issue/KT-63142[0m[0m


> Configure project :komelia-domain:komga-api
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-domain:offline
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:jni
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:ncnn-upscaler
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m

w: [33m[1m⚠️ Deprecated 'Android Style' Source Directory[0m[0m
Usage of 'Android Style' source directory komelia-infra/ncnn-upscaler/src/main/kotlin is deprecated.
Use komelia-infra/ncnn-upscaler/src/androidMain/kotlin instead.

To suppress this warning: put the following in your gradle.properties:
kotlin.mpp.androidSourceSetLayoutV2AndroidStyleDirs.nowarn=true
[32m[1mSolution:[0m[0m
[32m[3mPlease migrate to the new source directory: komelia-infra/ncnn-upscaler/src/androidMain/kotlin[0m[0m
[36mLearn more: [0m[34mhttps://kotl.in/android-source-set-layout-v2[0m[36m[0m


> Configure project :komelia-infra:webview
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:database:shared
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:database:sqlite
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:database:transaction
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:image-decoder:shared
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:image-decoder:vips
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:onnxruntime:api
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :komelia-infra:onnxruntime:jvm
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :third_party:ChipTextField:chiptextfield-core
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :third_party:ChipTextField:chiptextfield-m3
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Configure project :third_party:compose-sonner:sonner
w: [33m[1m⚠️ The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility with Android Gradle plugin: 'com.android.library'[0m[0m
The 'org.jetbrains.kotlin.multiplatform' plugin will not be compatible with 'com.android.library' starting with Android Gradle Plugin 9.0.0.
[32m[1mSolution:[0m[0m
[32m[3mPlease use the 'com.android.kotlin.multiplatform.library' plugin instead of 'com.android.library'.[0m[0m
[36mSee [0m[34mhttps://kotl.in/gradle/agp-new-kmp[0m[36m for more details.[0m


> Task :epub-reader:preBuild UP-TO-DATE
> Task :epub-reader:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-ui:convertXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-ui:kmpPartiallyResolvedDependenciesChecker
> Task :epub-reader:preDebugBuild UP-TO-DATE
> Task :komelia-ui:convertXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-ui:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-ui:preBuild UP-TO-DATE
> Task :komelia-ui:copyNonXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-ui:generateComposeResClass UP-TO-DATE
> Task :komelia-ui:copyNonXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :komelia-ui:preDebugBuild UP-TO-DATE
> Task :komelia-ui:convertXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :komelia-domain:core:kmpPartiallyResolvedDependenciesChecker
> Task :epub-reader:generateDebugResValues UP-TO-DATE
> Task :komelia-ui:generateExpectResourceCollectorsForCommonMain UP-TO-DATE
> Task :epub-reader:javaPreCompileDebug UP-TO-DATE
> Task :komelia-domain:core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-ui:generateDebugResValues UP-TO-DATE
> Task :komelia-domain:offline:preBuild UP-TO-DATE
> Task :komelia-domain:komga-api:preBuild UP-TO-DATE
> Task :epub-reader:generateDebugResources UP-TO-DATE
> Task :komelia-domain:komga-api:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-domain:offline:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-ui:prepareComposeResourcesTaskForAndroidMain NO-SOURCE
> Task :komelia-infra:database:transaction:preBuild UP-TO-DATE
> Task :komelia-infra:database:transaction:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-domain:komga-api:preDebugBuild UP-TO-DATE
> Task :komelia-ui:generateDebugResources UP-TO-DATE
> Task :komelia-domain:offline:preDebugBuild UP-TO-DATE
> Task :komelia-domain:core:extractDebugProto UP-TO-DATE
> Task :komelia-domain:komga-api:generateDebugResValues UP-TO-DATE
> Task :komelia-ui:prepareComposeResourcesTaskForAndroidDebug NO-SOURCE
> Task :komelia-infra:database:transaction:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-domain:offline:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-domain:komga-api:javaPreCompileDebug UP-TO-DATE
> Task :komelia-domain:komga-api:generateDebugResources UP-TO-DATE
> Task :komelia-infra:database:transaction:preDebugBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:kmpPartiallyResolvedDependenciesChecker
> Task :epub-reader:packageDebugResources UP-TO-DATE
> Task :komelia-domain:offline:generateDebugResValues UP-TO-DATE
> Task :komelia-ui:generateResourceAccessorsForAndroidMain NO-SOURCE
> Task :komelia-ui:generateResourceAccessorsForAndroidDebug NO-SOURCE
> Task :komelia-infra:image-decoder:shared:preBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:database:transaction:generateDebugResValues UP-TO-DATE
> Task :komelia-domain:komga-api:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:database:transaction:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:preDebugBuild UP-TO-DATE
> Task :epub-reader:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:jni:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:jni:preBuild UP-TO-DATE
> Task :komelia-domain:offline:generateDebugResources UP-TO-DATE
> Task :komelia-domain:offline:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:jni:preDebugBuild UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:preBuild UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:database:transaction:generateDebugResources UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:javaPreCompileDebug UP-TO-DATE
> Task :komelia-domain:komga-api:packageDebugResources UP-TO-DATE
> Task :komelia-domain:offline:packageDebugResources UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:jni:javaPreCompileDebug UP-TO-DATE
> Task :epub-reader:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:jni:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:generateDebugResources UP-TO-DATE
> Task :komelia-infra:jni:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-domain:offline:parseDebugLocalResources UP-TO-DATE
> Task :komelia-domain:komga-api:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:preBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:ncnn-upscaler:preDebugBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:preDebugBuild UP-TO-DATE
> Task :komelia-infra:jni:generateDebugResources UP-TO-DATE
> Task :komelia-domain:komga-api:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:database:transaction:packageDebugResources UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:database:transaction:parseDebugLocalResources UP-TO-DATE
> Task :komelia-domain:offline:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:packageDebugResources UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:generateDebugResources UP-TO-DATE
> Task :komelia-infra:jni:packageDebugResources UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:ncnn-upscaler:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:onnxruntime:api:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:onnxruntime:api:preBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:preDebugBuild UP-TO-DATE
> Task :komelia-infra:database:transaction:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:packageDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:generateDebugResValues UP-TO-DATE
> Task :komelia-domain:komga-api:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:generateDebugResources UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:parseDebugLocalResources UP-TO-DATE
> Task :komelia-domain:komga-api:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-domain:komga-api:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:generateDebugRFile UP-TO-DATE
> Task :komelia-domain:komga-api:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-domain:komga-api:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:packageDebugResources UP-TO-DATE
> Task :komelia-infra:database:transaction:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-domain:core:extractProto UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:generateDebugRFile UP-TO-DATE
> Task :komelia-domain:core:preBuild UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:javaPreCompileDebug UP-TO-DATE
> Task :komelia-ui:copyNonXmlValueResourcesForCommonMain UP-TO-DATE
> Task :komelia-domain:komga-api:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:generateDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:webview:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:webview:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:webview:copyNonXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-infra:jni:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:webview:convertXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-infra:image-decoder:shared:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:jni:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:packageDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:preBuild UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:preDebugBuild UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:webview:convertXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-infra:webview:copyNonXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-infra:onnxruntime:jvm:javaPreCompileDebug UP-TO-DATE
> Task :komelia-domain:core:preDebugBuild UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:image-decoder:shared:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-ui:prepareComposeResourcesTaskForCommonMain UP-TO-DATE
> Task :komelia-infra:database:transaction:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-infra:ncnn-upscaler:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:generateDebugResValues UP-TO-DATE
> Task :komelia-ui:generateResourceAccessorsForCommonMain UP-TO-DATE
> Task :komelia-infra:database:transaction:processDebugJavaRes UP-TO-DATE
> Task :komelia-domain:core:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:database:transaction:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-infra:jni:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-domain:core:javaPreCompileDebug UP-TO-DATE
> Task :komelia-domain:core:generateDebugResources UP-TO-DATE
> Task :komelia-infra:database:transaction:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:shared:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-ui:copyDebugComposeResourcesToAndroidAssets UP-TO-DATE
> Task :komelia-infra:database:transaction:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:generateDebugResources UP-TO-DATE
> Task :komelia-ui:packageDebugResources UP-TO-DATE
> Task :komelia-infra:jni:processDebugJavaRes UP-TO-DATE
> Task :komelia-domain:core:packageDebugResources UP-TO-DATE
> Task :komelia-infra:webview:prepareComposeResourcesTaskForAndroidMain NO-SOURCE
> Task :komelia-infra:image-decoder:shared:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:jni:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-infra:webview:prepareComposeResourcesTaskForCommonMain NO-SOURCE
> Task :komelia-infra:webview:generateComposeResClass SKIPPED
> Task :komelia-ui:parseDebugLocalResources UP-TO-DATE
> Task :komelia-ui:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:packageDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-ui:generateActualResourceCollectorsForAndroidMain UP-TO-DATE
> Task :komelia-infra:webview:generateResourceAccessorsForAndroidMain SKIPPED
> Task :komelia-infra:jni:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-infra:webview:generateResourceAccessorsForCommonMain SKIPPED
> Task :komelia-infra:onnxruntime:jvm:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-domain:core:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:jni:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-infra:webview:generateActualResourceCollectorsForAndroidMain SKIPPED
> Task :komelia-infra:webview:copyNonXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :komelia-infra:jni:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:webview:preBuild UP-TO-DATE
> Task :komelia-infra:webview:generateExpectResourceCollectorsForCommonMain SKIPPED
> Task :komelia-infra:ncnn-upscaler:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:ncnn-upscaler:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:webview:convertXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :komelia-domain:core:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-infra:database:shared:preBuild UP-TO-DATE
> Task :komelia-infra:webview:preDebugBuild UP-TO-DATE
> Task :komelia-infra:database:shared:preDebugBuild UP-TO-DATE
> Task :komelia-infra:webview:prepareComposeResourcesTaskForAndroidDebug NO-SOURCE
> Task :komelia-infra:webview:generateDebugResValues UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:onnxruntime:api:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-infra:webview:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:webview:generateDebugResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:convertXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-infra:onnxruntime:api:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:database:shared:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:onnxruntime:api:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-infra:database:shared:generateDebugResValues UP-TO-DATE
> Task :komelia-infra:database:shared:javaPreCompileDebug UP-TO-DATE
> Task :komelia-infra:database:shared:generateDebugResources UP-TO-DATE
> Task :komelia-domain:offline:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:copyNonXmlValueResourcesForAndroidMain NO-SOURCE
> Task :komelia-infra:webview:copyDebugComposeResourcesToAndroidAssets UP-TO-DATE
> Task :komelia-domain:offline:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-domain:offline:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-domain:offline:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:database:shared:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :komelia-infra:image-decoder:vips:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:convertXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-domain:offline:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:webview:generateResourceAccessorsForAndroidDebug SKIPPED
> Task :third_party:ChipTextField:chiptextfield-core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :third_party:ChipTextField:chiptextfield-core:copyNonXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-infra:image-decoder:vips:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-infra:image-decoder:vips:processDebugJavaRes UP-TO-DATE
> Task :komelia-infra:database:shared:packageDebugResources UP-TO-DATE
> Task :komelia-infra:webview:packageDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:api:createFullJarDebug UP-TO-DATE
> Task :komelia-domain:offline:createFullJarDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :komelia-infra:image-decoder:vips:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:webview:parseDebugLocalResources UP-TO-DATE
> Task :komelia-infra:database:shared:parseDebugLocalResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:prepareComposeResourcesTaskForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:convertXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:prepareComposeResourcesTaskForCommonMain NO-SOURCE
> Task :komelia-infra:webview:generateDebugRFile UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:generateResourceAccessorsForCommonMain SKIPPED
> Task :komelia-infra:image-decoder:vips:createFullJarDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:generateExpectResourceCollectorsForCommonMain SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:onnxruntime:jvm:processDebugJavaRes UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:convertXmlValueResourcesForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:generateResourceAccessorsForAndroidMain SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:convertXmlValueResourcesForCommonMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:generateActualResourceCollectorsForAndroidMain SKIPPED
> Task :third_party:ChipTextField:chiptextfield-core:generateComposeResClass SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:generateComposeResClass SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:copyNonXmlValueResourcesForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:preBuild UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:copyNonXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:copyNonXmlValueResourcesForCommonMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:convertXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :komelia-infra:onnxruntime:jvm:compileDebugJavaWithJavac NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:preDebugBuild UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:preBuild UP-TO-DATE
> Task :komelia-infra:webview:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:javaPreCompileDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:prepareComposeResourcesTaskForCommonMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:generateDebugResValues UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:copyNonXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:generateResourceAccessorsForCommonMain SKIPPED
> Task :third_party:ChipTextField:chiptextfield-m3:preDebugBuild UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateExpectResourceCollectorsForCommonMain SKIPPED
> Task :third_party:ChipTextField:chiptextfield-core:generateDebugResources UP-TO-DATE
> Task :komelia-infra:database:shared:generateDebugRFile UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateDebugResValues UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:prepareComposeResourcesTaskForAndroidDebug NO-SOURCE
> Task :komelia-infra:webview:compileDebugJavaWithJavac NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:javaPreCompileDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateDebugResources UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:onnxruntime:jvm:bundleLibRuntimeToJarDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:prepareComposeResourcesTaskForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:prepareComposeResourcesTaskForAndroidDebug NO-SOURCE
> Task :third_party:compose-sonner:sonner:convertXmlValueResourcesForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-core:generateResourceAccessorsForAndroidDebug SKIPPED
> Task :third_party:compose-sonner:sonner:kmpPartiallyResolvedDependenciesChecker
> Task :komelia-infra:onnxruntime:jvm:createFullJarDebug UP-TO-DATE
> Task :third_party:compose-sonner:sonner:copyNonXmlValueResourcesForAndroidMain NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:generateResourceAccessorsForAndroidDebug SKIPPED
> Task :third_party:ChipTextField:chiptextfield-core:copyDebugComposeResourcesToAndroidAssets UP-TO-DATE
> Task :third_party:compose-sonner:sonner:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :third_party:compose-sonner:sonner:convertXmlValueResourcesForCommonMain NO-SOURCE
> Task :third_party:compose-sonner:sonner:generateComposeResClass SKIPPED
> Task :third_party:compose-sonner:sonner:convertXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :third_party:compose-sonner:sonner:copyNonXmlValueResourcesForCommonMain NO-SOURCE
> Task :komelia-infra:webview:bundleLibCompileToJarDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateResourceAccessorsForAndroidMain SKIPPED
> Task :third_party:compose-sonner:sonner:preBuild UP-TO-DATE
> Task :third_party:compose-sonner:sonner:generateExpectResourceCollectorsForCommonMain SKIPPED
> Task :third_party:compose-sonner:sonner:prepareComposeResourcesTaskForCommonMain NO-SOURCE
> Task :third_party:compose-sonner:sonner:copyNonXmlValueResourcesForAndroidDebug NO-SOURCE
> Task :third_party:compose-sonner:sonner:preDebugBuild UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:copyDebugComposeResourcesToAndroidAssets UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:packageDebugResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateActualResourceCollectorsForAndroidMain SKIPPED
> Task :third_party:compose-sonner:sonner:prepareComposeResourcesTaskForAndroidMain NO-SOURCE
> Task :third_party:compose-sonner:sonner:generateResourceAccessorsForAndroidMain SKIPPED
> Task :third_party:compose-sonner:sonner:prepareComposeResourcesTaskForAndroidDebug NO-SOURCE
> Task :third_party:compose-sonner:sonner:javaPreCompileDebug UP-TO-DATE
> Task :third_party:compose-sonner:sonner:generateResourceAccessorsForCommonMain SKIPPED
> Task :third_party:compose-sonner:sonner:generateResourceAccessorsForAndroidDebug SKIPPED
> Task :komelia-domain:core:extractIncludeDebugProto UP-TO-DATE
> Task :third_party:compose-sonner:sonner:generateActualResourceCollectorsForAndroidMain SKIPPED
> Task :third_party:compose-sonner:sonner:generateDebugResValues UP-TO-DATE
> Task :third_party:compose-sonner:sonner:copyDebugComposeResourcesToAndroidAssets UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:parseDebugLocalResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:packageDebugResources UP-TO-DATE
> Task :third_party:compose-sonner:sonner:generateDebugResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:parseDebugLocalResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:generateDebugRFile UP-TO-DATE
> Task :komelia-domain:core:generateDebugProto UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:generateDebugRFile UP-TO-DATE
> Task :third_party:compose-sonner:sonner:packageDebugResources UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:compose-sonner:sonner:parseDebugLocalResources UP-TO-DATE
> Task :komelia-domain:core:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:compileDebugJavaWithJavac NO-SOURCE
> Task :third_party:compose-sonner:sonner:generateDebugRFile UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-core:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-domain:core:compileDebugJavaWithJavac UP-TO-DATE
> Task :third_party:compose-sonner:sonner:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-domain:core:bundleLibCompileToJarDebug UP-TO-DATE
> Task :third_party:compose-sonner:sonner:compileDebugJavaWithJavac NO-SOURCE
> Task :third_party:compose-sonner:sonner:bundleLibCompileToJarDebug UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:compileDebugKotlinAndroid UP-TO-DATE
> Task :third_party:ChipTextField:chiptextfield-m3:compileDebugJavaWithJavac NO-SOURCE
> Task :komelia-infra:database:shared:compileDebugKotlinAndroid UP-TO-DATE
> Task :komelia-infra:database:shared:compileDebugJavaWithJavac NO-SOURCE
> Task :third_party:ChipTextField:chiptextfield-m3:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-infra:database:shared:bundleLibCompileToJarDebug UP-TO-DATE
> Task :epub-reader:compileDebugKotlin
> Task :epub-reader:compileDebugJavaWithJavac NO-SOURCE
> Task :epub-reader:bundleLibCompileToJarDebug UP-TO-DATE
> Task :komelia-ui:compileDebugKotlinAndroid
Unable to release compile session, maybe daemon is already down
java.rmi.ConnectException: Connection refused to host: 127.0.0.1; nested exception is: 
	java.net.ConnectException: Connection refused
	at java.rmi/sun.rmi.transport.tcp.TCPEndpoint.newSocket(TCPEndpoint.java:626)
	at java.rmi/sun.rmi.transport.tcp.TCPChannel.createConnection(TCPChannel.java:217)
	at java.rmi/sun.rmi.transport.tcp.TCPChannel.newConnection(TCPChannel.java:204)
	at java.rmi/sun.rmi.server.UnicastRef.invoke(UnicastRef.java:133)
	at java.rmi/java.rmi.server.RemoteObjectInvocationHandler.invokeRemoteMethod(RemoteObjectInvocationHandler.java:215)
	at java.rmi/java.rmi.server.RemoteObjectInvocationHandler.invoke(RemoteObjectInvocationHandler.java:160)
	at jdk.proxy4/jdk.proxy4.$Proxy189.releaseCompileSession(Unknown Source)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.compileWithDaemon(GradleKotlinCompilerWork.kt:245)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.compileWithDaemonOrFallbackImpl(GradleKotlinCompilerWork.kt:143)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.run(GradleKotlinCompilerWork.kt:107)
	at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction.execute(GradleCompilerRunnerWithWorkers.kt:75)
	at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:68)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:64)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:61)
	at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:61)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:58)
	at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$0(DefaultWorkerExecutor.java:176)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:194)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$700(DefaultConditionalExecutionQueue.java:127)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:169)
	at org.gradle.internal.Factories$1.create(Factories.java:33)
	at org.gradle.internal.work.DefaultWorkerLeaseService.lambda$withLocksAcquired$0(DefaultWorkerLeaseService.java:269)
	at org.gradle.internal.work.ResourceLockStatistics$1.measure(ResourceLockStatistics.java:42)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocksAcquired(DefaultWorkerLeaseService.java:267)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:259)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:164)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:133)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.connect0(Native Method)
	at java.base/sun.nio.ch.Net.connect(Net.java:591)
	at java.base/sun.nio.ch.Net.connect(Net.java:580)
	at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:593)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327)
	at java.base/java.net.Socket.connect(Socket.java:633)
	at java.base/java.net.Socket.connect(Socket.java:583)
	at java.base/java.net.Socket.<init>(Socket.java:507)
	at java.base/java.net.Socket.<init>(Socket.java:319)
	at org.jetbrains.kotlin.daemon.common.LoopbackNetworkInterface$ClientLoopbackSocketFactory.socketCreate(NetworkUtils.kt:94)
	at org.jetbrains.kotlin.daemon.common.LoopbackNetworkInterface$ClientLoopbackSocketFactory.socketCreate(NetworkUtils.kt:93)
	at org.jetbrains.kotlin.daemon.common.LoopbackNetworkInterface$AbstractClientLoopbackSocketFactory.createSocket(NetworkUtils.kt:83)
	at org.jetbrains.kotlin.daemon.common.LoopbackNetworkInterface$ClientLoopbackSocketFactory.createSocket(NetworkUtils.kt:93)
	at java.rmi/sun.rmi.transport.tcp.TCPEndpoint.newSocket(TCPEndpoint.java:620)
	... 47 more
Failed to compile with Kotlin daemon: org.jetbrains.kotlin.gradle.tasks.DaemonCrashedException: Connection to the Kotlin daemon has been unexpectedly lost. This might be caused by the daemon being killed by another process or the operating system, or by JVM crash.
	at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.wrapCompilationExceptionIfNeeded(tasksUtils.kt:54)
	at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.wrapAndRethrowCompilationException(tasksUtils.kt:65)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.compileWithDaemon(GradleKotlinCompilerWork.kt:227)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.compileWithDaemonOrFallbackImpl(GradleKotlinCompilerWork.kt:143)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.run(GradleKotlinCompilerWork.kt:107)
	at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction.execute(GradleCompilerRunnerWithWorkers.kt:75)
	at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:68)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:64)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:61)
	at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:61)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:58)
	at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$0(DefaultWorkerExecutor.java:176)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:194)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$700(DefaultConditionalExecutionQueue.java:127)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:169)
	at org.gradle.internal.Factories$1.create(Factories.java:33)
	at org.gradle.internal.work.DefaultWorkerLeaseService.lambda$withLocksAcquired$0(DefaultWorkerLeaseService.java:269)
	at org.gradle.internal.work.ResourceLockStatistics$1.measure(ResourceLockStatistics.java:42)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocksAcquired(DefaultWorkerLeaseService.java:267)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:259)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:164)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:133)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
Caused by: java.rmi.UnmarshalException: Error unmarshaling return header; nested exception is: 
	java.io.EOFException
	at java.rmi/sun.rmi.transport.StreamRemoteCall.executeCall(StreamRemoteCall.java:255)
	at java.rmi/sun.rmi.server.UnicastRef.invoke(UnicastRef.java:165)
	at java.rmi/java.rmi.server.RemoteObjectInvocationHandler.invokeRemoteMethod(RemoteObjectInvocationHandler.java:215)
	at java.rmi/java.rmi.server.RemoteObjectInvocationHandler.invoke(RemoteObjectInvocationHandler.java:160)
	at jdk.proxy4/jdk.proxy4.$Proxy189.compile(Unknown Source)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.incrementalCompilationWithDaemon(GradleKotlinCompilerWork.kt:312)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.compileWithDaemon(GradleKotlinCompilerWork.kt:219)
	... 40 more
Caused by: java.io.EOFException
	at java.base/java.io.DataInputStream.readUnsignedByte(DataInputStream.java:290)
	at java.base/java.io.DataInputStream.readByte(DataInputStream.java:268)
	at java.rmi/sun.rmi.transport.StreamRemoteCall.executeCall(StreamRemoteCall.java:241)
	... 46 more
Using fallback strategy: Compile without Kotlin daemon
Try ./gradlew --stop if this issue persists
If it does not look related to your configuration, please file an issue with logs to https://kotl.in/issue to verify fix, then address any remaining errors.
