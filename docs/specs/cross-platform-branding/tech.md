# Spec Técnico — Cross-Platform Support & Branding

## Stack
- Kotlin Multiplatform Mobile (KMM) + Compose Multiplatform
- Targets: Android (API 24+) + iOS (arm64, simulatorArm64)
- Supabase-kt 3.5.0 / Ktor 3.4.2

---

## 1. Refactor de arquitectura

### Estructura de carpetas (commonMain)
Se migró de layer-based a feature-based:

```
com/yediaz/jefac/
├── App.kt                        ← entry point compartido Android/iOS
├── AppNavigations.kt             ← navegación por rol (movido a commonMain)
├── core/
│   ├── Platform.kt / Result.kt / SupabaseClient.kt / SupabaseConfig.kt
│   ├── ui/AppColors.kt
│   └── models/
│       ├── BusinessModels.kt     → Business, AppUser
│       ├── AppointmentModels.kt  → Appointment, AppointmentService, Client, Professional, Service
│       ├── CafeModels.kt         → CafeTable, Order, OrderItem, Product
│       └── FinanceModels.kt      → Transaction
└── feature/
    ├── auth/     ← AuthContract, AuthViewModel, AuthRepository, LoginScreen
    ├── home/     ← HomeContract, HomeViewModel, HomeRepository, HomeScreen
    ├── appointments/ ← 3 contratos, 3 ViewModels, Repository, 3 Screens
    ├── cafe/     ← 3 contratos, 2 ViewModels, Repository, 3 Screens
    └── finance/  ← FinanceContract, FinanceViewModel, FinanceRepository, FinanceScreen, PricingCalculator
```

### Contratos MVI
Cada pantalla tiene su propio `*Contract.kt` (State + Intent + Effect). Se eliminaron los archivos multi-pantalla (`CafeContract.kt` → `CafeContract.kt` + `OrderContract.kt` + `InventoryContract.kt`).

### Modelos — @SerialName + camelCase
Todos los campos snake_case fueron renombrados a camelCase con `@SerialName` para mantener compatibilidad con Supabase:
```kotlin
@SerialName("business_id") val businessId: String
@SerialName("scheduled_at") val scheduledAt: String
```
Afecta ~30 campos en 12 modelos.

---

## 2. Soporte iOS

### Motor de red — Ktor Darwin
```kotlin
// build.gradle.kts
listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.compilations.getByName("main").defaultSourceSet.dependencies {
        implementation(libs.ktor.client.darwin)
    }
}
```

### ViewModel factories
`viewModel()` sin factory falla en iOS/Kotlin Native (`SavedStateViewModelFactory` no soportado). Solución: factory lambda explícita donde no había factory:
```kotlin
// Antes (falla en iOS)
viewModel: AuthViewModel = viewModel()
// Después
viewModel: AuthViewModel = viewModel { AuthViewModel() }
```
Los demás ViewModels ya tenían `Factory` companion object con `create(KClass, CreationExtras)`.

### Deprecated APIs
- `monthNumber` → `month.ordinal + 1` (kotlinx-datetime)
- `dayOfMonth` → `day`
- `Icons.Filled.KeyboardArrowLeft/Right` → `Icons.AutoMirrored.Filled.*`
- `@OptIn(ExperimentalMaterial3Api::class)` en `DatePickerField`
- `-Xexpect-actual-classes` en `compilerOptions` global

### expect/actual — SupabaseConfig
```
commonMain/core/SupabaseConfig.kt   ← expect object SupabaseConfig
androidMain/core/SupabaseConfig.android.kt  ← actual (gitignored)
iosMain/core/SupabaseConfig.ios.kt          ← actual (gitignored)
```

---

## 3. Branding

### Logo en Login
```kotlin
Image(
    painter = painterResource(Res.drawable.logo),
    modifier = Modifier.size(90.dp).padding(bottom = 12.dp)
)
```
Fuente: `commonMain/composeResources/drawable/logo.png` (500×500px, fondo transparente).

### Splash — Android (SplashScreen API)
- Dependencia: `androidx.core:core-splashscreen:1.2.0`
- `installSplashScreen()` antes de `super.onCreate()` en `MainActivity`
- Tema `Theme.App.SplashScreen` en `themes.xml`:
  - `windowSplashScreenBackground`: `#000000`
  - `windowSplashScreenAnimatedIcon`: `@drawable/ic_splash` (PNG por densidad)
  - `windowSplashScreenIconBackgroundColor`: `#000000`
- PNGs generados con `sips`: canvas = 240dp×densidad, logo al 55% centrado con fondo negro.

### Splash — iOS
- `UILaunchScreen` en `Info.plist` con `UIColorName: LaunchBackground` (negro `#000000`).
- Color definido en `Assets.xcassets/LaunchBackground.colorset/Contents.json`.

### Íconos Android
- **Adaptive icon** (`mipmap-anydpi-v26`): background negro (`ic_launcher_background.xml`) + foreground logo blanco tintado (`ic_launcher_foreground.xml` con `android:tint="#FFFFFF"`).
- **Legacy PNGs** (`mipmap-*/ic_launcher.png`): generados con `sips` — pad blanco + CIColorInvert → logo blanco sobre fondo negro, logo al 35%.

### Ícono iOS
- `Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`: 1024×1024px, fondo negro, logo blanco al 55%.
- Generado con Xcode Swift toolchain + CoreImage `CIColorInvert`.

---

## 4. Corrección de padding

Todas las pantallas tenían `top = 52dp` / `top = 48dp` hardcodeado para esquivar la status bar manualmente. El `Scaffold` ya gestiona los insets, por lo que se duplicaba el espacio. Reducido a `top = 16dp` en 8 archivos.

---

## Archivos modificados clave

| Archivo | Cambio |
|---|---|
| `build.gradle.kts` | ktor-darwin, splashscreen, `-Xexpect-actual-classes` |
| `gradle/libs.versions.toml` | ktor-darwin, coreSplashscreen |
| `App.kt` | Entry point real (login → navegación por rol) |
| `AppNavigations.kt` | Movido a commonMain |
| `MainActivity.kt` | `installSplashScreen()`, llama `App()` |
| `AndroidManifest.xml` | Tema splash |
| `iosMain/MainViewController.kt` | Sin cambios (ya llamaba `App()`) |
| 8 screens | `top = 16dp` |
| `LoginScreen.kt` | Logo + `viewModel { AuthViewModel() }` |
