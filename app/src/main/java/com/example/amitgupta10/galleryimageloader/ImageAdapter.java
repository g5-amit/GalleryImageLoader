package com.example.amitgupta10.galleryimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.amitgupta10.galleryimageloader.Interfaces.GalleryInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by amit.gupta10 .
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> implements GalleryInterface.bitmapRetrivedListener {

    private ArrayList<String> mImagesList;
    private Context mContext;
    private SparseBooleanArray mSparseBooleanArray;
    private SparseArray<MyViewHolder> mPostionHolderMap;
    private HashMap<MyViewHolder, String> mHolderUrlMap;
    private SparseArray<ImageViewData> mImageViewData;
    private FileCache fileCache;
    private MemoryCache memoryCache;
    private ExecutorService executorService;

    public ImageAdapter(Context context, ArrayList<String> imageList) {
        mContext = context;
        this.mImagesList = imageList;

        mSparseBooleanArray = new SparseBooleanArray();
        mPostionHolderMap = new SparseArray<MyViewHolder>();
        mHolderUrlMap = new HashMap<MyViewHolder, String>();
        mImageViewData = new SparseArray<>();

        fileCache = new FileCache(GalleryApplication.getContext());
        memoryCache = new MemoryCache();

        executorService = Executors.newFixedThreadPool(4);
    }

    public ArrayList<String> getCheckedItems() {
        ArrayList<String> mTempArry = new ArrayList<String>();

        for(int i=0;i<mImagesList.size();i++) {
            if(mSparseBooleanArray.get(i)) {
                mTempArry.add(mImagesList.get(i));
            }
        }

        return mTempArry;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
        }
    };

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_multiphoto_item, parent, false);

        MyViewHolder holder =  new MyViewHolder(itemView);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(parent.getWidth(), parent.getHeight()/2);
        holder.imageView.setLayoutParams(lp);
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String imageUrl = mImagesList.get(position);

        mPostionHolderMap.put(position, holder);
        mHolderUrlMap.put(holder, imageUrl);

        Bitmap bitmap = memoryCache.get(imageUrl);
        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
        Log.d("amit", " after start imageUrl =" + imageUrl + " pos ="+ position);
        if(bitmap == null){
            ViewGroup.LayoutParams lp = holder.imageView.getLayoutParams();
            int req_width = lp.width;
            int req_height = lp.height;
            ImageViewData imageViewData;
            if(mImageViewData.get(position) == null){
                imageViewData = new ImageViewData(imageUrl, position);
            }else{
                imageViewData = mImageViewData.get(position);
            }
            imageViewData.setDataRetrivedListener(this);
            executorService.submit(new BitmapThread(imageViewData, req_width, req_height));

        }else {
            setFadeEffect(holder.imageView, bitmap);

//        Glide.with(mContext)
//                .load("file://"+imageUrl)
//                .centerCrop()
//                .placeholder(R.drawable.ic_launcher)
//                .error(R.drawable.ic_launcher)
//                .into(holder.imageView);

            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(mSparseBooleanArray.get(position));
            holder.checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
        }
    }

    private void setFadeEffect(ImageView imageView, Bitmap bitmap) {
//        Drawable currentDrawable = imageView.getDrawable();
//        Drawable[] arrayDrawable = new Drawable[2];
//        arrayDrawable[0] = currentDrawable;
//        arrayDrawable[1] = new BitmapDrawable(mContext.getResources(), bitmap);
//        //arrayDrawable[1] = drawable;
//        TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
//        transitionDrawable.setCrossFadeEnabled(true);
//        imageView.setImageDrawable(transitionDrawable);
//        Log.d("amit", "viewwidth= " + imageView.getWidth() + "viewheight= "+ imageView.getHeight());
//        transitionDrawable.startTransition(1000);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return mImagesList.size();
    }

    @Override
    public void onBitmapRetreived(Bitmap bitmap, String imageUrl, int position) {
        if(bitmap == null) {
            Log.d("amit", "null bitmap imageurl = "+imageUrl+" pos = "+position);
            return;
        }
        memoryCache.put(imageUrl, bitmap);

        if(mPostionHolderMap != null && mPostionHolderMap.get(position)!=null){
            MyViewHolder holder = mPostionHolderMap.get(position);
            String curHolderUrl = mHolderUrlMap.get(holder);
            //is Holder Reused while scrolling
            if(TextUtils.isEmpty(curHolderUrl) || !curHolderUrl.equals(imageUrl)){
                Log.d("amit", "reused imageurl = "+imageUrl+" pos = "+position);
                return;
            }
            Log.d("amit", "after result imageUrl =" + imageUrl + " pos ="+ position);

            setFadeEffect(holder.imageView, bitmap);

            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(mSparseBooleanArray.get(position));
            holder.checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
        }

    }

    @Override
    public void onBitmapError() {

    }

    private static class BitmapThread implements Runnable{

        private ImageViewData imageViewData;
        private int reqWidth;
        private int reqHeight;

        public BitmapThread(ImageViewData imageViewData, int reqWidth, int reqHeight) {
            this.imageViewData = imageViewData;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
        }

        @Override
        public void run() {
            Bitmap bitmap = getBitmap(imageViewData.getUrl(), reqWidth, reqHeight);
            imageViewData.setBitmapResult(bitmap);
        }

        private Bitmap getBitmap(String url, int reqWidth, int reqHeight)
        {
//        File f=fileCache.getFile(url);
            File f = new File(url);

            //from SD cache
            Bitmap b = decodeFile(f, reqWidth, reqHeight);
            Log.d("amit", "finalwidth= " + b.getWidth() + "finalheight= "+ b.getHeight());

            if(b!=null)
                return b;

            //from web
            try {
//            Bitmap bitmap=null;
//            URL imageUrl = new URL(url);
//            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
//            conn.setConnectTimeout(30000);
//            conn.setReadTimeout(30000);
//            conn.setInstanceFollowRedirects(true);
//            InputStream is=conn.getInputStream();
//            OutputStream os = new FileOutputStream(f);
//            Utils.CopyStream(is, os);
//            os.close();
//            bitmap = decodeFile(f);
//            return bitmap;
            } catch (Throwable ex){
                ex.printStackTrace();
//            if(ex instanceof OutOfMemoryError)
//                memoryCache.clear();
                return null;
            }
            return null;
        }
        //decodes image and scales it to reduce memory consumption
        private Bitmap decodeFile(File f, int reqWidth, int reqHeight){
            try {
                //decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f),null,o);

                //Find the correct scale value. It should be the power of 2.
//            final int REQUIRED_SIZE= 100;
                int width_tmp=o.outWidth, height_tmp=o.outHeight;
                int scale=1;
                Log.d("amit", "oriwidth= " + width_tmp + "oriheight= "+ height_tmp);
                Log.d("amit", "width= " + reqWidth + "height= "+ reqHeight);
                if(reqHeight > 0 && reqWidth > 0) {
                    while (true) {
                        if (width_tmp / 2 > reqWidth || height_tmp / 2 > reqHeight) {
                            width_tmp /= 2;
                            height_tmp /= 2;
                            scale *= 2;
                        }
                        break;
                    }
                }

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize=scale;
                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            } catch (FileNotFoundException e) {}
            return null;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CheckBox checkBox;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);

            checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
            imageView = (ImageView) view.findViewById(R.id.imageView1);
        }
    }
}