package com.example.xiugeweather.db;

import org.litepal.crud.DataSupport;

/**
 * @pengjiaxing
 * @日期
 */
public class City extends DataSupport {
    private int id;
    private String CityName;
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
