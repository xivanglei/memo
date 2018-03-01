package com.example.mymemo.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.mymemo.R;

public class Picture extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Intent intent = getIntent();
        String path = intent.getStringExtra("picture");
        ImageView picture = (ImageView) findViewById(R.id.picture);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        picture.setImageBitmap(bitmap);
    }

    public static void actionStart(Context context, String path) {
        Intent intent = new Intent(context, Picture.class);
        intent.putExtra("picture", path);
        context.startActivity(intent);
    }
}
