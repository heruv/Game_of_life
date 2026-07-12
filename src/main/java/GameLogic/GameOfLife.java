package GameLogic;

public class GameOfLife {
    // TO-DO: add cicleTime field

    public int gridSize;
    //probably should be private
    public boolean[][] grid = new boolean[gridSize][gridSize]; // 0 - dead cell | 1 - live cell


    public GameOfLife(int gridSize) {
        this.gridSize = gridSize;
    }

    public void updateState() {
        for (int i = 0; i < gridSize-1; i++) {
            for (int j = 0; j < gridSize-1; j++) {
                int neigbors = checkNeigbors(i, j);
                if (neigbors < 2 | neigbors > 3) {
                    deadCell(grid[i][j]); 
                }
                else if (neigbors == 3) aliveCell(grid[i][j]);
            }
        }
    }

    private int checkNeigbors(int indexX, int indexY) {
        int neigborsCount = 0;

        boolean[] neigbors = new boolean[8];

        neigbors[0] = grid[indexX][indexY+1];
        neigbors[1] = grid[indexX+1][indexY+1];
        neigbors[2] = grid[indexX+1][indexY];
        neigbors[3] = grid[indexX+1][indexY-1];
        neigbors[4] = grid[indexX][indexY-1];
        neigbors[5] = grid[indexX-1][indexY-1];
        neigbors[6] = grid[indexX-1][indexY];
        neigbors[7] = grid[indexX-1][indexY+1];

        for (boolean cell : neigbors) {
            if (cell == true) neigborsCount++;
        }

        return neigborsCount;
    }

    // probably should make one function from this two functions
    private void deadCell(boolean cell) {
        cell = false;
    }

    private void aliveCell (boolean cell) {
        cell = true;
    }
}
