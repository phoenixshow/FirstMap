package com.phoenix.firstmap;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class GeoCodeActivity extends BaseActivity {
    private ListView listView;
    //城市文本框，关键字文本框
    private EditText editCityEt, editSearchKeyEt;

    GeoCoder mSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_code);

        editCityEt = (EditText) findViewById(R.id.city_et);
        editSearchKeyEt = (EditText) findViewById(R.id.searchKey_et);

        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
//        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        //创建地理编码检索实例；
        mSearch = GeoCoder.newInstance();
        //设置地理编码检索监听者；
        mSearch.setOnGetGeoCodeResultListener(listener);
    }

    //创建地理编码检索监听者；
    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        //地址转坐标
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
                Toast.makeText(GeoCodeActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            //获取地理编码结果
            Toast.makeText(GeoCodeActivity.this, "位置："+result.getLocation().latitude
                +","+result.getLocation().longitude, Toast.LENGTH_LONG).show();
        }

        //坐标转地址
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有找到检索结果
                Toast.makeText(GeoCodeActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            //获取反向地理编码结果
            Toast.makeText(GeoCodeActivity.this, "位置："+result.getAddress(), Toast.LENGTH_LONG).show();
        }
    };

    public void geoCode(View view) {
        //发起地理编码检索
        mSearch.geocode(new GeoCodeOption()
                .city("北京")
                .address("海淀区上地十街10号"));
    }

    public void reverseGeoCode(View view) {
        LatLng pt = new LatLng(39.963175, 116.400244);
        //发起反地理编码检索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(pt));
    }

    @Override
    protected void onDestroy() {
        mSearch.destroy();
        super.onDestroy();
    }
}
