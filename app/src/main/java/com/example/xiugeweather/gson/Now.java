package com.example.xiugeweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @pengjiaxing
 * @日期
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
