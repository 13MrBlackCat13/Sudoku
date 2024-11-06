import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class NewFXMLGenerator {
    // UI Constants
    private static final String BG_COLOR = "#588157";
    private static final String BUTTON_COLOR = "#e0e0e0";

    // Style Classes
    private static final String MAIN_CONTAINER_CLASS = "main-container";
    private static final String CONTROL_PANEL_CLASS = "control-panel";
    private static final String CONTROL_BUTTON_CLASS = "control-button";
    private static final String GAME_BUTTON_CLASS = "game-button";
    private static final String TIMER_LABEL_CLASS = "timer-label";
    private static final String GAME_GRID_CLASS = "game-grid";
    private static final String GAME_CELL_CLASS = "game-cell";

    // Layout Constants
    private static final int PADDING = 20;
    private static final int SPACING = 10;
    private static final int VBOX_SPACING = 20;
    private static final int GRID_SIZE = 9;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        try {
            generateFXMLFiles();
            System.out.println("FXML files successfully generated!");
        } catch (IOException e) {
            System.err.println("Error generating FXML files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateFXMLFiles() throws IOException {
        String[] difficulties = {"easy", "medium", "hard"};
        for (String difficulty : difficulties) {
            generateGameFXML(difficulty + ".fxml");
        }
        generateMainMenuFXML("interface.fxml");
    }

    private static void generateGameFXML(String filename) throws IOException {
        Path filePath = Paths.get("src", filename);
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writeHeader(writer);
            writeMainContainer(writer);
        }
    }

    private static void writeHeader(PrintWriter writer) {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println();
        writer.println("<?import javafx.scene.layout.*?>");
        writer.println("<?import javafx.scene.control.*?>");
        writer.println("<?import javafx.scene.text.*?>");
        writer.println("<?import javafx.geometry.Insets?>");
        writer.println("<?import java.lang.String?>");
        writer.println();
    }

    private static void writeMainContainer(PrintWriter writer) {
        writer.printf("<BorderPane xmlns=\"http://javafx.com/javafx/23\" " +
                        "xmlns:fx=\"http://javafx.com/fxml/1\" " +
                        "fx:controller=\"GameController\" " +
                        "styleClass=\"%s\" " +
                        "style=\"-fx-background-color: %s;\">%n",
                MAIN_CONTAINER_CLASS, BG_COLOR);

        writeControlPanel(writer);
        writeGameGrid(writer);
        writeStylesheets(writer);

        writer.println("</BorderPane>");
    }

    private static void writeControlPanel(PrintWriter writer) {
        writer.println("    <left>");
        writer.printf("        <VBox spacing=\"%d\" styleClass=\"%s\">%n",
                VBOX_SPACING, CONTROL_PANEL_CLASS);

        writePadding(writer, 3);

        // Back Button
        writeButton(writer, "backButton", "%game.back", "#handleBack",
                CONTROL_BUTTON_CLASS, 3);

        // Timer Label
        writer.printf("            <Label fx:id=\"timerLabel\" " +
                "text=\"00:00\" styleClass=\"%s\"/>%n", TIMER_LABEL_CLASS);

        // Save/Load Buttons Group
        writeButtonGroup(writer, new String[][]{
                {"saveButton", "%game.save", "#handleSave"},
                {"loadButton", "%game.load", "#handleLoad"}
        }, 3);

        // Restart/Hint Buttons Group
        writeButtonGroup(writer, new String[][]{
                {"restartButton", "%game.restart", "#handleRestart"},
                {"hintButton", "%game.hint", "#handleHint"}
        }, 3);

        writer.println("        </VBox>");
        writer.println("    </left>");
    }

    private static void writeGameGrid(PrintWriter writer) {
        writer.println("    <center>");
        writer.println("        <StackPane alignment=\"CENTER\" style=\"-fx-padding: 20;\">");

        // Добавляем GridPane для игрового поля
        writer.printf("            <GridPane fx:id=\"gamePane\" styleClass=\"%s\" " +
                "hgap=\"1\" vgap=\"1\" alignment=\"CENTER\">%n", GAME_GRID_CLASS);

        // Настройки колонок
        for (int i = 0; i < GRID_SIZE; i++) {
            writer.printf("                <columnConstraints>%n");
            writer.printf("                    <ColumnConstraints hgrow=\"SOMETIMES\" " +
                    "minWidth=\"50.0\" prefWidth=\"50.0\"/>%n");
            writer.println("                </columnConstraints>");
        }

        // Настройки строк
        for (int i = 0; i < GRID_SIZE; i++) {
            writer.printf("                <rowConstraints>%n");
            writer.printf("                    <RowConstraints vgrow=\"SOMETIMES\" " +
                    "minHeight=\"50.0\" prefHeight=\"50.0\"/>%n");
            writer.println("                </rowConstraints>");
        }

        // Генерация ячеек
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                writer.printf("                <TextField fx:id=\"cell_%d_%d\" " +
                                "styleClass=\"%s\" " +
                                "alignment=\"CENTER\" " +
                                "maxWidth=\"50\" maxHeight=\"50\" " +
                                "GridPane.rowIndex=\"%d\" GridPane.columnIndex=\"%d\"/>%n",
                        i, j, GAME_CELL_CLASS, i, j);
            }
        }

        writer.println("            </GridPane>");
        writer.println("        </StackPane>");
        writer.println("    </center>");
    }

    private static void writePadding(PrintWriter writer, int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        writer.println(indent + "<padding>");
        writer.printf(indent + "    <Insets top=\"%d\" right=\"%d\" bottom=\"%d\" left=\"%d\"/>%n",
                PADDING, PADDING, PADDING, PADDING);
        writer.println(indent + "</padding>");
    }

    private static void writeButton(PrintWriter writer, String id, String text,
                                    String action, String styleClass, int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        writer.printf(indent + "<Button fx:id=\"%s\" text=\"%s\" " +
                        "onAction=\"%s\" styleClass=\"%s\"/>%n",
                id, text, action, styleClass);
    }

    private static void writeButtonGroup(PrintWriter writer, String[][] buttons,
                                         int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        writer.printf(indent + "<HBox spacing=\"%d\" styleClass=\"button-group\">%n",
                SPACING);

        for (String[] button : buttons) {
            writeButton(writer, button[0], button[1], button[2],
                    GAME_BUTTON_CLASS, indentLevel + 1);
        }

        writer.println(indent + "</HBox>");
    }

    private static void writeStylesheets(PrintWriter writer) {
        writer.println("    <stylesheets>");
        writer.println("        <String fx:value=\"styles.css\"/>");
        writer.println("    </stylesheets>");
    }

    private static void generateMainMenuFXML(String filename) throws IOException {
        Path filePath = Paths.get("src", filename);
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<?import javafx.scene.control.Button?>");
            writer.println("<?import javafx.scene.layout.Pane?>");
            writer.println("<?import javafx.scene.text.Font?>");
            writer.println("<?import javafx.scene.text.Text?>");
            writer.println("<?import javafx.scene.control.ComboBox?>");
            writer.println();

            writer.println("<Pane maxHeight=\"-Infinity\" maxWidth=\"-Infinity\" " +
                    "minHeight=\"-Infinity\" minWidth=\"-Infinity\" " +
                    "prefHeight=\"720.0\" prefWidth=\"1280.0\" " +
                    "style=\"-fx-background-color: " + BG_COLOR + ";\" " +
                    "xmlns=\"http://javafx.com/javafx/23\" " +
                    "xmlns:fx=\"http://javafx.com/fxml/1\" " +
                    "fx:controller=\"Controller\">");

            writer.println("   <children>");

            // Language selector
            writer.println("      <ComboBox fx:id=\"languageSelector\" " +
                    "layoutX=\"1100.0\" layoutY=\"20.0\" " +
                    "onAction=\"#handleLanguageChange\" " +
                    "prefWidth=\"150.0\" />");

            // Title
            writer.println("      <Text layoutX=\"467.0\" layoutY=\"136.0\" " +
                    "strokeType=\"OUTSIDE\" strokeWidth=\"0.0\" " +
                    "text=\"%menu.title\" " +
                    "textAlignment=\"CENTER\" " +
                    "wrappingWidth=\"345.13671875\">");
            writer.println("         <font>");
            writer.println("            <Font name=\"Times New Roman Bold Italic\" size=\"61.0\" />");
            writer.println("         </font>");
            writer.println("      </Text>");

            // Difficulty buttons
            writeMainMenuButton(writer, "buttonEasy", 130, 350, "%menu.easy");
            writeMainMenuButton(writer, "buttonMedium", 530, 350, "%menu.medium");
            writeMainMenuButton(writer, "buttonHard", 930, 350, "%menu.hard");

            writer.println("   </children>");
            writer.println("</Pane>");
        }
    }

    private static void writeMainMenuButton(PrintWriter writer, String id,
                                            int layoutX, int layoutY, String textKey) {
        writer.printf("      <Button fx:id=\"%s\" " +
                        "layoutX=\"%d.0\" layoutY=\"%d.0\" " +
                        "mnemonicParsing=\"false\" " +
                        "onAction=\"#open%s\" " +
                        "prefHeight=\"90.0\" prefWidth=\"220.0\" " +
                        "style=\"-fx-background-radius: 20; -fx-background-color: %s;\" " +
                        "text=\"%s\" textAlignment=\"CENTER\">%n",
                id, layoutX, layoutY,
                id.substring(6), // Remove "button" prefix for action name
                BUTTON_COLOR, textKey);
        writer.println("         <font>");
        writer.println("            <Font name=\"Times New Roman Bold Italic\" size=\"22.0\" />");
        writer.println("         </font>");
        writer.println("      </Button>");
    }
}