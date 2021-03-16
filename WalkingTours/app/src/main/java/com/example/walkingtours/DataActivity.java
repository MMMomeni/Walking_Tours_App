package com.example.walkingtours;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

public class DataActivity extends AppCompatActivity {

    private Typeface myCustomFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        ConstraintLayout layout = findViewById(R.id.layout);
        TextView nameText = findViewById(R.id.nameBox);
        TextView addressText = findViewById(R.id.addressBox);
        TextView descriptionText = findViewById(R.id.descriptionBox);
        ImageView image = findViewById(R.id.imageBox);

        myCustomFont = Typeface.createFromAsset(getAssets(), "Acme-Regular.ttf");

        descriptionText.setMovementMethod(new ScrollingMovementMethod());


        FenceData fd = (FenceData) getIntent().getSerializableExtra("DATA");

        if (fd != null) {
            //layout.setBackgroundColor(Color.parseColor(fd.getFenceColor()));
            nameText.setText(fd.getId());
            addressText.setText(fd.getAddress());
            descriptionText.setText(fd.getDescription());

            if (!fd.getImage().isEmpty()) {
                Picasso.get().load(fd.getImage())
                        .error(R.drawable.noimage)
                        .placeholder(R.drawable.loading)
                        .into(image);
            }
        }
        nameText.setTypeface(myCustomFont);
        addressText.setTypeface(myCustomFont);
        descriptionText.setTypeface(myCustomFont);
        customizeActionBar();
    }

    private void customizeActionBar() {

        // This function sets the font of the title in the app bar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String t = getTitle().toString();
        TextView tv = new TextView(this);
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.home_image);

        //tv.setText(t);
        //tv.setTextSize(24);
       // tv.setTextColor(Color.WHITE);
        //tv.setTypeface(myCustomFont, Typeface.NORMAL);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(image);
    }
}