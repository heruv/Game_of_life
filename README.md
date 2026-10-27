# Game of Life

A [Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) implementation in Java, rendered in real time with OpenGL via [LWJGL 3](https://www.lwjgl.org/).

## Overview

The project is split into two main parts:

- **`GameLogic`** — the cellular automaton simulation (`GameOfLife`) operating on a `gridSize × gridSize` boolean grid. It implements the standard Game of Life rules:
  - A living cell survives with 2 or 3 living neighbours.
  - A dead cell becomes alive with exactly 3 living neighbours.
  - Neighbour counting treats out-of-bounds cells as dead (no wrap-around).
  - Each generation is computed into a fresh grid and then swapped in.
- **`Render`** — the windowing and drawing layer:
  - `Window` — creates a GLFW window with an OpenGL 3.3 core context, builds the grid line geometry, uploads it to the GPU (VAO/VBO), and runs the render loop. On startup it prefers the Wayland backend when available, otherwise falls back to the platform default.
  - `Shader` — loads, compiles and links GLSL vertex/fragment shaders from `src/main/resources/shaders/`, and exposes a `setColour` uniform setter for the grid line colour.

## Project structure

```
src/
├── main/
│   ├── java/
│   │   ├── Main.java                 # entry point: builds a 10×10 world and an 800×800 window
│   │   ├── GameLogic/GameOfLife.java # simulation: get/setCellState, proceed(), print()
│   │   └── Render/
│   │       ├── Window.java           # GLFW window, grid geometry, render loop
│   │       └── Shader.java           # GLSL shader loading/compilation
│   └── resources/shaders/            # vertex.glsl, fragment.glsl
└── test/java/GameLogic/GameOfLifeTest.java
```

## Requirements

- JDK 26+ (the build targets Java release 26)
- [Maven](https://maven.apache.org/)
- OpenGL 3.3-capable GPU/driver
- Linux or Windows (native LWJGL libraries are selected automatically by Maven OS profiles)

## Building and running

```bash
# compile
mvn compile

# run the application (adds --enable-native-access=ALL-UNNAMED automatically)
mvn exec:java

# run the tests
mvn test
```

## Controls

- **Esc** — quit the application

## Roadmap

- Interactive cell toggling via 2D input (mouse)
- Wiring the simulation step loop into the render loop and drawing live cells
