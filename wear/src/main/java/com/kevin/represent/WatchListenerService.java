package com.kevin.represent;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, fred vs lexy)

        if (messageEvent.getPath().equalsIgnoreCase("/ZIP")) {
            String zip = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            Toast toast = Toast.makeText(getApplicationContext(), "New ZIP: " + zip, Toast.LENGTH_LONG);
            toast.show();

            intent.putExtra("ZIP", zip);
            startActivity(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase("/COORD")) {
            System.out.println("COORD RECEIVED");
            String coord = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast toast = Toast.makeText(getApplicationContext(), "New COORD: " + coord, Toast.LENGTH_LONG);
            toast.show();
            String[] latlng = coord.split(",");
            intent.putExtra("LAT", Double.parseDouble(latlng[0]));
            intent.putExtra("LON", Double.parseDouble(latlng[1]));
            startActivity(intent);
        }
    }
}