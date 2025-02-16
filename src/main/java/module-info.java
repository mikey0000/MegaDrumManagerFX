module info.megadrum.managerfx {
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires org.apache.commons.configuration2;


    opens info.megadrum.managerfx to javafx.fxml;
    exports info.megadrum.managerfx;
}