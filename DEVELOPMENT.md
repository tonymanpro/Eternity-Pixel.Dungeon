# 🛠️ Guía de Desarrollo, Compilación, Debug y Auto-Test Runner

Bienvenido a la guía técnica oficial para el desarrollo, compilación, depuración en vivo y pruebas automáticas de **Eternity Pixel Dungeon**.

---

## 📋 Índice
1. [Requisitos Previos del Entorno](#1-requisitos-previos-del-entorno)
2. [Comandos de Compilación y Ejecución](#2-comandos-de-compilación-y-ejecución)
3. [Herramienta de Auto-Test Runner](#3-herramienta-de-auto-test-runner)
4. [Consola y Comandos de Debug en el Juego (Scroll of Debug)](#4-consola-y-comandos-de-debug-en-el-juego-scroll-of-debug)

---

## ☕ 1. Requisitos Previos del Entorno

- **Java Development Kit (JDK)**: Requiere **Java 17+** (Adoptium Temurin Hotspot 17).
- **Variable de Entorno (PowerShell en Windows)**:
  Antes de ejecutar cualquier comando de Gradle, asegúrate de tener configurado tu `JAVA_HOME`:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
  ```

---

## ⚡ 2. Comandos de Compilación y Ejecución

| Tarea / Comando | Propósito | Salida Generada |
| :--- | :--- | :--- |
| `.\gradlew.bat desktop:debug` | Ejecuta el juego en modo desarrollo con la **Consola de Debug activa**. | Ejecución en vivo |
| `.\gradlew.bat desktop:release` | Compila el archivo universal ejecutable `.jar`. | `desktop/build/libs/desktop-v<VERSION>.jar` |
| `.\gradlew.bat desktop:jpackageImage` | Empaqueta el ejecutable nativo de Windows (`.exe`) con JRE 17 embebido. | `desktop/build/Eternity-Pixel-Dungeon/` |
| `.\gradlew.bat desktop:autoTest` | Ejecuta la suite de pruebas automatizadas y el bot IA en modo Headless. | Reporte en consola y archivo de log |

---

## 🤖 3. Herramienta de Auto-Test Runner

El **Auto-Test Runner** permite validar la integridad completa del juego en modo **Headless** (100% en memoria RAM, ultra-rápido y sin abrir ventanas).

### Comando de Ejecución:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
.\gradlew.bat desktop:autoTest
```

### ¿Qué módulos prueba y para qué sirve cada uno?

1. 🏰 **Generación y Conectividad de Mazmorras (`DungeonGenTest`)**:
   - Genera los niveles 1 al 26 y el piso de la Bóveda / Minería.
   - Comprueba que la entrada y la salida estén conectadas por caminos transitables y que las salas de los 5 jefes se generen correctamente.
2. ⚔️ **Mecánicas de Héroes y Clases (`HeroMechanicsTest`)**:
   - Prueba las 7 clases (Guerrero, Mago, Pícaro, Cazadora, Duelista, Clérigo, Rey Rata).
   - Valida la progresión de nivel 1 al 30, escalado de vida (`HP`), fuerza (`STR`), árboles de talentos y hechizos del Clérigo (*Castigo Divino*, *Radiancia*, *Arma Sagrada*).
3. 🐾 **Sistema de Mascotas y Compañeros (`PetSystemTest`)**:
   - Prueba la incubación de huevos (`PetEgg`), eclosión de las 4 especies (*Dragón*, *Lobo*, *Hada*, *Araña*), alimentación con carnes/pociones y asignación de órdenes tácticas.
4. 💎 **Economía y Rarezas de Ítems (`ItemRarityTest`)**:
   - Genera 1,000 objetos aleatorios verificando atributos de rareza (Común $\rightarrow$ Mítico) y ausencia de punteros nulos.
5. 🕹️ **Bot Autónomo con IA (`AutonomousBotSim`)**:
   - Un bot con inteligencia artificial juega 50 turnos seguidos caminando por el nivel, combatiendo enemigos y recogiendo botín sin provocar bloqueos.

### Ubicación del Reporte Generado:
Cada ejecución guarda automáticamente un informe detallado en:
📁 [`desktop/build/reports/autoTest-report.txt`](file:///d:/Desarollo/InfinityPixelDungeon/Infinite-Pixel-Dungeon/desktop/build/reports/autoTest-report.txt)

---

## 📜 4. Consola y Comandos de Debug en el Juego (Scroll of Debug)

Al iniciar el juego en modo desarrollo con:
```powershell
.\gradlew.bat desktop:debug
```
Aparecerá automáticamente en el inventario del héroe el **Pergamino de Depuración (Scroll of Debug)** con icono de pergamino púrpura. Al leerlo, se abrirá la consola de comandos de debug.

---

### 🎮 Lista de Comandos de Debug Disponibles

#### 1. Obtener Objetos (`give`)
Genera cualquier objeto del juego y lo coloca directamente en tu inventario o mochila.
- **Sintaxis**: `give <objeto> [+nivel] [xcantidad] [-f]`
- **Ejemplos**:
  - `give PetEgg` $\rightarrow$ Te entrega un Huevo de Mascota.
  - `give WarHammer +15` $\rightarrow$ Te da un Martillo de Guerra mejorado a +15.
  - `give PotionOfHealing x20` $\rightarrow$ Te da 20 Pociones de Curación.
  - `give PlateArmor +10` $\rightarrow$ Te da una Armadura de Placas +10.
  - `give ScrollOfUpgrade x50` $\rightarrow$ Te entrega 50 Pergaminos de Mejora.

---

#### 2. Invocar Criaturas y Mascotas (`spawn`)
Genera monstruos, jefes, NPCs o compañeros en la posición del jugador o en una casilla seleccionada.
- **Sintaxis**: `spawn <criatura> [xcantidad | -p]`
- **Ejemplos**:
  - `spawn DragonPet` $\rightarrow$ Invoca una Cría de Dragón aliada.
  - `spawn WolfPet` $\rightarrow$ Invoca un Lobo Fiel.
  - `spawn Goo` $\rightarrow$ Invoca al jefe Goo en el piso actual.
  - `spawn Tengu` $\rightarrow$ Invoca a Tengu.
  - `spawn Rat x10` $\rightarrow$ Invoca 10 ratas en la sala.
  - `spawn Mimic -p` $\rightarrow$ Te permite colocar manualmente un Mímico haciendo clic en una casilla.

---

#### 3. Teletransporte a Niveles (`goto`)
Viaja instantáneamente a cualquier profundidad de la mazmorra sin necesidad de caminar.
- **Sintaxis**: `goto <profundidad>`
- **Ejemplos**:
  - `goto 1` $\rightarrow$ Va al Piso 1 (Cloacas).
  - `goto 6` $\rightarrow$ Va al Piso 6 (Prisión).
  - `goto 11` $\rightarrow$ Va al Piso 11 (Cuevas).
  - `goto 16` $\rightarrow$ Va al Piso 16 (Ciudad Enana).
  - `goto 21` $\rightarrow$ Va al Piso 21 (Salas Demoníacas).
  - `goto 26` $\rightarrow$ Va al Piso 26 (Cámara del Amuleto de Yendor).

---

#### 4. Teletransporte a Casillas (`warp`)
Teletransporta al héroe a una casilla específica en el mapa actual.
- **Sintaxis**: `warp [casilla]`
- **Ejemplo**: `warp` $\rightarrow$ Abre el cursor para seleccionar con un clic la casilla de destino.

---

#### 5. Aplicar Efectos y Estados (`affect`)
Aplica o retira estados, mejoras (buffs) o desventajas (debuffs) al héroe o a un objetivo visible.
- **Sintaxis**: `affect <buff> [duracion]`
- **Ejemplos**:
  - `affect Invulnerability 100` $\rightarrow$ Otorga 100 turnos de invulnerabilidad total.
  - `affect Light 500` $\rightarrow$ Ilumina toda la mazmorra durante 500 turnos.
  - `affect MindVision 200` $\rightarrow$ Revela la posición de todos los enemigos en el nivel.
  - `affect Burning` $\rightarrow$ Prende fuego al objetivo seleccionado.

---

#### 6. Generar Elementos de Terreno y Fluidos (`seed`)
Crea gases, fuego, agua o nubes mágicas en una casilla.
- **Sintaxis**: `seed <blob> [cantidad]`
- **Ejemplos**:
  - `seed Fire 10` $\rightarrow$ Prende fuego en la casilla.
  - `seed ToxicGas 50` $\rightarrow$ Llena la habitación de gas tóxico.
  - `seed Regrowth 20` $\rightarrow$ Hace florecer vegetación densa en el suelo.

---

#### 7. Colocar Trampas (`set`)
Coloca una trampa específica en una casilla elegida.
- **Sintaxis**: `set <trampa>`
- **Ejemplos**:
  - `set FireTrap` $\rightarrow$ Coloca una trampa de fuego.
  - `set SummoningTrap` $\rightarrow$ Coloca una trampa de invocación de monstruos.

---

#### 8. Inspección de Clases y Métodos (`inspect` / `use`)
Permite a desarrolladores inspeccionar y ejecutar métodos Java en vivo mediante reflexión.
- `inspect <clase>` $\rightarrow$ Muestra todos los métodos públicos y campos accesibles de un objeto.
- `use <objeto> <metodo> [argumentos]` $\rightarrow$ Ejecuta directamente un método Java sobre una entidad.

---

#### 9. Variables y Macros (`@` / `macro`)
- `@miVariable inv` $\rightarrow$ Guarda un objeto del inventario en una variable para usarlo en otros comandos.
- `macro <nombre>` $\rightarrow$ Guarda una secuencia de comandos de debug para ejecutarla de golpe con un solo alias.
