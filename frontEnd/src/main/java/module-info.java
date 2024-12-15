module com.mouad.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    requires mysql.connector.j;
    requires kafka.clients;
    requires org.json;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.mouad.frontend to javafx.fxml;
    opens com.mouad.frontend.Controllers.Admin to javafx.fxml;
    opens com.mouad.frontend.Controllers.Client to javafx.fxml;
    exports com.mouad.frontend;
    exports com.mouad.frontend.Controllers;
    exports com.mouad.frontend.Controllers.Admin;
    exports com.mouad.frontend.Controllers.Client;
    exports com.mouad.frontend.Views;
    exports com.mouad.frontend.Models;
}