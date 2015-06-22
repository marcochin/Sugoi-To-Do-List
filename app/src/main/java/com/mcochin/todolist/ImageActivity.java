package com.mcochin.todolist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Marco on 6/12/2015.
 */
public class ImageActivity extends Activity {
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String TAG = "imageActivity";

    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        final ImageView imageView = (ImageView)findViewById(R.id.imageView);
        String imagePath = getIntent().getStringExtra(KEY_IMAGE_PATH);

        Picasso.with(this)
                .load("file://" + imagePath)
                .fit()
                .centerInside()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
                        if(mAttacher!=null){
                            mAttacher.update();
                        }else{
                            mAttacher = new PhotoViewAttacher(imageView);
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        // Hide the status bar.
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  is to make status bar overlay content
        // View.SYSTEM_UI_FLAG_FULLSCREEN is to hide status bar
        final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        //set listener on the decorView for ui visibility changes;
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            imageView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //hide status bar after 3 seconds, if user pulls it down
                                    decorView.setSystemUiVisibility(uiOptions);
                                }
                            }, 3000);
                        } else {
                            // The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });
    }
}
