# Spec Funcional — Cross-Platform Support & Branding

## Objetivo
Habilitar el soporte multiplataforma real de la app JEFac (Android + iOS) e incorporar la identidad visual de Vibra Bonito en los puntos de entrada de la aplicación.

---

## Módulos afectados
- Todas las pantallas (refactor de arquitectura)
- Login
- Splash screen (Android y iOS)
- Íconos de aplicación (Android y iOS)

---

## Funcionalidades

### 1. Identidad visual — Logo de Vibra Bonito

**Login:**
- El logo de Vibra Bonito se muestra centrado encima del texto "Vibra Bonito" en la pantalla de login.
- El logo se renderiza en negro sobre el fondo crema de la pantalla.

**Splash screen — Android:**
- Al abrir la app se muestra una pantalla de splash con fondo crema (#FAF7F2) y el logo de Vibra Bonito en negro centrado.
- El logo ocupa aproximadamente el 55% del área segura del splash (dentro del safe zone de 160dp).
- El splash desaparece automáticamente al terminar de cargar la app.

**Splash screen — iOS:**
- Al abrir la app se muestra una pantalla de splash con fondo negro.
- Sin logo en esta versión (pendiente implementación con LaunchScreen.storyboard).

**Ícono de aplicación — Android:**
- El ícono en el cajón de aplicaciones muestra el logo de Vibra Bonito en blanco sobre fondo negro, centrado al 35% del área del ícono.
- Se generan íconos para todas las densidades (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).
- El adaptive icon usa el mismo esquema negro + logo blanco.

**Ícono de aplicación — iOS:**
- El ícono de la app muestra el logo de Vibra Bonito en blanco sobre fondo negro.
- Ícon de 1024×1024px, el logo ocupa el 55% del área.

---

### 2. Soporte multiplataforma — iOS

- La aplicación compila y corre en iOS sin crashes.
- El flujo de autenticación (login → navegación por rol) funciona correctamente en iOS.
- Los ViewModels se crean con factories explícitas, compatibles con Kotlin/Native.
- Las llamadas de red a Supabase funcionan en iOS usando el motor Ktor Darwin.

---

### 3. Correcciones visuales

- El contenido de todas las pantallas (Home, Servicios, Cafetería, Finanzas, etc.) aparece correctamente posicionado sin espacio excesivo en la parte superior.
- El espaciado superior respeta el área de la status bar sin duplicar el padding.

---

## Criterios de aceptación

| # | Criterio |
|---|---|
| 1 | La app compila y corre en Android sin errores |
| 2 | La app compila y corre en iOS sin crashes |
| 3 | El splash de Android muestra fondo negro + logo blanco centrado |
| 4 | El splash de iOS muestra fondo negro |
| 5 | El ícono en el cajón de Android muestra logo blanco sobre fondo negro |
| 6 | El ícono de iOS muestra logo blanco sobre fondo negro |
| 7 | El logo de Vibra Bonito aparece en la pantalla de login |
| 8 | Las pantallas no tienen espacio excesivo en la parte superior |
| 9 | El flujo login → home funciona en ambas plataformas |
