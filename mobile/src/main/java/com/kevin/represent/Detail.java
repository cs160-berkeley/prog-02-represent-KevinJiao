package com.kevin.represent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ms.square.android.expandabletextview.ExpandableTextView;
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

public class Detail extends AppCompatActivity {
    String bioId;
    ExpandableTextView committeeText;
    ArrayAdapter<String> adapter;
    ListView billList;
    private static final String TWITTER_KEY = "geDYiMZy7u8J0GW7UiVzdJP79";
    private static final String TWITTER_SECRET = "l0t4MO5tUGviFDLnBeMqjdNJg92rR15uPYLFhUB9G57dh234dk";
    AppSession guestSession;
    TwitterApiClient twitterApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        bioId = intent.getStringExtra("BIO_ID");
        String picURL = intent.getStringExtra("PIC_URL");
        String name = intent.getStringExtra("NAME");
        final String twitterId = intent.getStringExtra("TWITTER_ID");
        final ImageView pic = (ImageView) findViewById(R.id.detail_pic);
        TextView nameView = (TextView) findViewById(R.id.detail_name);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        if (picURL == null) {
            if (guestSession == null) {
                TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
                    @Override
                    public void success(Result<AppSession> result) {
                        guestSession = result.data;
                        twitterApiClient = TwitterCore.getInstance().getApiClient(guestSession);
                        StatusesService statusesService = twitterApiClient.getStatusesService();
                        statusesService.userTimeline(null, twitterId, 1, null, null, false, true, false, false, new Callback<List<Tweet>>() {
                            @Override
                            public void success(Result<List<Tweet>> result) {
                                List<Tweet> tweets = result.data;
                                for (Tweet t : tweets) {
                                    final String url = t.user.profileImageUrl.replace("_normal", "");
                                    System.out.println(url);
                                    Picasso.with(getBaseContext()).load(url).into(pic);

                                }
                            }

                            @Override
                            public void failure(TwitterException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                });

            }
        } else {
            Picasso.with(this).load(picURL).into(pic);
        }
        billList = (ListView) findViewById(R.id.bill_list);
        nameView.setText(name);
        committeeText = (ExpandableTextView) findViewById(R.id.expand_text_view);
        setCommitteeString();
        setVoteList();
    }


    public void setCommitteeString() {
        RequestParams params = new RequestParams();
        params.put("member_ids", bioId);
        params.put("apikey", "a347f6559ad649bf940fae34119fe3c4");
        new AsyncHttpClient().get("http://congress.api.sunlightfoundation.com/committees", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject res;
                String committees = "Committees: ";
                try {
                    res = new JSONObject(new String(responseBody));
                    JSONArray committeeObjs = res.getJSONArray("results");
                    for (int i = 0; i < res.getInt("count"); i++) {
                        committees += committeeObjs.getJSONObject(i).getString("name") + " ";
                    }
                    committeeText.setText(committees);
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("GETTING COMITTEEES FAILED");
                System.out.println(new String(responseBody));
            }
        });
    }

    public void setVoteList() {
        RequestParams params = new RequestParams();
        params.put("voter_ids." + bioId + "__exists", true);
        params.put("apikey", "a347f6559ad649bf940fae34119fe3c4");
        System.out.println(params.toString());
        new AsyncHttpClient().get("http://congress.api.sunlightfoundation.com/votes", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject res;
                ArrayList<String> billTitles = new ArrayList<String>();
                try {
                    res = new JSONObject(new String(responseBody));
                    JSONArray bills = res.getJSONArray("results");
                    for (int i = 0; i < 10; i++) {
                        JSONObject bill = bills.getJSONObject(i);
                        billTitles.add(bill.getString("question"));
                    }
                    adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, billTitles);
                    billList.setAdapter(adapter);
                    setListViewHeightBasedOnItems(billList);
                    System.out.println(billTitles);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(new String(responseBody));
            }
        });
    }

    private class BillAdapter extends ArrayAdapter<String> {
        Context context;
        List<String> objects;

        public BillAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 30;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }
}
