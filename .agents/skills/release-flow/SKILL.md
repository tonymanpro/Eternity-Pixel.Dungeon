---
name: release-flow
description: Reglas y flujo estricto para la creación de commits, empaquetado de ejecutables y publicación de releases en Eternity Pixel Dungeon.
---

# Release Flow & Packaging Skill

Este skill define las directivas y el procedimiento obligatorio para la gestión de commits, empaquetado de artefactos y publicación de versiones en **Eternity Pixel Dungeon**.

## Reglas Mandatorias

### 1. Control Estricto de Commits
- **SOLO** se realiza `git commit` y `git push` cuando el usuario lo solicite **explícitamente**.
- Nunca realizar commits parciales, temporales o prematuros durante la fase de desarrollo o investigación.

### 2. Empaquetado y Release Obligatorio al Subir
Cada vez que el usuario solicite realizar un commit y subir los cambios al repositorio remoto, se debe ejecutar el siguiente ciclo completo:

1. **Auditoría de Internacionalización (i18n)**:
   - Ejecutar `.\.agents\skills\i18n-sync\scripts\verify_i18n.ps1 -TargetLang es` para validar 0 claves faltantes.
2. **Compilación de Artefactos**:
   - JAR Release: `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"; .\gradlew.bat desktop:release`
   - Ejecutable Nativo de Windows: `.\gradlew.bat desktop:jpackageImage`
   - APK Android Release: `.\gradlew.bat android:assembleRelease`
   - APK Android Debug: `.\gradlew.bat android:assembleDebug`
3. **Generación del Paquete de Distribución**:
   - Comprimir el ejecutable de Windows: `Compress-Archive -Path "desktop/build/jpackage/Eternity Pixel Dungeon/*" -DestinationPath "desktop/build/Eternity-Pixel-Dungeon-v<VERSION>-Windows.zip" -Force`
   - Artefacto APK Android: `android/build/outputs/apk/release/android-release-unsigned.apk`
4. **Publicación y Tagging**:
   - Subir el commit a la rama principal: `git push origin main`
   - Crear y subir el Git Tag: `git tag -fa v<VERSION> -m "Eternity Pixel Dungeon v<VERSION> - <Resumen>"` y `git push origin v<VERSION>`
5. **Documentación del Package / Release (Orientada al Jugador)**:
   - **Cero detalles técnicos**: Omitir nombres de clases Java, funciones, excepciones (`NullPointerException`), rutas de archivos o jerga de código.
   - **Enfoque en contenido**: Especificar claramente:
     - **Novedades**: Qué características, mascotas, héroes o mecánicas se agregaron.
     - **Cambios**: Qué elementos se quitaron o modificaron en el equilibrio del juego.
     - **Defectos y Bugs**: Qué fallos o comportamientos extraños ocurrían en el juego y cómo fueron corregidos en un lenguaje sencillo y comprensible para cualquier jugador.
