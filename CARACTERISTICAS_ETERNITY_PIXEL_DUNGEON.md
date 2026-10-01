# ⚔️ Eternity Pixel Dungeon — Guía Definitiva de Características

**Eternity Pixel Dungeon** es una evolución profunda y ambiciosa del clásico roguelike táctico por turnos. Tomando la robustez mecánica tradicional del género, expande la experiencia con un universo propio de contenido: nuevos héroes, compañeros leales, escalado numérico infinito, mecánicas de sigilo y magia divina, integración global en la nube y una capa audiovisual original.

---

## 🌟 1. Lo que Hace a Eternity Único (Diferenciadores Clave)

A diferencia de *Vanilla Pixel Dungeon*, *Shattered Pixel Dungeon* y otras variantes de la comunidad, **Eternity Pixel Dungeon** se distingue por los siguientes pilares de diseño:

| Característica | Pixel Dungeon Tradicional | Eternity Pixel Dungeon |
| :--- | :--- | :--- |
| **Clases de Héroes** | 4 - 5 Clases | **8 Clases completas** (+ Clérigo, Bárbaro y Rey Rata jugable) |
| **Compañeros / Mascotas** | Invocaciones temporales / limitadas | **Sistema de Mascotas persistente** con niveles, stats y panel táctico |
| **Arquitectura de Escalado** | Límite entero estándar (`int` 32-bit) | **Escalado infinito extendido (`long` 64-bit)** sin desbordamiento |
| **Competitividad** | Récords locales en el dispositivo | **Salón de la Fama y Leaderboards Globales** sincronizados en la nube |
| **Cross-Platform & Licencias** | Compras aisladas por tienda | **Cross-License universal (`EPD-GOLD-...`)** entre PC, Steam y Móvil |
| **Banda Sonora y Arte** | Pistas MIDI / Chiptune estándar | **Banda Sonora Original (OST)** orquestada y capa gráfica propia |
| **Monetización** | A veces con anuncios o microtransacciones | **100% Sin Publicidad**, sin muros de energía ni Pay-to-Win |

---

## 🛡️ 2. El Elenco Expandido de Héroes (8 Clases)

Cada clase cuenta con mecánicas de inicio, árboles de talentos (Tiers 1 a 4) y dos subclases exclusivas con estilos de juego contrastantes:

1. **Guerrero (Warrior)**
   - *Subclases:* **Berserker** (aumento brutal de daño al recibir castigo) y **Gladiador** (encadenamiento de combos marciales).
   - *Rol:* Resistencia física, dominio del combate cuerpo a cuerpo y sellos de armadura transferibles.
2. **Mago (Mage)**
   - *Subclases:* **Batallador (Battlemage)** (combina ataques cuerpo a cuerpo con báculos) y **Brujo (Warlock)** (marca almas para drenar vitalidad y saciar el hambre).
   - *Rol:* Dominio arcano, recarga de varitas mágicas e impacto elemental a distancia.
3. **Pícara (Rogue)**
   - *Subclases:* **Asesina (Assassin)** (golpes letales demoledores desde las sombras) y **Corredora Libre (Freerunner)** (agilidad sobrehumana y evasión por velocidad).
   - *Rol:* Capa de sigilo, detección de trampas, emboscadas y movilidad táctica.
4. **Cazadora (Huntress)**
   - *Subclases:* **Francotiradora (Sniper)** (disparos certeros ignorando blindajes) y **Guardiana (Warden)** (conexión con la naturaleza, plantas y semillas protectoras).
   - *Rol:* Arco espiritual con munición infinita, visión a través de vegetación y ataques a larga distancia.
5. **Duelista (Duelist)**
   - *Subclases:* **Campeona (Champion)** (manejo simultáneo de dos armas) y **Monje (Monk)** (uso de energía interior, combate desarmado y meditación).
   - *Rol:* Habilidades maestras específicas por cada tipo de arma del juego.
6. **Clérigo (Cleric) — ¡Exclusivo de Eternity!**
   - *Subclases:* **Paladín (Paladin)** (defensor acorazado empoderado por bendiciones divinas) y **Sacerdote (Priest)** (canalizador de luz celestial, curación y purificación de no-muertos).
   - *Mecánica única:* Sistema de Fe y Devoción divina mediante el **Tomo Sagrado (*Holy Tome*)**, plegarias protectoras y castigo a criaturas de la oscuridad.
7. **Bárbaro (Barbarian) — ¡Exclusivo de Eternity!**
   - *Subclases:* **Belisario (Warmonger)** (máquina de guerra imparable con sed de sangre) y **Maestro de Bestias (Beastmaster)** (sinergia feral y comando supremo de bestias).
   - *Mecánica única:* Gestión de Furia que incrementa velocidad de ataque y reduce el daño entrante mientras entra en frenesí.
8. **Rey Rata (Rat King) — ¡Héroe Especial Jugable!**
   - *Subclase:* **Rey (King)**.
   - *Mecánicas únicas:* Habilidades de Ira Heredada (*LegacyWrath*), transformación de enemigos (*Ratmogrify*) y dominio sobre las alimañas de la mazmorra.

---

## 🐾 3. Sistema Profundo de Mascotas y Compañeros

Las mascotas en Eternity no son adornos pasivos; son aliados tácticos con experiencia propia, progresión de atributos y habilidades únicas:

