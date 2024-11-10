import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameSaver {
    private static final String SAVE_DIRECTORY = "saves";
    private static final String FILE_EXTENSION = ".sudoku";

    static {
        new File(SAVE_DIRECTORY).mkdirs();
    }

    public static class GameState implements Serializable {
        private final int[][] board;
        private final int[][] solution;
        private final SudokuLogic.Difficulty difficulty;
        private final int timeElapsed;

        public GameState(int[][] board, int[][] solution,
                         SudokuLogic.Difficulty difficulty, int timeElapsed) {
            this.board = deepCopy(board);
            this.solution = deepCopy(solution);
            this.difficulty = difficulty;
            this.timeElapsed = timeElapsed;
        }

        private int[][] deepCopy(int[][] original) {
            int[][] copy = new int[original.length][];
            for (int i = 0; i < original.length; i++) {
                copy[i] = original[i].clone();
            }
            return copy;
        }

        public int[][] getBoard() {
            return deepCopy(board);
        }

        public int[][] getSolution() {
            return deepCopy(solution);
        }

        public SudokuLogic.Difficulty getDifficulty() {
            return difficulty;
        }

        public int getTimeElapsed() {
            return timeElapsed;
        }
    }

    public static void saveGame(String saveName, GameState state) throws IOException {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            oos.writeObject(state);
        }
    }

    public static GameState loadGame(String saveName) throws IOException, ClassNotFoundException {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {
            return (GameState) ois.readObject();
        }
    }

    public static Map<String, Long> getSaveFiles() {
        File saveDir = new File(SAVE_DIRECTORY);
        Map<String, Long> saves = new HashMap<>();
        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
        if (files != null) {
            for (File file : files) {
                saves.put(file.getName().replace(FILE_EXTENSION, ""),
                        file.lastModified());
            }
        }
        return saves;
    }

    public static void deleteSave(String saveName) {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        new File(filePath).delete();
    }
}