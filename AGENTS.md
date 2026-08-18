# Eternity Pixel Dungeon - Reglas de Desarrollo del Proyecto

## 1. Regla Obligatoria de Localización e Internacionalización (i18n)

> **REGLA CRÍTICA**: Todos los textos nuevos, modificados o refactorizados en el juego **DEBEN** mantenerse traducidos y sincronizados en todos los idiomas soportados (actualmente Inglés y Español).

- **Ubicación de los paquetes de mensajes**:
  - `core/src/main/assets/messages/`
    - `actors/actors.properties` y `actors_es.properties`
    - `items/items.properties` y `items_es.properties`
    - `journal/journal.properties` y `journal_es.properties`
    - `levels/levels.properties` y `levels_es.properties`
    - `misc/misc.properties` y `misc_es.properties`
    - `plants/plants.properties` y `plants_es.properties`
    - `scenes/scenes.properties` y `scenes_es.properties`
    - `services/services.properties` y `services_es.properties`
    - `ui/ui.properties` y `ui_es.properties`
    - `windows/windows.properties` y `windows_es.properties`
- **Registro de Idiomas**:
  - Cada idioma soportado debe estar declarado en `com.shatteredpixel.shatteredpixeldungeon.messages.Languages`.
- **Cero Claves Faltantes**:
  - Antes de completar cualquier tarea o commit que agregue contenido o cambie la interfaz, se debe verificar que la diferencia de claves entre el archivo base `.properties` y `*_es.properties` sea **0**.
  - Utilizar el skill `i18n-sync` para auditar la integridad de las traducciones.

---

## 2. Entorno de Compilación y Ejecución

- **Java SDK**: Requiere Java 17+ (Adoptium Temurin Hotspot 17).
- **Android SDK**: Requiere `local.properties` con `sdk.dir` apuntando al SDK de Android.
- **Variable de entorno**: Configurar siempre `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"` antes de invocar `.\gradlew.bat`.
- **Comandos Principales**:
  - Compilar JAR ejecutable Desktop: `.\gradlew.bat desktop:release`
  - Empaquetar ejecutable `.exe` nativo Windows: `.\gradlew.bat desktop:jpackageImage`
  - Ejecutar en modo desarrollo: `.\gradlew.bat desktop:debug`
  - Ejecutar suite de pruebas automáticas (sin imágenes): `.\gradlew.bat desktop:autoTest`
  - Exportar capturas HD de mapas a `Marketing/art/`: `.\gradlew.bat desktop:exportMaps`
  - Compilar APK Android Debug: `.\gradlew.bat android:assembleDebug`
  - Compilar APK Android Release: `.\gradlew.bat android:assembleRelease`


---

## 3. Reglas Obligatorias de Commits, Releases y Empaquetado

> **REGLA 1 (Control de Commits y Submódulos)**:
> - **Solo se realiza commit y push cuando el usuario lo solicite explícitamente.** No realizar commits anticipados o intermedios sin autorización directa.
> - **Sincronización Obligatoria de Submódulos**: Al realizar un commit y push en el repositorio principal, siempre se deben commitear y pushear primero los cambios existentes dentro de los submódulos (`core/src/main/assets/packages/eternity` hacia `Eternity-Commercial-Assets`), y luego subir el repositorio principal con `git push --recurse-submodules=on-demand origin main` para que los punteros remotos queden siempre sincronizados.
>
> **REGLA 2 (Empaquetado y Release Obligatorio)**: Siempre que se solicite subir un commit al repositorio remoto:
> 1. Compilar el JAR ejecutable (`.\gradlew.bat desktop:release`).
> 2. Empaquetar el ejecutable nativo de Windows (`.\gradlew.bat desktop:jpackageImage`).
> 3. Crear el paquete `.zip` de distribución para Windows (`desktop/build/Eternity-Pixel-Dungeon-v<VERSION>-Windows.zip`).
> 4. Crear y subir el Git Tag correspondiente a la versión (`git tag -fa v<VERSION> -m "..."` y `git push origin v<VERSION>`).
> 5. Generar y documentar las notas de Release / Package con la lista completa de todas las mejoras y cambios aplicados.
>
> **REGLA 3 (Redacción Orientada al Jugador en Releases, Blogs y Changelogs)**:
> - **Omitir detalles técnicos**: No incluir nombres de clases Java, métodos, excepciones (`NullPointerException`, etc.), rutas de archivos ni jerga interna de programación.
> - **Contenido claro y directo**: Enfocarse exclusivamente en:
>   - **Novedades**: Qué características, héroes, mascotas, objetos o mecánicas se agregaron.
>   - **Cambios**: Qué elementos se modificaron o eliminaron del juego.
>   - **Defectos y Bugs**: Qué errores ocurrían en el juego y cómo fueron corregidos desde la perspectiva del jugador (ej. *"Corregido un error que cerraba el juego al seleccionar hechizos del Clérigo"*).
>
> **REGLA 4 (Control de Despliegues en Hosting / Web)**: **No realizar despliegues automáticos a Firebase Hosting (`firebase deploy` / `firebase-tools`) sin que el usuario lo autorice o solicite explícitamente.** Todos los cambios web se desarrollarán y probarán localmente hasta recibir la orden directa de despliegue.

---

## 4. Capa Propietaria de Recursos (Assets) y Wrapper de Steamworks

### A. Desacoplamiento de Contenido Propietario / Comercial
- **Motor de Juego (GPL v3)**: El código fuente base reside bajo GPL v3.
- **Capa de Contenido Propietario (`core/src/main/assets/packages/eternity/`)**:
  - `branding/`: Logos de título, banners y emblemas comerciales.
  - `music/`: Pistas musicales originales de autor.
  - `story/`: Textos narrativos, diálogos de historia y lore exclusivo.
  - `sprites/`: Sprites de héroes, efectos y criaturas propietarias.
- **Resolución Transparente (`AssetPackResolver`)**:
  - Toda carga de texturas (`TextureCache`), música (`Music`), efectos de sonido (`Sample`) o fuentes se resuelve prioritariamente desde `packages/eternity/` con fallback automático al paquete base de assets.

### B. Integración Multiplataforma y Steamworks
- **Arquitectura Desacoplada (`PlatformServices`)**:
  - Toda interacción con plataformas comerciales (Steam, itch.io, GOG) para logros, estadísticas, guardado en la nube y presencia enriquecida debe realizarse mediante `PlatformManager.get()`.
  - El juego incluye por defecto `NullPlatformServices` (No-Op seguro) permitiendo compilaciones 100% libres y sin dependencias externas obligatorias, y `SteamworksWrapper` con enlace dinámico para compilaciones de Steam.


