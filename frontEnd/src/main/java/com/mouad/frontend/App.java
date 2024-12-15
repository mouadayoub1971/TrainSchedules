package com.mouad.frontend;

import com.mouad.frontend.Controllers.Admin.AdminLogsViewer;
import com.mouad.frontend.Models.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/LogsViewer.fxml"));
//        Parent root = loader.load();
//
//        primaryStage.setTitle("Kafka Log Viewer");
//        primaryStage.setScene(new Scene(root, 1200, 800));
//        primaryStage.show();
//
//        // Ensure Kafka consumer is stopped when application closes
//        primaryStage.setOnCloseRequest(event -> {
//            AdminLogsViewer controller = loader.getController();
//            controller.shutdown();
//        });
        Model.getInstance().getViewsFactory().Reservation();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
