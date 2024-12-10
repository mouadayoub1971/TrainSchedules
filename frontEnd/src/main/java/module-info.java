module com.mouad.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.mouad.frontend to javafx.fxml;
    opens com.mouad.frontend.Controllers to javafx.fxml;
    exports com.mouad.frontend;
    exports com.mouad.frontend.Controllers;
    exports com.mouad.frontend.Controllers.Admin;
    exports com.mouad.frontend.Controllers.Client;
    exports com.mouad.frontend.Views;
    exports com.mouad.frontend.Models;
}