#version 330 core

in vec4 vertexColor;

out vec4 fragColour;

void main() {
    fragColour = vertexColor;
}

