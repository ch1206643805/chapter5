package com.bytedance.android.lesson.restapi.solution.bean;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.17 18:08
 */
public class Cat {

    // TODO-C1 (1) Implement your Cat Bean here according to the response json

 //   @SerializedName("breeds")   private JsonArray[]  breeds;
    @SerializedName("height")   private int height;
    @SerializedName("id")   private String id;
    @SerializedName("url")  private String url;
    @SerializedName("width")    private int width;

    public int getHeight(){
        return height;
    }
    public String   getCat(){
        return id;
    }
    public String   getUrl(){
        return url;
    }
    public int getWidth() {
        return width;
    }


}
