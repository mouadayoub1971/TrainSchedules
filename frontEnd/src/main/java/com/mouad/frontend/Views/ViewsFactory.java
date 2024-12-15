package com.mouad.frontend.Views;

import com.mouad.frontend.Controllers.Admin.AdminLogsViewer;
import com.mouad.frontend.Controllers.Admin.BookingDashboardController;
import com.mouad.frontend.Controllers.Admin.BookingDashboardControllerv1;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewsFactory {
    public ViewsFactory (){};
    public void showLogsViewer() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/LogsViewer.fxml"));
        Parent root = loader.load();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Kafka Log Viewer");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();

        // Ensure Kafka consumer is stopped when application closes
        primaryStage.setOnCloseRequest(event -> {
            AdminLogsViewer controller = loader.getController();
            controller.shutdown();
        });
    }
    public void showDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/BookingDashboard.fxml"));
        Parent root = loader.load();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("dashboard");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Ensure Kafka consumer is stopped when application closes
        primaryStage.setOnCloseRequest(event -> {
            BookingDashboardControllerv1 controller = loader.getController();
        });
    }
    public void BookingClient() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Booking.fxml"));
        Parent root = loader.load();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Booking");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    public void Reservation() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Reservation.fxml"));
        Parent root = loader.load();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Reservation");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