- **Huevos de Mascota (*Pet Egg*)**: Encontrados en salas secretas y nidos de la mazmorra, listos para ser incubados y criados durante tu descenso.
- **Panel Táctico (*PetTacticalPanel*)**: Permite dar órdenes directas al compañero en tiempo real (modo agresivo, cobertura defensiva, retirada táctica o uso de habilidad especial).
- **Silbato de Mascota (*PetWhistle*)**: Herramienta de llamada rápida para coordinar emboscadas.
- **Especies de Compañeros:**
  - 🐉 **Dragón (Dragon Pet)**: Escupe ráfagas de fuego y calcina grupos de enemigos con daño en área.
  - 🐺 **Lobo (Wolf Pet)**: Velocidad extrema, flanqueo táctico y desgarro con alta probabilidad de crítico.
  - 🧚 **Hada (Fairy Pet)**: Proveedora constante de bendiciones curativas, escudos de maná y visión lejana.
  - 🕷️ **Araña (Spider Pet)**: Dispara redes inmovilizadoras, administra veneno progresivo y controla el flujo de los pasillos.
  - 🦁 **Mantícora (Manticore Pet) — Exclusiva Supporter/Gold**: Colosal criatura alada con aguijón de toxina paralizante y zarpazos de daño penetrante.

---

## ♾️ 4. Motor Extendido e Infinito (Arquitectura `long` de 64-bit)

Para los veteranos que buscan partidas extremas, bucles (*endless loops*) o retos sin techo:
- **Superación del límite de 32 bits**: Todas las variables clave de daño, estadísticas, escalado de oro y puntos de experiencia fueron migradas a tipos `long`.
- **Modo Desafío Infinito**: Posibilidad de encadenar victorias, subir el equipo a niveles inimaginables (+50, +100...) y enfrentarse a hordas de enemigos con escalado colosal sin que el juego sufra de *overflow* aritmético o congelamientos.

---

## 🏺 5. Nuevos Objetos, Alquimia Avanzada y Bóveda Secreta

- **Armas y Misiles Propios**:
  - *Shuriken de las Sombras (ShurikenOfShadows)*: Proyectil que amplifica las emboscadas en sigilo.
  - *Bola de Arcilla (Clayball)*: Objeto arrojadizo de retención e impacto.
  - *Bomba Cegadora (Flashbang)*: Creada mediante alquimia para aturdir, cegar y desorientar salas enteras de monstruos peligrosos.
- **Infusión de Maldiciones Ampliada (*CurseInfusion*)**:
  - Sistema de riesgo y recompensa donde encantar piezas con maldiciones oscuras desbloquea niveles de mejora descomunales y efectos especiales si el héroe aprende a convivir con la corrupción.
- **La Bóveda del Diablillo Ambicioso (*Imp Quest Vault*)**:
  - Nueva zona de mazmorra generada proceduralmente donde el héroe entra despojado de sus pertenencias, debiendo superar el laberinto únicamente con astucia y un cristal de teletransporte de emergencia.

---

## 🌐 6. Ecosistema en la Nube, Salón de la Fama y Cross-Platform

- **Tablas de Clasificación Globales (Hall of Fame)**:
  - Sistema integrado con servicios seguros en la nube para registrar tus mejores hazañas, profundidad máxima alcanzada, clase utilizada y puntuación total contra jugadores de todo el mundo.
- **Cross-License Unificado (`EPD-GOLD-...`)**:
  - Si adquieres o desbloqueas la edición Gold en una plataforma (Steam, Itch.io o Android Google Play), tu clave te permite desbloquear tus beneficios cosméticos y héroes en cualquier otro dispositivo sin recompras.
- **Multidispositivo Real**:
  - **PC Windows**: Ejecutable nativo `.exe` ultra liviano y optimizado.
  - **Multiplataforma**: Archivo `.jar` universal compatible con Linux, macOS y Steam Deck.
  - **Android**: Disponible en Google Play Store y distribución directa optimizada para controles táctiles.

---

## 🎨 7. Identidad Audiovisual y Ambientación

- **Banda Sonora Original (OST)**: Composiciones exclusivas de alta fidelidad que dotan de atmósfera tétrica, misteriosa y épica a cada una de las 5 regiones principales (Alcantarillas, Prisión, Cuevas, Metrópolis Enana y Salones Demoníacos).
- **Capa Gráfica Propietaria**: Nuevos sprites para jefes, efectos mágicos de iluminación, marcos dorados radiantes para seguidores y una interfaz pulida y moderna.
- **Paridad Lingüística Absoluta**: Totalmente traducido y validado en 22 idiomas (incluyendo Español e Inglés al 100%).

---

## 💡 ¿Por Qué es tan Entretenido?

Eternity Pixel Dungeon combina la tensión clásica del **permadeath** (muerte permanente) donde cada error cuenta, con una **profundidad táctica inigualable**:
- No hay dos partidas iguales: mapas, pociones y pergaminos se barajan proceduralmente en cada intento.
- La combinación de 8 héroes con mascotas tácticas crea cientos de sinergias posibles (ej. Clérigo Paladín tanqueando mientras su Dragón calcina a distancia, o Pícara Asesina coordinando emboscadas con su Lobo sombra).
- La satisfacción de dominar el posicionamiento, la gestión de inventario y los recursos mágicos, ahora ampliada para quienes buscan la gloria eterna en las profundidades de la mazmorra.
