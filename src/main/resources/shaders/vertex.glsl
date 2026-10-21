#version 330 core
#pragma shader_stage(vertex)

layout (location=0) in vec2 aPos;

out vec4 vertexColor;

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);
    vertexColor = vec4(1.0, 1.0, 1.0, 1.0);
}