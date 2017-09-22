package com.example.amitgupta10.galleryimageloader;

import android.app.Application;
import android.content.Context;

/**
 * Created by amit.gupta10 .
 */

public class GalleryApplication extends Application {

    private static GalleryApplication instance;

    public GalleryApplication(){
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }


    public static GalleryApplication getInstance() {
        return instance;
    }
}
