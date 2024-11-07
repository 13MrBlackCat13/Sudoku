import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Map;

public class GameController {
    @FXML
    private GridPane gamePane;

    @FXML
    private Button backButton;

    @FXML
    private Button restartButton;

    @FXML
    private Button hintButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button loadButton;

    @FXML
    private Label timerLabel;

    private SudokuLogic game;
    private TextField[][] cells;
    private SudokuLogic.Difficulty currentDifficulty;
    private Timeline timer;
    private int secondsElapsed;
    private boolean isProcessingInput = false;

    @FXML
    public void initialize() {
        System.out.println("GameController initialization started");

        if (gamePane == null) {
            System.err.println("Error: gamePane is not initialized!");
            return;
        }

        game = new SudokuLogic();
        cells = new TextField[9][9];
        secondsElapsed = 0;

        setupTimer();
        initializeCells();

        System.out.println("Board state after initialization:");
        if (game.getBoard() != null) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    System.out.print(game.getBoard()[i][j] + " ");
                }
                System.out.println();
            }
        }

        System.out.println("GameController initialization completed");
    }

    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            updateTimerLabel();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void initializeCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String cellId = String.format("#cell_%d_%d", i, j);
                TextField cell = (TextField) gamePane.lookup(cellId);

                if (cell == null) {
                    System.err.printf("Could not find cell with id: %s%n", cellId);
                    continue;
                }

                cell.clear();
                cells[i][j] = cell;

                final int row = i;
                final int col = j;

                // Добавляем обработчик клавиш
                cell.setOnKeyPressed(event -> {
                    System.out.println("Key pressed: " + event.getCode());
                    if (!cell.isEditable()) {
                        System.out.println("Cell is not editable");
                        return;
                    }

                    String keyValue = event.getText();
                    System.out.println("Key value: " + keyValue);

                    if (keyValue.matches("[1-9]")) {
                        int value = Integer.parseInt(keyValue);
                        System.out.println("Attempting to set value: " + value + " at [" + row + "," + col + "]");

                        if (game.isValidMove(row, col, value)) {
                            System.out.println("Valid move detected");
                            game.getBoard()[row][col] = value;
                            cell.setText(keyValue);

                            if (game.isComplete()) {
                                timer.stop();
                                showAlert(LanguageManager.getInstance().getString("dialog.congratulations"),
                                        LanguageManager.getInstance().getString("dialog.puzzle.solved"));
                            }
                        } else {
                            System.out.println("Invalid move detected");
                            cell.setText("");
                        }
                    }
                });

                // Обработчик для предотвращения нежелательного ввода
                cell.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!cell.isFocused()) return;

                    if (newValue == null || newValue.isEmpty()) {
                        game.getBoard()[row][col] = 0;
                        return;
                    }

                    // Если введено что-то кроме одной цифры от 1 до 9
                    if (!newValue.matches("[1-9]")) {
                        Platform.runLater(() -> cell.setText(oldValue));
                    }
                });

                // Обработчик фокуса для подсветки
                cell.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (isFocused) {
                        System.out.println("Cell focused: " + row + "," + col);
                        highlightRelatedCells(row, col);
                    } else {
                        unhighlightAllCells();
                    }
                });
            }
        }
    }

    // Добавляем метод для проверки состояния ячейки
    private void debugCellState(TextField cell, int row, int col) {
        System.out.println("Cell [" + row + "," + col + "]:");
        System.out.println("- Editable: " + cell.isEditable());
        System.out.println("- Focused: " + cell.isFocused());
        System.out.println("- Current value: " + cell.getText());
        System.out.println("- Board value: " + game.getBoard()[row][col]);
    }



    private void setupCellBehavior(TextField cell, int row, int col) {
        // Ограничиваем ввод одной цифрой
        cell.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isProcessingInput) return;

            isProcessingInput = true;
            try {
                if (newValue == null || newValue.isEmpty()) {
                    game.getBoard()[row][col] = 0;
                    return;
                }

                // Оставляем только первый символ если введено больше
                if (newValue.length() > 1) {
                    cell.setText(newValue.substring(0, 1));
                    return;
                }

                // Проверяем, что введена цифра от 1 до 9
                if (!newValue.matches("[1-9]")) {
                    cell.setText(oldValue);
                    return;
                }

                int value = Integer.parseInt(newValue);
                if (game.isValidMove(row, col, value)) {
                    game.getBoard()[row][col] = value;

                    if (game.isComplete()) {
                        timer.stop();
                        Platform.runLater(() -> {
                            showAlert("Поздравляем!", "Вы успешно решили головоломку!");
                        });
                    }
                } else {
                    cell.setText("");
                }
            } finally {
                isProcessingInput = false;
            }
        });

        // Добавляем подсветку связанных ячеек
        cell.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                highlightRelatedCells(row, col);
            } else {
                unhighlightAllCells();
            }
        });
    }

    private void highlightRelatedCells(int row, int col) {
        String highlightStyle = "-fx-background-color: #f0f0f0;";

        for (int i = 0; i < 9; i++) {
            if (cells[row][i] != null) cells[row][i].setStyle(highlightStyle);
            if (cells[i][col] != null) cells[i][col].setStyle(highlightStyle);
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[boxRow + i][boxCol + j] != null) {
                    cells[boxRow + i][boxCol + j].setStyle(highlightStyle);
                }
            }
        }
    }

    private void unhighlightAllCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j] != null) {
                    if (!cells[i][j].isEditable()) {
                        cells[i][j].setStyle("-fx-background-color: #f8f8f8;");
                    } else {
                        cells[i][j].setStyle("");
                    }
                }
            }
        }
    }

    public void startGame(SudokuLogic.Difficulty difficulty) {
        System.out.println("Starting new game with difficulty: " + difficulty);

        if (timer != null) {
            timer.stop();
        }

        this.currentDifficulty = difficulty;
        game.generatePuzzle(difficulty);

        Platform.runLater(() -> {
            try {
                int[][] currentBoard = game.getBoard();

                // Сначала делаем все ячейки редактируемыми и очищаем их
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (cells[i][j] != null) {
                            TextField cell = cells[i][j];
                            cell.setEditable(true);
                            cell.clear();
                            cell.setStyle(null);
                        }
                    }
                }

                // Затем устанавливаем начальные значения
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        TextField cell = cells[i][j];
                        int value = currentBoard[i][j];

                        if (cell != null) {
                            isProcessingInput = true;
                            try {
                                if (value != 0) {
                                    cell.setText(String.valueOf(value));
                                    cell.setEditable(false);
                                    cell.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #000000; -fx-font-weight: bold;");
                                }
                                debugCellState(cell, i, j);
                            } finally {
                                isProcessingInput = false;
                            }
                        }
                    }
                }

                resetTimer();
                timer.play();
            } catch (Exception e) {
                System.err.println("Error updating board: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Добавляем вспомогательный метод для отладки
    private void printBoard(int[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    private void resetTimer() {
        secondsElapsed = 0;
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        Platform.runLater(() -> {
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        });
    }

    private void updateBoard() {
        if (game == null || cells == null) {
            System.err.println("Game or cells array is null");
            return;
        }

        int[][] board = game.getBoard();
        System.out.println("Updating board with current state:");

        Platform.runLater(() -> {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    TextField cell = cells[i][j];
                    int value = board[i][j];

                    if (cell != null) {
                        if (value != 0) {
                            cell.setText(String.valueOf(value));
                            cell.setEditable(false);
                            cell.setStyle("-fx-background-color: #f8f8f8; " +
                                    "-fx-text-fill: #000000; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-alignment: center; " +
                                    "-fx-font-size: 20px;");
                        } else {
                            cell.clear();
                            cell.setEditable(true);
                            cell.setStyle("-fx-background-color: white; " +
                                    "-fx-alignment: center; " +
                                    "-fx-font-size: 20px;");
                        }
                    }
                }
            }
        });
    }


    @FXML
    private void handleHint() {
        if (game == null) return;

        int[][] solution = game.getSolution();
        int[][] current = game.getBoard();

        java.util.List<int[]> emptyCells = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (current[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(new java.util.Random().nextInt(emptyCells.size()));
            cells[cell[0]][cell[1]].setText(String.valueOf(solution[cell[0]][cell[1]]));
            cells[cell[0]][cell[1]].setStyle("-fx-text-fill: #0000FF;");
        }
    }

    @FXML
    private void handleRestart() {
        if (game != null && currentDifficulty != null) {
            game.generatePuzzle(currentDifficulty);
            updateBoard();
            resetTimer();
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (timer != null) {
                timer.stop();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("interface.fxml"),
                    LanguageManager.getInstance().getBundle()
            );
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Could not return to main menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        TextInputDialog dialog = new TextInputDialog("game1");
        dialog.setTitle("Сохранение игры");
        dialog.setHeaderText("Введите имя сохранения:");
        dialog.setContentText("Имя:");

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                GameSaver.GameState state = new GameSaver.GameState(
                        game.getBoard(),
                        game.getSolution(),
                        game.getCurrentDifficulty(),
                        secondsElapsed
                );
                GameSaver.saveGame(saveName, state);
                showAlert("Сохранение", "Игра успешно сохранена!");
            } catch (IOException e) {
                showError("Ошибка сохранения", "Не удалось сохранить игру: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleLoad() {
        Map<String, Long> saves = GameSaver.getSaveFiles();
        if (saves.isEmpty()) {
            showAlert("Загрузка", "Нет доступных сохранений!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                saves.keySet().iterator().next(),
                saves.keySet()
        );
        dialog.setTitle("Загрузка игры");
        dialog.setHeaderText("Выберите сохранение:");
        dialog.setContentText("Сохранение:");

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                GameSaver.GameState state = GameSaver.loadGame(saveName);
                loadGameState(state);
                showAlert("Загрузка", "Игра успешно загружена!");
            } catch (Exception e) {
                showError("Ошибка загрузки", "Не удалось загрузить игру: " + e.getMessage());
            }
        });
    }

    private void loadGameState(GameSaver.GameState state) {
        if (timer != null) {
            timer.stop();
        }

        game = new SudokuLogic();
        currentDifficulty = state.getDifficulty();
        secondsElapsed = state.getTimeElapsed();

        updateTimerLabel();
        timer.play();

        updateBoardFromState(state.getBoard());
    }

    private void updateBoardFromState(int[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j] != null) {
                    String value = board[i][j] == 0 ? "" : String.valueOf(board[i][j]);
                    cells[i][j].setText(value);
                    cells[i][j].setEditable(board[i][j] == 0);
                }
            }
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}