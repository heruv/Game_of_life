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
        checkCompile(vertex, "VERTEX");

        fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fragmentCode);
        glCompileShader(fragment);
        checkCompile(fragment, "FRAGMENT");

        ID = glCreateProgram();
        glAttachShader(ID, vertex);
        glAttachShader(ID, fragment);
        glLinkProgram(ID);
        if (glGetProgrami(ID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program link error:\n" + glGetProgramInfoLog(ID));
        }

        glDeleteShader(fragment);
        glDeleteShader(vertex);
    }

    public void setColour(float red, float green, float blue, float alpha) {
        int colourLocation = glGetUniformLocation(this.ID, "vertexColour");

        if (colourLocation == -1) {
            System.out.println("can't find uniform variable");
        }

        glUniform4f(colourLocation, red, green, blue, alpha);
    }

    public void setColour(float red, float green, float blue) {
        setColour(red, green, blue, 1.0f);
    }

    public void setUniformInt(String name, int value) {
        int loc = glGetUniformLocation(this.ID, name);
        if (loc != -1) {
            glUniform1i(loc, value);
        }
    }

    private String loadResource(String path) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("resourse not found: " + path);
            }
            return new String(stream.readAllBytes());
        } 
    }

    private void checkCompile(int shader, String type) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(type + "shader compile erroe:\n" + glGetShaderInfoLog(shader));
        }
    }
}
