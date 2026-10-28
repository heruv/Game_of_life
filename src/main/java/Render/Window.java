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

    private int windowWidth;
    private int windowHeight;
    private int cellCount;

    private boolean[][] gridState;
    private boolean cellsDirty = false;

    private int cellVertexCount;

    private EventBus bus;
    /**
     * Creates a window that renders the given simulation.
     *
     */
    public Window(int windowWidth, int windowHeight, int cellCount, EventBus bus) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.cellCount = cellCount ;
        this.bus = bus;
    }
     
    private float[] makeGrid(int gridCount, int width, int height) {
        float gridSize = width / (float)gridCount;
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

        // 12 floats per cell: 6 vertices * 2 coordinates
        //optimize for only 4 vertices later
        float[] cellsCords = new float[12 * liveCellCount];

        float cellWidth = windowWidth / (float) cols;
        float cellHeight = windowHeight / (float) rows;
        int index = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!gridState[i][j]) continue;

                float left = j * cellWidth;      // column goes with x
                float right = left + cellWidth;
                float top = i * cellHeight;      // row goes with y
                float bottom = top + cellHeight;

                // triangle 1
                cellsCords[index++] = left;   cellsCords[index++] = top;
                cellsCords[index++] = left;   cellsCords[index++] = bottom;
                cellsCords[index++] = right;  cellsCords[index++] = top;

                // triangle 2
                cellsCords[index++] = right;  cellsCords[index++] = top;
                cellsCords[index++] = left;   cellsCords[index++] = bottom;
                cellsCords[index++] = right;  cellsCords[index++] = bottom;
            }
        }

        normalizeCords(cellsCords, windowWidth, windowHeight);
        return cellsCords;
    }

    private void normalizeCords(float[] vertices, int width, int height) {
        for (int i = 0; i < vertices.length; i += 2) {
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
            if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                bus.publish(GameEvent.TOGGLE_PAUSE);
            }
        });

        glfwMakeContextCurrent(window);
        GL.createCapabilities(); 
        glfwSwapInterval(1); 

        Shader shader = new Shader("/shaders/vertex.glsl", "/shaders/fragment.glsl");

        float[] vertices = makeGrid(cellCount, windowWidth, windowHeight);
        normalizeCords(vertices, windowWidth, windowHeight);
        bufferInit(vertices);
        cellBufferInit();

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
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
            if (cellVertexCount > 0) {
                shader.setColour(0.0f, 0.8f, 0.3f);
                glBindVertexArray(cellVao[0]);
                glDrawArrays(GL_TRIANGLES, 0, cellVertexCount);
            }

            shader.setColour(1.0f, 1.0f, 1.0f);

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

        glBindVertexArray(cellVao[0]);

        glBindBuffer(GL_ARRAY_BUFFER, cellVbo[0]);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0); 
    }

    private void updateCells() {
        float[] cellsCords = drawCell(gridState);

        glBindVertexArray(cellVao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, cellVbo[0]);
        glBufferData(GL_ARRAY_BUFFER, cellsCords, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // alive cells * 6 vertices (two triangles)
        cellVertexCount = cellsCords.length / 2;
    }

    @Override 
    public void onEvent(GameEvent event, Object data) {
        if (event == GameEvent.STATE_CHANGED) {
            gridState = (boolean[][]) data;
            cellsDirty = true;
        }
     }
}