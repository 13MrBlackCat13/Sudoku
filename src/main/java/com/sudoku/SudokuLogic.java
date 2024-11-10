package com.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SudokuLogic {
    private int[][] board;
    private int[][] solution;
    private final int GRID_SIZE = 9;
    private final Random random = new Random();
    private Difficulty currentDifficulty;

    public enum Difficulty {
        EASY(35),    // 35 клеток заполнены
        MEDIUM(30),  // 30 клеток заполнены
        HARD(25);    // 25 клеток заполнены

        private final int filledCells;

        Difficulty(int filledCells) {
            this.filledCells = filledCells;
        }
    }

    public SudokuLogic() {
        board = new int[GRID_SIZE][GRID_SIZE];
        solution = new int[GRID_SIZE][GRID_SIZE];
        currentDifficulty = Difficulty.EASY;
    }

    public void generatePuzzle(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        clearBoard();

        boolean success = generateSolution();
        if (!success) {
            System.err.println("Failed to generate solution");
            return;
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(solution[i], 0, board[i], 0, GRID_SIZE);
        }

        createPuzzle(difficulty);

        System.out.println("Generated board with difficulty: " + difficulty);
        printBoard();
    }

    private void printBoard() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    private void clearBoard() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                board[i][j] = 0;
                solution[i][j] = 0;
            }
        }
    }

    private boolean generateSolution() {
        return fillBoard(0, 0, solution);
    }

    private boolean fillBoard(int row, int col, int[][] grid) {
        if (col >= GRID_SIZE) {
            row++;
            col = 0;
        }

        if (row >= GRID_SIZE) {
            return true;
        }

        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);

        for (int number : numbers) {
            if (isValidMove(row, col, number, grid)) {
                grid[row][col] = number;

                if (fillBoard(row, col + 1, grid)) {
                    return true;
                }

                grid[row][col] = 0;
            }
        }

        return false;
    }

    private void createPuzzle(Difficulty difficulty) {
        int cellsToRemove = GRID_SIZE * GRID_SIZE - difficulty.filledCells;

        while (cellsToRemove > 0) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);

            if (board[row][col] != 0) {
                board[row][col] = 0;
                cellsToRemove--;
            }
        }
    }

    private boolean isValidMove(int row, int col, int number, int[][] grid) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (grid[row][i] == number) {
                return false;
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            if (grid[i][col] == number) {
                return false;
            }
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[boxRow + i][boxCol + j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    public int[][] getBoard() {
        return board;
    }

    public int[][] getSolution() {
        return solution;
    }

    public boolean isComplete() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] != solution[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidMove(int row, int col, int number) {
        if (board[row][col] != 0) {
            return false;
        }

        board[row][col] = number;

        boolean isValid = isValidPosition(row, col);

        board[row][col] = 0;

        return isValid;
    }

    private boolean isValidPosition(int row, int col) {
        int number = board[row][col];

        for (int j = 0; j < 9; j++) {
            if (j != col && board[row][j] == number) {
                return false;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (i != row && board[i][col] == number) {
                return false;
            }
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int currentRow = boxRow + i;
                int currentCol = boxCol + j;
                if ((currentRow != row || currentCol != col) &&
                        board[currentRow][currentCol] == number) {
                    return false;
                }
            }
        }

        return true;
    }
}