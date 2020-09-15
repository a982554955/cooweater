package com.example.xiugeweather.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.xiugeweather.R;
import com.example.xiugeweather.gson.Forecast;
import com.example.xiugeweather.gson.Weather;
import com.example.xiugeweather.util.HttpUtil;
import com.example.xiugeweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mCityTitle;
    private TextView mUpdateTimeTitle;
    private TextView mTextDegree;
    private TextView mInfoTextWeather;
    private LinearLayout mLayoutForecast;
    private TextView mTextAqi;
    private TextView mTextPm25;
    private TextView mTextComfort;
    private TextView mWashTextCar;
    private TextView mTextSport;
    private ScrollView mLayoutWeather;
    private ImageView mPicImgBing;
    public SwipeRefreshLayout mRefreshSwipe;
    private Button mButtonNav;

    public DrawerLayout mLayoutDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
    }

    private void initView() {
        mCityTitle = (TextView) findViewById(R.id.title_city);
        mUpdateTimeTitle = (TextView) findViewById(R.id.title_update_time);
        mTextDegree = (TextView) findViewById(R.id.degree_text);
        mInfoTextWeather = (TextView) findViewById(R.id.weather_info_text);
        mLayoutForecast = (LinearLayout) findViewById(R.id.forecast_layout);
        mTextAqi = (TextView) findViewById(R.id.aqi_text);
        mTextPm25 = (TextView) findViewById(R.id.pm25_text);
        mTextComfort = (TextView) findViewById(R.id.comfort_text);
        mWashTextCar = (TextView) findViewById(R.id.car_wash_text);
        mTextSport = (TextView) findViewById(R.id.sport_text);
        mLayoutWeather = (ScrollView) findViewById(R.id.weather_layout);
        mPicImgBing = (ImageView) findViewById(R.id.bing_pic_img);
        mRefreshSwipe = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mButtonNav = (Button) findViewById(R.id.nav_button);
        mButtonNav.setOnClickListener(this);
        mLayoutDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRefreshSwipe.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            mLayoutWeather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        mRefreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(mPicImgBing);
        } else {
            loadBingpic();
        }


    }

    /*
     * 加载必应每日一图
     * */
    private void loadBingpic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("bing_pic", bingPic);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mPicImgBing);
                    }
                });
            }
        });
    }

    /*
     * 根据天气id 请求城市天气信息
     * */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mRefreshSwipe.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mRefreshSwipe.setRefreshing(false);
                    }
                });
            }
        });
        loadBingpic();
    }

    /*
     * 处理并展示Weather实体类中的数据
     * */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mCityTitle.setText(cityName);
        mUpdateTimeTitle.setText(updateTime);
        mTextDegree.setText(degree);
        mInfoTextWeather.setText(weatherInfo);
        mLayoutForecast.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mLayoutForecast, false);
            TextView deteText = view.findViewById(R.id.data_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            deteText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            mLayoutForecast.addView(view);
        }
        if (weather.aqi != null) {
            mTextAqi.setText(weather.aqi.city.aqi);
            mTextPm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        mTextComfort.setText(comfort);
        mWashTextCar.setText(carWash);
        mTextSport.setText(sport);
        mLayoutWeather.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_button:
                // TODO 20/09/15
                mLayoutDrawer.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
    }
}
