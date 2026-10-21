#version 330 core
#pragma shader_stage(fragment)

in vec4 vertexColor;

out vec4 fragColour;

void main() {
    fragColour = vertexColor;
}

