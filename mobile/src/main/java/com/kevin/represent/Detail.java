package com.kevin.represent;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class Detail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        String bioId = intent.getStringExtra("BIO_ID");
        String picURL = intent.getStringExtra("PIC_URL");
        String name = intent.getStringExtra("NAME");
        ImageView pic = (ImageView) findViewById(R.id.detail_pic);
        TextView nameView = (TextView) findViewById(R.id.detail_name);
        nameView.setText(name);
        Picasso.with(this).load(picURL).into(pic);
    }


}
