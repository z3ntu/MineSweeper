package xyz.z3ntu.minesweeper;

import java.util.Random;

public class Game {
    public int cols, rows;
    private GameState[][] gameState;
    private ShowState[][] showState;
    private int numVisible;
    private int numFlagged;

    public Game(int cols, int rows, int numBombs) {
        setup(cols, rows, numBombs);
    }

    public void setGameState(GameState[][] gameState) {
        this.gameState = gameState;
    }

    public void setShowState(ShowState[][] showState) {
        this.showState = showState;
    }

    public GameState getGameState(int x, int y) {
        return gameState[x][y];
    }

    public ShowState getShowState(int x, int y) {
        return showState[x][y];
    }

    public void setup(int cols, int rows, int numBombs) {
        this.cols = cols;
        this.rows = rows;
        gameState = new GameState[cols][rows];
        showState = new ShowState[cols][rows];
        numVisible = 0;
        numFlagged = 0;
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++) {
                gameState[i][j] = GameState.ZERO;
                showState[i][j] = ShowState.HIDDEN;
            }
        Random rand = new Random();
        for (int i = 0; i < numBombs; i++) {
            int bx = rand.nextInt(cols);
            int by = rand.nextInt(rows);
            if (gameState[bx][by] == GameState.ZERO) gameState[bx][by] = GameState.BOMB;
            else i--;
        }
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++) {
                if (gameState[i][j] == GameState.BOMB) {
                    if (i > 0 && j > 0 && gameState[i - 1][j - 1] != GameState.BOMB)
                        gameState[i - 1][j - 1] = gameState[i - 1][j - 1].inc();
                    if (j > 0 && gameState[i][j - 1] != GameState.BOMB)
                        gameState[i][j - 1] = gameState[i][j - 1].inc();
                    if (i < cols - 1 && j > 0 && gameState[i + 1][j - 1] != GameState.BOMB)
                        gameState[i + 1][j - 1] = gameState[i + 1][j - 1].inc();
                    if (i > 0 && gameState[i - 1][j] != GameState.BOMB)
                        gameState[i - 1][j] = gameState[i - 1][j].inc();
                    if (i < cols - 1 && gameState[i + 1][j] != GameState.BOMB)
                        gameState[i + 1][j] = gameState[i + 1][j].inc();
                    if (i > 0 && j < rows - 1 && gameState[i - 1][j + 1] != GameState.BOMB)
                        gameState[i - 1][j + 1] = gameState[i - 1][j + 1].inc();
                    if (j < rows - 1 && gameState[i][j + 1] != GameState.BOMB)
                        gameState[i][j + 1] = gameState[i][j + 1].inc();
                    if (i < cols - 1 && j < rows - 1 && gameState[i + 1][j + 1] != GameState.BOMB)
                        gameState[i + 1][j + 1] = gameState[i + 1][j + 1].inc();
                }
            }
    }

    public Result uncover(int x, int y) {
        if (cols * rows - numVisible - numFlagged == 0)
            return Result.INVALID;
        if (showState[x][y] == ShowState.HIDDEN) {
            showState[x][y] = ShowState.VISIBLE;
            numVisible++;
            if (gameState[x][y] == GameState.BOMB) {
                uncoverAll();
                return Result.HIT;
            }
            if (gameState[x][y] == GameState.ZERO) recursiveUncover(x, y);
            if (cols * rows - numVisible - numFlagged == 0)
                return Result.WIN;
            return Result.VALID;
        }
        return Result.INVALID;
    }

    public void uncoverAll() {
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                showState[i][j] = ShowState.VISIBLE;
    }

    public void recursiveUncover(int x, int y) {
        if (x > 0 && y > 0) recUncover(x - 1, y - 1);
        if (y > 0) recUncover(x, y - 1);
        if (x < cols - 1 && y > 0) recUncover(x + 1, y - 1);
        if (x > 0) recUncover(x - 1, y);
        if (x < cols - 1) recUncover(x + 1, y);
        if (x > 0 && y < rows - 1) recUncover(x - 1, y + 1);
        if (y < rows - 1) recUncover(x, y + 1);
        if (x < cols - 1 && y < rows - 1) recUncover(x + 1, y + 1);

    }

    private void recUncover(int x, int y) {
        if (showState[x][y] == ShowState.HIDDEN) {
            numVisible++;
            showState[x][y] = ShowState.VISIBLE;
            if (gameState[x][y] == GameState.ZERO) recursiveUncover(x, y);
        }
    }

    public Result flag(int x, int y) {
        if (cols * rows - numVisible - numFlagged == 0)
            return Result.INVALID;
        if (showState[x][y] == ShowState.HIDDEN) {
            numFlagged++;
            showState[x][y] = ShowState.FLAGGED;
            if (cols * rows - numVisible - numFlagged == 0)
                return Result.WIN;
            return Result.VALID;
        } else if (showState[x][y] == ShowState.FLAGGED) {
            numFlagged--;
            showState[x][y] = ShowState.HIDDEN;
            return Result.VALID;
        }
        return Result.INVALID;
    }

    public enum ShowState {
        HIDDEN(0), VISIBLE(1), FLAGGED(2);
        private final int num;

        ShowState(int i) {
            num = i;
        }

        public static ShowState showStateFromInt(int i) {
            return ShowState.values()[i];
        }

        public int getInt() {
            return num;
        }
    }

    public enum Result {WIN, HIT, VALID, INVALID}

    public enum GameState {
        BOMB(-1, "\uD83D\uDCA3"), ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8);
        private final int num;
        private final String str;

        GameState(int n) {
            num = n;
            str = String.valueOf(n);
        }

        GameState(int n, String s) {
            num = n;
            str = s;
        }

        public static GameState gameStateFromInt(int i) {
            return GameState.values()[i + 1];
        }

        public String getStr() {
            return str;
        }

        public int getInt() {
            return num;
        }

        public GameState inc() {
            if (num >= 0 && num < 8)
                return GameState.values()[num + 2];
            else throw new ArrayIndexOutOfBoundsException("Wrong value in enum GameState");
        }
    }
}
