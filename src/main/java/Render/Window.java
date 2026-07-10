package Render;

import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;

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

        System.out.println("OpenGL версия: " + glGetString(GL_VERSION));

        // Главный игровой цикл
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Очищаем экран
            
            // Здесь будет логика отрисовки клеток Game of Life
            glClearColor(0.15f, 0.15f, 0.15f, 1.0f); // Темно-серый фон

            glfwSwapBuffers(window); // Меняем буферы местами (показываем нарисованное)
            glfwPollEvents();        // Обрабатываем ввод (клавиатура/мышь)
        }

        glfwTerminate();
    }
}
