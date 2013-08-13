package com.strollimo.android.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.strollimo.android.R;
import com.strollimo.android.StrollimoApplication;
import com.strollimo.android.controller.AccomplishableController;
import com.strollimo.android.model.Mystery;
import com.strollimo.android.network.AmazonS3Controller;

public class MysteryOpenActivity extends Activity {
    public static final String PLACE_ID_EXTRA = "place_id";
    private AccomplishableController mAccomplishableController;
    private Mystery mCurrentMystery;

    public static Intent createDetailsIntent(Context context, String mysteryId) {
        Intent intent = new Intent(context, MysteryOpenActivity.class);
        intent.putExtra(PLACE_ID_EXTRA, mysteryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccomplishableController = ((StrollimoApplication) getApplication()).getService(AccomplishableController.class);
        mCurrentMystery = mAccomplishableController.getMysteryById(getIntent().getStringExtra(PLACE_ID_EXTRA));
        setContentView(R.layout.mystery_open_layout);
        findViewById(R.id.open_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DetailsActivity.createDetailsIntent(MysteryOpenActivity.this, mCurrentMystery.getId()));
            }
        });
        ImageView detailsPhoto = (ImageView)findViewById(R.id.detailed_photo);
        String imageUrl = StrollimoApplication.getService(AmazonS3Controller.class).getUrl(mCurrentMystery.getImgUrl());
        Glide.load(imageUrl).centerCrop().animate(android.R.anim.fade_in).placeholder(R.drawable.closed).into(detailsPhoto);
    }
}