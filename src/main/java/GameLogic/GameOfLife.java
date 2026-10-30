package GameLogic;

import Events.EventBus;
import Events.GameEvent;

public class GameOfLife {
    public int gridSize;
    public boolean[][] grid;

    private EventBus bus;

    public GameOfLife(int gridSize, EventBus bus) {
        this.gridSize = gridSize;
        this.grid = new boolean[gridSize][gridSize];
        this.bus = bus;
    }

    public GameOfLife(int gridSize) {
        this(gridSize, new EventBus());
    }   

    public boolean getCellState(int xPos, int yPos) {
        if (xPos < 0 || xPos >= gridSize || yPos < 0 || yPos >= gridSize) {
            return false;
        }

        return grid[xPos][yPos];
    }

    public void setCellState(int xPos, int yPos) {
        if (xPos < 0 || xPos >= gridSize || yPos < 0 || yPos >= gridSize) {
            return ;
        }

        grid[xPos][yPos] = !grid[xPos][yPos];

        bus.publish(GameEvent.CELL_EDITED, new int[]{xPos, yPos, grid[xPos][yPos] ? 1 : 0});
    }

    /**
     * make every cell dead, for correect swaping buffers logick
     */
    public void anigilation() {
        // is it posible to use one cicle here
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = false;
            }
        }

        bus.publish(GameEvent.STATE_CHANGED, grid);
    }

    public void proceed() {
        boolean[][] next = new boolean[grid.length][grid[0].length];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                int neighbours = 0;

                if (getCellState(i - 1, j - 1)) neighbours++;
                if (getCellState(i - 1, j))     neighbours++;
                if (getCellState(i - 1, j + 1)) neighbours++;
                if (getCellState(i,     j - 1)) neighbours++;
                if (getCellState(i,     j + 1)) neighbours++;
                if (getCellState(i + 1, j - 1)) neighbours++;
                if (getCellState(i + 1, j))     neighbours++;
                if (getCellState(i + 1, j + 1)) neighbours++;

                if (grid[i][j]) {
                    //cell survied
                    next[i][j] = neighbours == 2 || neighbours == 3;
                } else {
                    //dead cell alive
                    next[i][j] = neighbours == 3;
                }
            }
        }

        grid = next;
        bus.publish(GameEvent.STATE_CHANGED, next); //TODO: replace next to copyofgrid() later
    }

    public void print() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                sb.append(getCellState(i, j) ? '#' : '.');
            }
            sb.append('\n');
        }

        System.out.print(sb);
    }

}
