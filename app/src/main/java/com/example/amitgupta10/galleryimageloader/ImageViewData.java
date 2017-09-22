package com.example.amitgupta10.galleryimageloader;

import android.graphics.Bitmap;
import com.example.amitgupta10.galleryimageloader.Interfaces.GalleryInterface;
import java.lang.ref.WeakReference;

/**
 * Created by amit.gupta10 .
 */

public class ImageViewData {
    private String url;
    private int pos;
    private WeakReference<GalleryInterface.bitmapRetrivedListener> dataRetrivedListener;


    public String getUrl() {
        return url;
    }

    public ImageViewData(String url, int pos) {
        this.url = url;
        this.pos = pos;
    }

    public void setDataRetrivedListener(GalleryInterface.bitmapRetrivedListener dataRetrivedListener){
        this.dataRetrivedListener = new WeakReference<GalleryInterface.bitmapRetrivedListener>(dataRetrivedListener);
    }

    public void setBitmapResult(Bitmap bitmap){
        if(dataRetrivedListener !=null && dataRetrivedListener.get()!=null){
            dataRetrivedListener.get().onBitmapRetreived(bitmap, url, pos);
        }
    }
}