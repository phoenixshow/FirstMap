package com.phoenix.firstmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class GeoCodeActivity extends AppCompatActivity {
    private ListView listView;
    //城市文本框，关键字文本框
    private EditText editCityEt, editSearchKeyEt;

    private TextureMapView mMapView = null;
//    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearch.destroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
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
}
