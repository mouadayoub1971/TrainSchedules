module com.mouad.frontend {
    requires javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    requires mysql.connector.j;
    requires kafka.clients;
    requires org.json;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    opens com.mouad.frontend.Controllers.Admin to javafx.fxml;
    opens com.mouad.frontend.Controllers.Client to javafx.fxml; 
    opens com.mouad.frontend to javafx.fxml, javafx.graphics;

    opens com.mouad.frontend.Controllers to javafx.fxml;
    opens com.mouad.frontend.Models to javafx.base;
    opens com.mouad.frontend.Services to javafx.base;
    opens com.mouad.frontend.Views to javafx.fxml;

    exports com.mouad.frontend;
    exports com.mouad.frontend.Controllers;
    exports com.mouad.frontend.Controllers.Admin;
    exports com.mouad.frontend.Models;
    exports com.mouad.frontend.Services;
}