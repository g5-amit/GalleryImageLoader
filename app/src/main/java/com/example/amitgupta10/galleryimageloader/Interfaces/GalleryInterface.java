package com.example.amitgupta10.galleryimageloader.Interfaces;

import android.graphics.Bitmap;

/**
 * Created by amit.gupta10 .
 */

public class GalleryInterface {

    public interface bitmapRetrivedListener{
        public void onBitmapRetreived(Bitmap bitmap, String url, int pos);
        public void onBitmapError();
    }
}
