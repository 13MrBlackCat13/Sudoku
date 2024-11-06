import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private Button buttonEasy;

    @FXML
    private Button buttonMedium;

    @FXML
    private Button buttonHard;

    @FXML
    private ComboBox<LanguageManager.Language> languageSelector;

    @FXML
    public void initialize() {
        LOGGER.info("Controller initialized");

        // Инициализация селектора языка
        languageSelector.getItems().addAll(LanguageManager.Language.values());
        languageSelector.setValue(LanguageManager.getInstance().getCurrentLanguage());
    }

    @FXML
    private void handleLanguageChange() {
        LanguageManager.Language selectedLanguage = languageSelector.getValue();
        if (selectedLanguage != null) {
            LanguageManager.getInstance().setLanguage(selectedLanguage);
            // Перезагрузка сцены для применения нового языка
            reloadScene();
        }
    }

    private void reloadScene() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("interface.fxml"),
                    LanguageManager.getInstance().getBundle()
            );
            Parent root = loader.load();
            Stage stage = (Stage) buttonEasy.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reloading scene", e);
            showError("Error", "Failed to change language: " + e.getMessage());
        }
    }

    private void startGame(String fxmlFile, SudokuLogic.Difficulty difficulty) {
        try {
            LOGGER.info("Starting game with difficulty: " + difficulty);

            // Создаем loader с передачей ResourceBundle
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlFile),
                    LanguageManager.getInstance().getBundle()
            );

            Parent root = loader.load();

            GameController gameController = loader.getController();
            if (gameController == null) {
                throw new RuntimeException("Failed to get GameController");
            }

            Stage stage = (Stage) buttonEasy.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            Platform.runLater(() -> {
                try {
                    Thread.sleep(100);
                    gameController.startGame(difficulty);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error starting game", e);
                    showError("Game Error", "Failed to start game: " + e.getMessage());
                }
            });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading FXML", e);
            showError("Loading Error", "Failed to load game interface: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            showError("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void openEasy() {
        LOGGER.info("Opening easy difficulty");
        startGame("easy.fxml", SudokuLogic.Difficulty.EASY);
    }

    @FXML
    private void openMedium() {
        LOGGER.info("Opening medium difficulty");
        startGame("medium.fxml", SudokuLogic.Difficulty.MEDIUM);
    }

    @FXML
    private void openHard() {
        LOGGER.info("Opening hard difficulty");
        startGame("hard.fxml", SudokuLogic.Difficulty.HARD);
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}