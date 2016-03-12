package com.kevin.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

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
        if (messageEvent.getPath().equalsIgnoreCase("/REPS")) {

            // Value contains the String we sent over in WatchToPhoneService, "good job"
            String rep = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            System.out.println("RECEIVE SHIT FROM WATCH" + rep);
            Intent intent = new Intent(getApplicationContext(), Detail.class);
            String bioId = "", twitterId = "", name = "";
            JSONObject repObj;
            try {
                repObj = new JSONObject(rep);
                bioId = repObj.getString("bioguide_id");
                name = repObj.getString("first_name") + " " + repObj.getString("last_name") + " " + repObj.getString("party");
                twitterId = repObj.getString("twitter_id");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtra("BIO_ID", bioId);
            intent.putExtra("TWITTER_ID", twitterId);
            //intent.putExtra("PIC_URL", url);
            intent.putExtra("NAME", name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {
            super.onMessageReceived(messageEvent);
        }

    }
}
