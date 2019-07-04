package com.bytedance.android.lesson.restapi.solution;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytedance.android.lesson.restapi.solution.bean.Feed;
import com.bytedance.android.lesson.restapi.solution.bean.FeedResponse;
import com.bytedance.android.lesson.restapi.solution.bean.PostVideoResponse;
import com.bytedance.android.lesson.restapi.solution.newtork.IMiniDouyinService;
import com.bytedance.android.lesson.restapi.solution.utils.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Solution2C2Activity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "Solution2C2Activity";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution2_c2);
        initRecyclerView();
        initBtns();
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });

        mBtnRefresh = findViewById(R.id.btn_refresh);
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                ImageView imageView = new ImageView(viewGroup.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                return new Solution2C1Activity.MyViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                ImageView iv = (ImageView) viewHolder.itemView;

                // TODO-C2 (10) Uncomment these 2 lines, assign image url of Feed to this url variable
                String url = mFeeds.get(i).getImage_url();
                Glide.with(iv.getContext()).load(url).into(iv);
            }

            @Override public int getItemCount() {
                return mFeeds.size();
            }
        });
    }

    public void chooseImage() {
        // TODO-C2 (4) Start Activity to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),
                PICK_IMAGE);
    }


    public void chooseVideo() {
        // TODO-C2 (5) Start Activity to select a video
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Viedo"),
                PICK_VIDEO);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(Solution2C2Activity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);



        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            Log.e(TAG, "postVideo: no permission" );
        }
        else{
            Log.e(TAG, "postVideo: permission ok" );
        }
        // TODO-C2 (6) Send Request to post a video with its cover image

        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                MultipartBody.Part coverImage =getMultipartFromUri("cover_image",mSelectedImage);
                MultipartBody.Part Video =getMultipartFromUri("video",mSelectedVideo);
                Retrofit retrofit =new Retrofit.Builder()
                        .baseUrl("http://test.androidcamp.bytedance.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Response<PostVideoResponse> responseResponse= null;
                try {
                    responseResponse =retrofit.create(IMiniDouyinService.class).
                            postVideo("110110","bilibili",coverImage,Video)
                            .execute();

                    if(responseResponse.body()!=null && responseResponse.body().getSuccess()){
                            handler.sendEmptyMessage(2);

                    }
                    else{
                            handler.sendEmptyMessage(3);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();


        /*
        异步写法
        MultipartBody.Part coverImage =getMultipartFromUri("cover_image",mSelectedImage);
        MultipartBody.Part coverVideo =getMultipartFromUri("video",mSelectedVideo);
        Retrofit retrofit =new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Call<PostVideoResponse> call = retrofit.create(IMiniDouyinService.class).postVideo("110110","bilibili",coverImage,coverVideo);
        call.enqueue(new Callback<PostVideoResponse>() {
                         @Override
                         public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                            if(response.isSuccessful()){
                                if( response.body()!=null && response.body().getSuccess()){
                                    Log.d(TAG, "onResponse: sucess");
                                    mBtn.setEnabled(true);
                                }else{
                                    Log.d(TAG, "onResponse: faillll");
                                    mBtn.setEnabled(true);
                                }
                            }
                            else{
                                Log.d(TAG, "onResponse: fail");
                                mBtn.setEnabled(true);
                            }
                         }
                         @Override
                         public void onFailure(Call<PostVideoResponse> call, Throwable t) {

                         }
                     });
    */

                // if success, make a text Toast and show
    }
    private Response<FeedResponse>   response;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                switch (msg.what){
                    case 1:{
                        mRv.getAdapter().notifyDataSetChanged();
                        resetRefreshBtn();
                        break;
                    }
                    case 2:{
                        Toast.makeText(Solution2C2Activity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        mBtn.setText(R.string.select_an_image);
                        mBtn.setEnabled(true);
                        break;
                    }
                    case 3:{
                        Toast.makeText(Solution2C2Activity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        mBtn.setText(R.string.select_an_image);
                        mBtn.setEnabled(true);
                        break;
                    }


                }
        }
    };
    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        // TODO-C2 (9) Send Request to fetch feed
        // if success, assign data to mFeeds and call mRv.getAdapter().notifyDataSetChanged()
        // don't forget to call resetRefreshBtn() after response received
        new Thread(new Runnable() {
            @Override
            public void run() {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://api.icndb.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                try {
                    response = retrofit.create(IMiniDouyinService.class).randomfeed().execute();
                    mFeeds = response.body().getFeeds();
                    handler.sendEmptyMessage(1);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }

    private void resetRefreshBtn() {
        mBtnRefresh.setText(R.string.refresh_feed);
        mBtnRefresh.setEnabled(true);
    }
}
