---
name: i18n-sync
description: >-
  Audits, verifies, and maintains synchronization of language translations (.properties bundles)
  across English, Spanish, and any other supported locales in Infinite Pixel Dungeon.
  Use whenever new features, items, actors, UI strings, or dialogs are added or modified.
---

# i18n-sync: Sincronización y Validación de Localización

Este skill define el flujo de trabajo para mantener todas las traducciones al día en el proyecto.

## Reglas Obligatorias
1. Cada vez que se cree una nueva clave en los archivos `core/src/main/assets/messages/*/*.properties` (Inglés por defecto), se debe agregar su traducción en los archivos correspondientes para los demás idiomas soportados (ej. `*_es.properties`).
2. Se debe ejecutar el script de verificación antes de dar por completada cualquier tarea que modifique textos.

## Procedimiento de Verificación

1. Ejecutar el script de auditoría de localización:
   ```powershell
   .\.agents\skills\i18n-sync\scripts\verify_i18n.ps1 -TargetLang es
   ```
2. Si el script reporta claves faltantes, agregar las traducciones en el archivo correspondiente de `core/src/main/assets/messages/<categoría>/<categoría>_es.properties`.
3. Volver a ejecutar el script hasta obtener:
   `RESULTADO: ¡Todas las traducciones están al 100% sincronizadas!`
