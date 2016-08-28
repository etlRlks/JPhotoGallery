package com.jogue.photogallery.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.jogue.photogallery.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jogue- on 2016/8/23.
 */
public class DetailPhotoActivity extends AppCompatActivity {
    private ImageView mImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_photo);
        mImageView = (ImageView) findViewById(R.id.show_detail_photo_image_view);
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        OkHttpClient client = new OkHttpClient();

        Intent i = getIntent();
        Bundle bundle = i.getBundleExtra("bundle");
        String photoUrl = bundle.getString("PHOTO");
        Log.i("Photo",photoUrl);
        try {
            Request request = new Request.Builder()
                    .url(photoUrl)
                    .build();
            Response response = client.newCall(request).execute();
            InputStream is = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Picasso.with(DetailPhotoActivity.this)
                .load(photoUrl)
                .into(mImageView);
    }
}
