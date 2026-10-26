import GameLogic.GameOfLife;
import Render.Window;

public class Main {

    public static void main(String[] args) {
        int gridSize = 800;
        int wigth = gridSize, heigth = gridSize;

        GameOfLife world = new GameOfLife(gridSize);
        Window window = new Window(wigth, heigth);

        window.run();
    }
}