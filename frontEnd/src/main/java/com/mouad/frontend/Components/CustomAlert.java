package com.mouad.frontend.Components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Optional;

public class CustomAlert {
    private static Stage createDialogStage(String title, String message, boolean showCancel) {
        // Create UI components
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("alert-message");
        messageLabel.setWrapText(true);
        
        Button confirmButton = new Button("OK");
        confirmButton.getStyleClass().add("alert-confirm-button");
        confirmButton.setDefaultButton(true);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("alert-cancel-button");
        cancelButton.setCancelButton(true);
        cancelButton.setVisible(showCancel);
        cancelButton.setManaged(showCancel);
        
        // Create layout
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(confirmButton);
        if (showCancel) {
            buttonBox.getChildren().add(cancelButton);
        }
        
        VBox root = new VBox(20);
        root.getStyleClass().add("custom-alert");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20, 25, 20, 25));
        root.getChildren().addAll(titleLabel, messageLabel, buttonBox);
        
        // Create scene and stage
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CustomAlert.class.getResource("/styles/CustomAlert.css").toExternalForm());
        
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        
        // Set up result handling
        final ButtonType[] result = {null};
        
        confirmButton.setOnAction(e -> {
            result[0] = ButtonType.OK;
            stage.close();
        });
        
        cancelButton.setOnAction(e -> {
            result[0] = ButtonType.CANCEL;
            stage.close();
        });
        
        // Store result in stage properties
        stage.getProperties().put("result", result);
        
        return stage;
    }
    
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Stage stage = createDialogStage(title, message, true);
        stage.showAndWait();
        ButtonType[] result = (ButtonType[]) stage.getProperties().get("result");
        return Optional.ofNullable(result[0]);
    }
    
    public static void showInformation(String title, String message) {
        Stage stage = createDialogStage(title, message, false);
        stage.showAndWait();
    }

    public static void showError(String title, String message) {
        Stage stage = createDialogStage(title, message, false);
        stage.showAndWait();
    }

    public static void showSuccess(String title, String message) {
        Stage stage = createDialogStage(title, message, false);
        stage.showAndWait();
    }

    public static void showInfo(String title, String message) {
        showInformation(title, message);
    }
}
