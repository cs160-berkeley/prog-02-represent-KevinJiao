package com.kevin.represent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.internal.IMapFragmentDelegate;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;

public class Congressional extends AppCompatActivity {
    ArrayList<JSONObject> reps = new ArrayList<>();
    ArrayAdapter<JSONObject> adapter;
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "geDYiMZy7u8J0GW7UiVzdJP79";
    private static final String TWITTER_SECRET = "l0t4MO5tUGviFDLnBeMqjdNJg92rR15uPYLFhUB9G57dh234dk";
    AppSession guestSession;
    TwitterApiClient twitterApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);
        ViewGroup mRelativeLayout = (ViewGroup) findViewById(R.id.rep_layout);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://congress.api.sunlightfoundation.com/legislators/locate", this.getParams(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                JSONObject res;
                try {
                    res = new JSONObject(new String(response));
                    JSONArray repObjects = res.getJSONArray("results");
                    for (int i = 0; i < res.getInt("count"); i++) {
                        JSONObject repObj = repObjects.getJSONObject(i);
                        reps.add(repObj);
                    }
                    System.out.println(reps);
                    adapter.notifyDataSetChanged();
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("SUNLIGHT API FAILED");
                System.out.println(new String(responseBody));
            }
        });
        adapter = new repAdapter(this, R.layout.rep_card, reps);
        ListView repList = (ListView) findViewById(R.id.rep_list);
        repList.setAdapter(adapter);
    }

    public void getDetails(View view, String bioId) {
        Intent intent = new Intent(this, Detail.class);
        intent.putExtra("BIO_ID", bioId);
        startActivity(intent);
    }

    private class repAdapter extends ArrayAdapter<JSONObject> {
        Context context;
        List<JSONObject> objects;

        public repAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            final JSONObject rep = objects.get(pos);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View repView = inflater.inflate(R.layout.rep_card, parent, false);
            TextView nameView = (TextView) repView.findViewById(R.id.name);
            final TextView desc = (TextView) repView.findViewById(R.id.desc);
            final ImageView profileImage = (ImageView) repView.findViewById(R.id.pic);
            final ImageView twitter = (ImageView) repView.findViewById(R.id.twitter);
            final ImageView info = (ImageView) repView.findViewById(R.id.info);
            ImageView home = (ImageView) repView.findViewById(R.id.home);
            ImageView emailView = (ImageView) repView.findViewById(R.id.email);
            TextView titleView = (TextView) repView.findViewById(R.id.title);
            String title, website, email;
            final String bioId, name;
            try {
                name = rep.getString("first_name") + " " + rep.getString("last_name") + " " + rep.getString("party");
                website = rep.getString("website");
                email = rep.getString("oc_email");
                title = rep.getString("title");
                bioId = rep.getString("bioguide_id");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            nameView.setText(name);
            desc.setText(website);
            setListener(home, desc, website);
            setListener(emailView, desc, email);
            if (title.equals("Sen")) {
                titleView.setText("Senator");
            } else {
                titleView.setText("Representative");
            }
            if (guestSession == null) {
                TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
                    @Override
                    public void success(Result<AppSession> result) {
                        guestSession = result.data;
                        twitterApiClient = TwitterCore.getInstance().getApiClient(guestSession);
                        StatusesService statusesService = twitterApiClient.getStatusesService();
                        String twitterId = "";
                        try {
                            twitterId = rep.getString("twitter_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Getting tweets for " + twitterId);
                        statusesService.userTimeline(null, twitterId, 1, null, null, false, true, false, false, new Callback<List<Tweet>>() {
                            @Override
                            public void success(Result<List<Tweet>> result) {
                                List<Tweet> tweets = result.data;
                                for (Tweet t : tweets) {
                                    final String url = t.user.profileImageUrl.replace("_normal", "");
                                    System.out.println(url);
                                    setListener(twitter, desc, Html.fromHtml(t.text).toString());
                                    Picasso.with(getContext()).load(url).into(profileImage);
                                    info.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getContext(), Detail.class);
                                            intent.putExtra("BIO_ID", bioId);
                                            intent.putExtra("PIC_URL", url);
                                            intent.putExtra("NAME", name);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void failure(TwitterException e) {
                            }
                        });
                    }

                    @Override
                    public void failure(TwitterException e) {
                    }
                });
            }
            return repView;
        }

        public void setListener(View v, final TextView desc, final String msg) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    desc.setText(msg);
                }
            });
        }
    }

    private RequestParams getParams() {
        RequestParams params = new RequestParams();
        Intent intent = getIntent();
        String zip;
        double lat, lon;
        params.put("apikey", "a347f6559ad649bf940fae34119fe3c4");
        if (intent.hasExtra("ZIP")) {
            zip = intent.getStringExtra("ZIP");
            params.put("zip", zip);
            System.out.println("Using ZIP: " + zip);
        } else if (intent.hasExtra("LAT")) {
            lat = intent.getDoubleExtra("LAT", 0);
            lon = intent.getDoubleExtra("LON", 0);
            params.put("latitude", lat);
            params.put("longitude", lon);
            System.out.println("Using lat + long " + lat + ", " + lon);
        } else {
            System.out.println("NO LOCATION DATA IN INTENT");
        }
        return params;
    }
}