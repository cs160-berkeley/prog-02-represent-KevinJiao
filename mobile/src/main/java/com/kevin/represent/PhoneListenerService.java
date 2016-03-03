package com.kevin.represent;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class PhoneListenerService extends WearableListenerService {

    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String VIEW_REP = "/view_rep";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        System.out.println("message received");
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());
        if (messageEvent.getPath().equalsIgnoreCase(VIEW_REP)) {

            // Value contains the String we sent over in WatchToPhoneService, "good job"
            String rep = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            Intent intent = new Intent(getApplicationContext(), Detail.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {
            super.onMessageReceived(messageEvent);
        }

    }
}
