package Render;

import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;

    private int[] vaoArray = new int[1];
    private int[] vboArray = new int[1];
    private int[] eboArray = new int[1];
     
    public void run() {

        if (!glfwInit()) {
            throw new IllegalStateException("failed to init GLFW");
        }

        // set context
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(800, 600, "game of life", NULL, NULL);
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
        buffurInit();

        // Главный игровой цикл
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Очищаем экран
            
            // Здесь будет логика отрисовки клеток Game of Life
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f); // Темно-серый фон
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shader.ID);
            glBindVertexArray(vaoArray[0]);

            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window); // Меняем буферы местами (показываем нарисованное)
            glfwPollEvents();        // Обрабатываем ввод (клавиатура/мышь)
        }

        glfwTerminate();
    }

    private void buffurInit() {

        float[] vertices = new float[] {
            0.5f,  0.5f, 0.0f,  // верхняя правая
            0.5f, -0.5f, 0.0f,  // нижняя правая
            -0.5f, -0.5f, 0.0f,  // нижняя левая
            -0.5f,  0.5f, 0.0f   // верхняя левая 
        };

        int[] indices = new int[] {
            0, 1, 3,
            1, 2, 3
        };

        glGenBuffers(eboArray);
        
        

        glGenVertexArrays(vaoArray);
        glGenBuffers(vboArray);

        glBindVertexArray(vaoArray[0]);

        glBindBuffer(GL_ARRAY_BUFFER, vboArray[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboArray[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(vaoArray[0]);
    }
}
