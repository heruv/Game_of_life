#version 330 core
#pragma shader_stage(fragment)

uniform vec4 vertexColour;

out vec4 fragColour;

void main() {
    fragColour = vertexColour;
}

