package GameLogic;

public class GameOfLife {
    public int gridSize;

    public boolean[][] grid;


    public GameOfLife(int gridSize) {
        this.gridSize = gridSize;
        this.grid = new boolean[gridSize][gridSize]; 
    }

    public boolean getCellState(int xPos, int yPos) {
        if (xPos < 0 || xPos >= gridSize || yPos < 0 || yPos >= gridSize) {
            return false;
        }

        return grid[xPos][yPos];
    }

    public void setCellState(int xPos, int yPos, boolean state) {
        try {
            grid[xPos][yPos] = state;
        } catch (IndexOutOfBoundsException iob) {
            return ;
        }
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
    }
}
