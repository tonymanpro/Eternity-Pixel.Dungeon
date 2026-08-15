# Infinite Pixel Dungeon - Reglas de Desarrollo del Proyecto

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
- **Variable de entorno**: Configurar siempre `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"` antes de invocar `.\gradlew.bat`.
- **Comandos Principales**:
  - Compilar JAR ejecutable: `.\gradlew.bat desktop:release`
  - Empaquetar ejecutable `.exe` nativo: `.\gradlew.bat desktop:jpackageImage`
  - Ejecutar en modo desarrollo: `.\gradlew.bat desktop:debug`
