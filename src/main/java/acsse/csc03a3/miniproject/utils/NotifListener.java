package acsse.csc03a3.miniproject.utils;

import javafx.scene.control.TextArea;
import webphone.SIPNotification;
import webphone.SIPNotificationListener;

public class NotifListener extends SIPNotificationListener {

    private TextArea txtLog;

    public NotifListener(TextArea txtLog) {
        this.txtLog = txtLog;
    }

    @Override
    public void onAll(SIPNotification n) {
        txtLog.appendText("Notification received: " + n.toString() + "\n");
    }
}
