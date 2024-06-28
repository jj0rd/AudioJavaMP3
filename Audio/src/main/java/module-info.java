module com.example.audio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.audio to javafx.fxml;
    exports com.example.audio;
}