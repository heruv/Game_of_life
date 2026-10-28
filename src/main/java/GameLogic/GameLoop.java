package GameLogic;

import Events.GameEvent;
import Events.GameEventListener;

public class GameLoop implements GameEventListener {
    private final GameOfLife world;
    private final double stepInterval;   // seconds between generations

    private boolean paused = false;
    private double lastStep = -1;        // -1 means "clock not started yet"

    public GameLoop(GameOfLife world, double stepInterval) {
        this.world = world;
        this.stepInterval = stepInterval;
    }

    @Override
    public void onEvent(GameEvent event, Object data) {
        switch (event) {
            case TOGGLE_PAUSE:
                paused = !paused;
                break;

            case TICK:
                handleTick((Double) data);
                break;

            default:
                break;
        }
    }

    private void handleTick(double now) {
        if (lastStep < 0) {
            lastStep = now;          // the first tick only starts the clock
            return;
        }

        if (!paused && now - lastStep >= stepInterval) {
            lastStep = now;
            world.proceed();         // publishes STATE_CHANGED
        }
    }
}