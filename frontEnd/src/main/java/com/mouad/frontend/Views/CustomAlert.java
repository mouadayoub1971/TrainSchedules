package com.mouad.frontend.Views;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class CustomAlert {
    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        Scene scene = dialogPane.getScene();
        Stage stage = (Stage) scene.getWindow();
        
        // Load custom CSS
        dialogPane.getStylesheets().add(
            CustomAlert.class.getResource("/styles/CustomAlert.css").toExternalForm()
        );
        
        // Apply CSS classes
        dialogPane.getStyleClass().add("custom-alert");
        dialogPane.lookup(".content.label").getStyleClass().add("alert-message");
        dialogPane.lookup(".header-panel").getStyleClass().add("alert-title");
        
        // Style buttons
        dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("alert-confirm-button");
        if (alert.getAlertType() == Alert.AlertType.CONFIRMATION) {
            dialogPane.lookupButton(ButtonType.CANCEL).getStyleClass().add("alert-cancel-button");
        }
    }

    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        styleAlert(alert);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
