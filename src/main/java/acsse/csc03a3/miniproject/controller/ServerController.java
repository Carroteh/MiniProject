package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.net.server.Server;
import javafx.scene.control.TextArea;

public class ServerController {
    @javafx.fxml.FXML
    private TextArea txtServerLog;

    @javafx.fxml.FXML
    public void initialize() {
        Server.start(3301, txtServerLog);
    }

}
