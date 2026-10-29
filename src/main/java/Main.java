import Events.EventBus;
import GameLogic.GameLoop;
import GameLogic.GameOfLife;
import Render.Window;

public class Main {

    public static void main(String[] args) {
        int gridSize = 100;
        int wigth = 800, heigth = 800;

        EventBus bus = new EventBus();

        GameOfLife world = new GameOfLife(gridSize, bus);
        Window window = new Window(wigth, heigth, gridSize, bus);
        GameLoop loop = new GameLoop(world, 0.15);

        bus.subscribe(loop);
        bus.subscribe(window);

        window.run();
    }
}