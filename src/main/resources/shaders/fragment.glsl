#version 330 core
#pragma shader_stage(fragment)

in vec3 vColor;
uniform vec4 vertexColour;
uniform int useVertexColor;

out vec4 fragColour;

void main() {
    if (useVertexColor == 1) {
        fragColour = vec4(vColor, 1.0);
    } else {
        fragColour = vertexColour;
    }
}

