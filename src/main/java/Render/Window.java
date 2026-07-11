package Render;

import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;

    private int[] vaoArray = new int[1];
    private int[] vboArray = new int[1];

    private int windowWidth;
    private int windowHeight;

    public Window(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }
     
    public float[] makeGrid(int gridCount, int width, int height) {
        float gridSize = width / (float)gridCount;

        float[] gridCords = new float[4 * 2 * gridCount]; // {x1, y1, x2, y2} top and down ends of straigth line

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

    private void normalizeCords(float[] vertices, int width, int height) {
        for (int i = 0; i < vertices.length; i += 2) {
            // X: [0, width] → [-1, 1]
            vertices[i] = (vertices[i] / width) * 2.0f - 1.0f;
            
            // Y: [0, height] → [1, -1] (инвертировано!)
            vertices[i + 1] = 1.0f - (vertices[i + 1] / height) * 2.0f;
        }
    }

    public void run() {

        if (!glfwInit()) {
            throw new IllegalStateException("failed to init GLFW");
        }

        // set context
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(windowWidth, windowHeight, "game of life", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("unable to create a window");
        }

        // Обработка нажатия клавиш (для будущего управления игрой)
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // Закрыть окно по Escape
            }
        });

        // Инициализация OpenGL контекста
        glfwMakeContextCurrent(window);
        GL.createCapabilities(); 
        glfwSwapInterval(1); 

        Shader shader = new Shader("/shaders/vertex.glsl", "/shaders/fragment.glsl");

        // temp solution
        float[] vertices = makeGrid(20, 800, 800);
        normalizeCords(vertices, windowWidth, windowHeight);
        bufferInit(vertices);
        //end temp

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {GL33.glViewport(0, 0, width, height);});

        // Главный игровой цикл
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Очищаем экран
            
            // Здесь будет логика отрисовки клеток Game of Life
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f); // Темно-серый фон
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shader.ID);
            glBindVertexArray(vaoArray[0]);
            glDrawArrays(GL_LINES, 0, vertices.length);

            glfwSwapBuffers(window); // Меняем буферы местами (показываем нарисованное)
            glfwPollEvents();        // Обрабатываем ввод (клавиатура/мышь)
        }

        glfwTerminate();
    }

    @SuppressWarnings("unused")
    private void bufferInit() {

        float[] vertices = new float[1];
            

        glGenVertexArrays(vaoArray);
        glGenBuffers(vboArray);

        glBindVertexArray(vaoArray[0]);

        glBindBuffer(GL_ARRAY_BUFFER, vboArray[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(vaoArray[0]);
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
        glBindVertexArray(vaoArray[0]);
    }
}
