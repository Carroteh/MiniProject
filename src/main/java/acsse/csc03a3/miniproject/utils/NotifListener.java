package acsse.csc03a3.miniproject.utils;

import webphone.SIPNotification;
import webphone.SIPNotificationListener;

public class NotifListener extends SIPNotificationListener {
    @Override
    public void onAll(SIPNotification n) {
        System.out.println("Notification received: " + n.toString());
    }
}
