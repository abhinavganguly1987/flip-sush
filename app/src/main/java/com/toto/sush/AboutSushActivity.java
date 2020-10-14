package com.toto.sush;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class AboutSushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_sush);
        //creating the toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.aboutSushToolbar);
        myToolbar.setSubtitle("version 2");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView aboutSushTextPara = findViewById(R.id.textView3);
        aboutSushTextPara.setText(Html.fromHtml(getString(R.string.about_paragraph)));
        aboutSushTextPara.setMovementMethod(new ScrollingMovementMethod());


    }
}