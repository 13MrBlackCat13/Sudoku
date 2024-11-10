import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        LanguageManager langManager = LanguageManager.getInstance();
        System.out.println("Current locale: " + langManager.getCurrentLocale());
        System.out.println("Bundle: " + langManager.getBundle());

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("interface.fxml"),
                    langManager.getBundle()
            );
            System.out.println("FXML location: " + getClass().getResource("interface.fxml"));
            System.out.println("ResourceBundle: " + loader.getResources());

            Parent root = loader.load();
            Scene scene = new Scene(root);
            // Используем локализованный заголовок
            primaryStage.setTitle(langManager.getString("window.title"));
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}