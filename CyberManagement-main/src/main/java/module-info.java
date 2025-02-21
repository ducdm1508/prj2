module com.example.demo {
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires javafx.controls;

    opens com.cyber.server to javafx.fxml;
    opens com.cyber.server.model to javafx.base;

    exports com.cyber.server;
    exports com.cyber.server.model;
    exports com.cyber.server.controller.account;
    opens com.cyber.server.controller.account to javafx.fxml;
    exports com.cyber.server.controller.computer;
    opens com.cyber.server.controller.computer to javafx.fxml;
    exports com.cyber.server.controller.dashboard;
    opens com.cyber.server.controller.dashboard to javafx.fxml;
    exports com.cyber.server.controller.food;
    opens com.cyber.server.controller.food to javafx.fxml;
    exports com.cyber.server.controller.layout;
    opens com.cyber.server.controller.layout to javafx.fxml;
    exports com.cyber.server.controller.report;
    opens com.cyber.server.controller.report to javafx.fxml;
    exports com.cyber.server.controller.server;
    opens com.cyber.server.controller.server to javafx.fxml;
    exports com.cyber.server.controller.room;
    opens com.cyber.server.controller.room to javafx.fxml;
}
