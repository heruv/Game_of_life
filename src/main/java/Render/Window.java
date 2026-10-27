package Render;

import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;

    private int[] vaoArray = new int[1];
    private int[] vboArray = new int[1];

    private int windowWidth;
    private int windowHeight;
    private int cellCount;

    public Window(int windowWidth, int windowHeight, int cellCount) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.cellCount = cellCount;
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
        int vertexCount = 0;

        for (int i = 0; i < gridState.length; i++) {
            for (int j = 0; j < gridState[0].length; j++) {
                if (gridState[i][j]) {
                    vertexCount++;
                }
            }
        }

        float[] cellsCords = new float[6 * vertexCount];

        return cellsCords;
    }

    private void normalizeCords(float[] vertices, int width, int height) {
        for (int i = 0; i < vertices.length; i += 2) {
            vertices[i] = (vertices[i] / width) * 2.0f - 1.0f;
            vertices[i + 1] = 1.0f - (vertices[i + 1] / height) * 2.0f;
        }
    }

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
        });

        glfwMakeContextCurrent(window);
        GL.createCapabilities(); 
        glfwSwapInterval(1); 

        Shader shader = new Shader("/shaders/vertex.glsl", "/shaders/fragment.glsl");

        float[] vertices = makeGrid(cellCount, windowWidth, windowHeight);
        normalizeCords(vertices, windowWidth, windowHeight);
        bufferInit(vertices);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shader.ID);

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
}