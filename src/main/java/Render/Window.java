package Render;

import org.lwjgl.opengl.*;

import Events.EventBus;
import Events.GameEvent;
import Events.GameEventListener;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Renders the game grid and its live cells in a GLFW window using OpenGL 3.3.
 * <p>
 * Keeps two vertex buffers: one for the static grid lines and one for the live
 * cells, which is re-uploaded only when {@code dirtyFlag} signals a grid change.
 */
public class Window implements GameEventListener{
    private long window;

    private int[] vaoArray = new int[1];
    private int[] vboArray = new int[1];

    private int[] cellVao = new int[1];
    private int[] cellVbo = new int[1];
    private int[] cellEbo = new int[1];

    private int windowWidth;
    private int windowHeight;

    private float gridSize;

    private boolean[][] gridState;
    private int[] cellAge;
    private boolean cellsDirty = false;
    private int cellCount;
    private int cellIndexCount;

    private EventBus bus;

    /**
     * Creates a window that renders the given simulation.
     *
     */
    public Window(int windowWidth, int windowHeight, int cellCount, EventBus bus) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.cellCount = cellCount ;
        this.cellAge = new int[cellCount * cellCount];
        this.bus = bus;
    }
     
    private float[] makeGrid(int gridCount, int width, int height) {
        gridSize = width / (float)gridCount;
        float[] gridCords = new float[4 * 2 * gridCount];

        int index = 0;        
        float tempWidth = width;
        for (int i = 0; i < gridCount; i++) {
            gridCords[index++] = tempWidth - gridSize;
            gridCords[index++] = height;
            gridCords[index++] = tempWidth - gridSize;
            gridCords[index++] = 0.0f;
            tempWidth -= gridSize;
        }
        
        float tempHeight = height;
        for (int i = 0; i < gridCount; i++) {
            gridCords[index++] = 0.0f;
            gridCords[index++] = tempHeight - gridSize;
            gridCords[index++] = width;
            gridCords[index++] = tempHeight - gridSize;
            tempHeight -= gridSize;
        }

        return gridCords;
    }

    private float[] drawCell(boolean[][] gridState) {
        if (gridState == null) return new float[0];

        int rows = gridState.length;
        int cols = gridState[0].length;

        int liveCellCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gridState[i][j]) {
                    liveCellCount++;
                }
            }
        }

        // 20 floats per cell: 4 vertices * (2 pos + 3 color)
        float[] cellsCords = new float[20 * liveCellCount];

        float cellWidth = windowWidth / (float) cols;
        float cellHeight = windowHeight / (float) rows;
        int index = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!gridState[i][j]) continue;

                int age = cellAge[i * cols + j];
                // Age gradient: 0.0 (newborn) -> 1.0 (20+ generations)
                // Green -> Yellow -> Red -> Purple
                float t = Math.min((age - 1) / 20.0f, 1.0f);
                float r, g, b;
                if (t < 0.333f) {
                    // Phase 1: Green -> Yellow (ramp red up)
                    float u = t / 0.333f;
                    r = u;
                    g = 1.0f;
                    b = 0.0f;
                } else if (t < 0.666f) {
                    // Phase 2: Yellow -> Red (ramp green down)
                    float u = (t - 0.333f) / 0.333f;
                    r = 1.0f;
                    g = 1.0f - u;
                    b = 0.0f;
                } else {
                    // Phase 3: Red -> Purple (ramp blue up, deepen red)
                    float u = (t - 0.666f) / 0.334f;
                    r = 1.0f - 0.3f * u; // 1.0 -> 0.7
                    g = 0.0f;
                    b = 0.9f * u;        // 0.0 -> 0.9
                }

                float left = j * cellWidth;      // column goes with x
                float right = left + cellWidth;
                float top = i * cellHeight;      // row goes with y
                float bottom = top + cellHeight;

                // vertex 0: top-left
                cellsCords[index++] = left;   cellsCords[index++] = top;
                cellsCords[index++] = r;      cellsCords[index++] = g;      cellsCords[index++] = b;

                // vertex 1: bottom-left
                cellsCords[index++] = left;   cellsCords[index++] = bottom;
                cellsCords[index++] = r;      cellsCords[index++] = g;      cellsCords[index++] = b;

                // vertex 2: top-right
                cellsCords[index++] = right;  cellsCords[index++] = top;
                cellsCords[index++] = r;      cellsCords[index++] = g;      cellsCords[index++] = b;

                // vertex 3: bottom-right
                cellsCords[index++] = right;  cellsCords[index++] = bottom;
                cellsCords[index++] = r;      cellsCords[index++] = g;      cellsCords[index++] = b;
            }
        }

        normalizeCords(cellsCords, windowWidth, windowHeight, 5);
        return cellsCords;
    }

    private void normalizeCords(float[] vertices, int width, int height, int stride) {
        for (int i = 0; i < vertices.length; i += stride) {
            vertices[i] = (vertices[i] / width) * 2.0f - 1.0f;
            vertices[i + 1] = 1.0f - (vertices[i + 1] / height) * 2.0f;
        }
    }

    /**
     * Opens the window and runs the render loop, blocking until it is closed.
     * <p>
     * The simulation steps every 150 ms; Space toggles pause, Esc closes the window.
     */
    public void run() {
        // Prefer Wayland backend natively if supported by GLFW
        if (glfwPlatformSupported(GLFW_PLATFORM_WAYLAND)) {
            glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_WAYLAND);
        }

        if (!glfwInit()) {
            throw new IllegalStateException("failed to init GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(windowWidth, windowHeight, "game of life", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("unable to create a window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                bus.publish(GameEvent.TOGGLE_PAUSE);
            }
            if (key == GLFW_KEY_TAB && action == GLFW_RELEASE) {
                bus.publish(GameEvent.ABORT);
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            double[] x = new double[1];
            double[] y = new double[1];
            glfwGetCursorPos(window, x, y);

            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                defineNewCellCords(x[0], y[0]);
            }
        });

        // glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
        //     // System.out.printf("Xpos: %f, Ypos: %f\n", xpos, ypos);
        // });

        glfwMakeContextCurrent(window);
        GL.createCapabilities(); 
        glfwSwapInterval(1); 

        Shader shader = new Shader("/shaders/vertex.glsl", "/shaders/fragment.glsl");

        float[] vertices = makeGrid(cellCount, windowWidth, windowHeight);
        normalizeCords(vertices, windowWidth, windowHeight, 2);
        bufferInit(vertices);
        cellBufferInit();

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
            windowWidth = width;
            windowHeight = height;
        });

        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shader.ID);


            bus.publish(GameEvent.TICK, glfwGetTime());
            // re-upload cell data only when the grid changed
            // (startup, after proceed(), later after mouse edits)
            if (cellsDirty) {
                updateCells();
                cellsDirty = false;
            }

            // live cells; skip the draw call when there are none
            if (cellIndexCount > 0) {
                shader.setUniformInt("useVertexColor", 1);
                glBindVertexArray(cellVao[0]);
                glDrawElements(GL_TRIANGLES, cellIndexCount, GL_UNSIGNED_INT, 0L);
            }

            shader.setUniformInt("useVertexColor", 0);
            shader.setColour(1.0f, 1.0f, 1.0f, 0.7f);

            glBindVertexArray(vaoArray[0]);
            glDrawArrays(GL_LINES, 0, vertices.length / 2);

            

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwTerminate();
    }

    private void bufferInit(float[] vertices) {
        glGenVertexArrays(vaoArray);
        glGenBuffers(vboArray);

        glBindVertexArray(vaoArray[0]);

        glBindBuffer(GL_ARRAY_BUFFER, vboArray[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0); // <-- FIX: Unbind with 0 instead of re-binding the VAO
    }

    private void cellBufferInit() {
        glGenVertexArrays(cellVao);
        glGenBuffers(cellVbo);
        glGenBuffers(cellEbo);

        glBindVertexArray(cellVao[0]);

        glBindBuffer(GL_ARRAY_BUFFER, cellVbo[0]);
        // attribute 0: position (2 floats)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        // attribute 1: color (3 floats)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Pre-fill static indices for all possible cells: 2 triangles (6 indices) per quad
        int maxCells = cellCount * cellCount;
        int[] indices = new int[maxCells * 6];
        for (int i = 0; i < maxCells; i++) {
            int v = i * 4;
            int idx = i * 6;
            indices[idx]     = v;
            indices[idx + 1] = v + 1;
            indices[idx + 2] = v + 2;
            indices[idx + 3] = v + 2;
            indices[idx + 4] = v + 1;
            indices[idx + 5] = v + 3;
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cellEbo[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void updateCells() {
        float[] cellsCords = drawCell(gridState);

        glBindVertexArray(cellVao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, cellVbo[0]);
        glBufferData(GL_ARRAY_BUFFER, cellsCords, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // 4 vertices (20 floats) per cell -> 6 indices per cell
        cellIndexCount = (cellsCords.length / 20) * 6;
    }

    @Override 
    public void onEvent(GameEvent event, Object data) {
        if (event == GameEvent.STATE_CHANGED) {
            gridState = (boolean[][]) data;
            int rows = gridState.length;
            int cols = gridState[0].length;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int idx = i * cols + j;
                    if (gridState[i][j]) {
                        cellAge[idx]++;
                    } else {
                        cellAge[idx] = 0;
                    }
                }
            }
            cellsDirty = true;
        } else if (event == GameEvent.CELL_EDITED) {
            int[] edit = (int[]) data;
            int xNew = edit[0];
            int yNew = edit[1];
            boolean alive = (edit[2] == 1);
            if (gridState == null) {
                gridState = new boolean[cellCount][cellCount];
            }
            gridState[xNew][yNew] = alive;
            cellAge[xNew * cellCount + yNew] = alive ? 1 : 0;
            cellsDirty = true;
        }
    }

    private void defineNewCellCords(double xPos, double yPos) {
        if (xPos < 0 || xPos >= windowWidth || yPos < 0 || yPos >= windowHeight) {
            return;
        }

        int[] cords = new int[2];

        int col = (int) (xPos / windowWidth * cellCount);
        int row = (int) (yPos / windowHeight * cellCount);

        cords[0] = Math.min(row, cellCount - 1);
        cords[1] = Math.min(col, cellCount - 1);

        bus.publish(GameEvent.ADD_CELL, cords);
    }
}