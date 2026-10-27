import GameLogic.GameOfLife;
import Render.Window;

public class Main {

    public static void main(String[] args) {
        int gridSize = 100;
        int wigth = 800, heigth = 800;

        GameOfLife world = new GameOfLife(gridSize);
        Window window = new Window(wigth, heigth, world);

        world.grid[2][2] = true;
        world.grid[2][3] = true;
        world.grid[2][4] = true;
        world.grid[1][4] = true;
        world.grid[0][3] = true;
        
        window.run();
    }
}