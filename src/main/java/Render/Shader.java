package Render;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL33.*;

public class Shader {
    public int ID;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexCode;
        String fragmentCode;

        try {
            vertexCode = loadResource(vertexPath);
            fragmentCode = loadResource(fragmentPath);
        } catch (Exception e) {
            throw new RuntimeException("failed to load file", e);
        }

        int vertex, fragment;

        vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, vertexCode);
        glCompileShader(vertex);

        fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fragmentCode);
        glCompileShader(fragment);

        ID = glCreateProgram();
        glAttachShader(ID, vertex);
        glAttachShader(ID, fragment);
        glLinkProgram(ID);

        glDeleteShader(fragment);
        glDeleteShader(vertex);
    }

    private String loadResource(String path) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("resourse not found: " + path);
            }
            return new String(stream.readAllBytes());
        } 
    }
}
