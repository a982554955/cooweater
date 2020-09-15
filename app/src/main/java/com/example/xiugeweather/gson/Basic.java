package com.example.xiugeweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @pengjiaxing
 * @日期
 */
public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
