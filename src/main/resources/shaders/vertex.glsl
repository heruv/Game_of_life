#version 330 core

layout (location=0) in vec2 aPos;

out vec4 vertexColor;

void main() {
    gl_position = vec4(aPos, 0.0, 0.0);
    vertecColor = vec4(0.5, 0.0, 0.0, 1.0);
}