package com.kevin.represent;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class Congressional extends AppCompatActivity {
    private ViewGroup mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);
        Intent intent = getIntent();
        String zip = intent.getStringExtra("ZIP");
        mRelativeLayout = (ViewGroup) findViewById(R.id.rep_layout);

    }

    public void getDetails(View view) {
        Intent intent = new Intent(this, Detail.class);
        startActivity(intent);
    }
}