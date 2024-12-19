package com.mouad.frontend.Views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ViewsFactory {
    
    public static FXMLLoader loadFXML(String fxmlPath) throws IOException {
        URL fxmlUrl = ViewsFactory.class.getResource("/Fxml/" + fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("Could not find FXML file: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setClassLoader(ViewsFactory.class.getClassLoader());
        return loader;
    }

    public static void showWindow(String fxmlPath, String title) throws IOException {
        System.out.println("Loading FXML: " + fxmlPath);
        try {
            FXMLLoader loader = loadFXML(fxmlPath);
            Parent root = loader.load();
            
            // Close all existing windows
            for (Stage stage : new ArrayList<>(Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage)
                    .map(window -> (Stage) window)
                    .collect(Collectors.toList()))) {
                stage.close();
            }
            
            // Create and show new window
            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(root);
            URL cssResource = ViewsFactory.class.getResource("/styles/MainPage.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Warning: Could not load CSS file from: /styles/MainPage.css");
            }
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error loading FXML: " + e.getMessage(), e);
        }
    }

    public static Parent loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = loadFXML(fxmlPath);
        return loader.load();
    }

    public static void showMainPage() {
        try {
            showWindow("Admin/MainPage.fxml", "Main Page");
        } catch (IOException e) {
            System.err.println("Error showing main page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showTrainsPage() {
        try {
            showWindow("Admin/TrainsPage.fxml", "Trains Management");
        } catch (IOException e) {
            System.err.println("Error showing trains page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showSchedulesPage() {
        try {
            showWindow("Admin/SchedulesPage.fxml", "Schedules Management");
        } catch (IOException e) {
            System.err.println("Error showing schedules page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showBookingsPage() {
        try {
            showWindow("Admin/BookingsPage.fxml", "Bookings Management");
        } catch (IOException e) {
            System.err.println("Error showing bookings page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showStatisticsPage() {
        try {
            showWindow("Admin/StatisticsPage.fxml", "Statistics");
        } catch (IOException e) {
            System.err.println("Error showing statistics page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showLoginPage() {
        try {
            showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error showing login page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
