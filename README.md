# Eternity Pixel Dungeon 👑

[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android%20%7C%20Linux%20%7C%20macOS-blue.svg)](https://eternity-pixel-dungeon.web.app)
[![Version](https://img.shields.io/badge/Version-v1.0.0-green.svg)](https://github.com/tonymanpro/Eternity-Pixel.Dungeon/releases/tag/v1.0.0)
[![Website](https://img.shields.io/badge/Website-eternity--pixel--dungeon.web.app-brightgreen.svg)](https://eternity-pixel-dungeon.web.app)
[![Patreon](https://img.shields.io/badge/Patreon-Supporter%20Editions-red.svg)](https://www.patreon.com/c/EternityPixelDungeon/membership)

**Eternity Pixel Dungeon** es un roguelike tradicional de exploración infinita, combate táctico por turnos y alta rejugabilidad basado en *Shattered Pixel Dungeon* y *Experienced Pixel Dungeon*, con soporte completo de localización al español e inglés, sistema de compañeros y mascotas, motor visual en alta resolución (HD 2x) y una suite completa de pruebas automatizadas.

---

## 🌟 Características Principales de la Versión v1.0.0

- 🐾 **Sistema Completo de Mascotas y Compañeros**:
  - Incubación y eclosión de huevos raros en la mazmorra.
  - **4 Especies**: Cría de Dragón (fuego), Lobo Fiel (rastreo y sangrado), Hada de Luz (curación e iluminación) y Araña Tejedora (trampas y veneno).
  - Evolución (*Cría* $\rightarrow$ *Joven* $\rightarrow$ *Adulto*), alimentación y órdenes tácticas (*Seguir*, *Defender*, *Quedarse*).
- 🎨 **Motor Gráfico en Alta Resolución (HD 2x)**: Texturas en alta fidelidad y efectos de partículas brillantes en golpes críticos.
- 🧙‍♂️ **7 Clases Jugables**: Guerrero, Mago, Pícaro, Cazadora, Duelista, Clérigo (con magias sagradas y subclases) y Rey Rata.
- 🌐 **Localización e Internacionalización (i18n)**: 100% traducido y sincronizado en Español e Inglés.
- 🤖 **Auto-Test Runner Headless**: Suite automatizada para validar mazmorras, héroes, mascotas, economía y simulación de bot con IA en menos de 1 segundo.

---

## 🚀 Inicio Rápido (Comandos Principales)

> **Nota**: Requiere Java 17+ (Adoptium Temurin Hotspot 17). Configura `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"` en PowerShell antes de compilar.

```powershell
# 1. Ejecutar en modo desarrollo con la Consola de Debug activa
.\gradlew.bat desktop:debug

# 2. Ejecutar la suite de pruebas automáticas (Auto-Test Runner)
.\gradlew.bat desktop:autoTest

# 3. Compilar el archivo JAR ejecutable
.\gradlew.bat desktop:release

# 4. Empaquetar el ejecutable nativo para Windows (.exe)
.\gradlew.bat desktop:jpackageImage
```

Para consultar la **lista completa de comandos de debug en el juego**, macros y guías de desarrollo, revisa el documento detallado:
👉 **[DEVELOPMENT.md](DEVELOPMENT.md)**

---

## 🌐 Enlaces Oficiales del Proyecto

- 🏠 **Sitio Web Oficial**: [https://eternity-pixel-dungeon.web.app](https://eternity-pixel-dungeon.web.app)
- 📰 **Blog Oficial de Lanzamientos**: [https://eternity-pixel-dungeon.web.app/blog.html](https://eternity-pixel-dungeon.web.app/blog.html)
- 👑 **Membresía Patreon**: [https://www.patreon.com/c/EternityPixelDungeon/membership](https://www.patreon.com/c/EternityPixelDungeon/membership)
- 📦 **Releases en GitHub**: [https://github.com/tonymanpro/Eternity-Pixel.Dungeon/releases](https://github.com/tonymanpro/Eternity-Pixel.Dungeon/releases)