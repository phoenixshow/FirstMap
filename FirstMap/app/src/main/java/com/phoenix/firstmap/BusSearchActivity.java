package com.phoenix.firstmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import static com.phoenix.firstmap.R.id.searchKey_et;

public class BusSearchActivity extends AppCompatActivity {
    private ListView listView;
    //城市文本框，关键字文本框
    private EditText editCityEt, editSearchKeyEt;

    private TextureMapView mMapView = null;
//    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    PoiSearch mPoiSearch = null;
    private String busLineId;
    private BusLineSearch mBusLineSearch ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search);

        editCityEt = (EditText) findViewById(R.id.city_et);
        editSearchKeyEt = (EditText) findViewById(R.id.searchKey_et);

        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
//        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        //创建POI检索实例
        mPoiSearch = PoiSearch.newInstance();
        //设置POI检索监听者
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        // 创建Busline检索实例
        mBusLineSearch = BusLineSearch.newInstance();
        // 设置创建Busline检索实例检索监听者
        mBusLineSearch.setOnGetBusLineSearchResultListener(busLineListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPoiSearch.destroy();
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


    //公交信息检索
    public void busSearch(View view) {
        //设置检索参数，发起检索请求
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(editCityEt.getText().toString().trim())// 城市
                .keyword(editSearchKeyEt.getText().toString().trim()));// 关键字
    }

    //创建POI检索监听者
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
        public void onGetPoiResult(PoiResult result){
            //获取POI检索结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {// 没有找到检索结果，并提示用户
                Toast.makeText(BusSearchActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            //遍历所有POI，找到类型为公交线路的POI
            for (PoiInfo poi : result.getAllPoi()) {
                if (poi.type == PoiInfo.POITYPE.BUS_LINE ||poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                    //说明该条POI为公交信息，获取该条POI的UID
                    busLineId = poi.uid;
                    break;
                }
            }
            //如下代码为发起检索代码，定义监听者和设置监听器的方法与POI中的类似
            mBusLineSearch.searchBusLine((new BusLineSearchOption()
                    .city(editCityEt.getText().toString().trim())// 城市
                    .uid(busLineId)));
        }
        public void onGetPoiDetailResult(PoiDetailResult result){
            //获取Place详情页检索结果
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            //获取POI室内检索结果
        }
    };

    OnGetBusLineSearchResultListener busLineListener =new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult result) {
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(BusSearchActivity.this, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            // 检索结果正常返回
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                BusLineOverlay overlay = new MyBuslineOverlay(mBaiduMap);// 用于显示一条公交详情结果的Overlay
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result);
                overlay.addToMap();// 将overlay添加到地图上
                overlay.zoomToSpan();// 缩放地图，使所有overlay都在合适的视野范围内
                // 公交线路名称
                Toast.makeText(BusSearchActivity.this,
                        result.getBusLineName(), Toast.LENGTH_SHORT)
                        .show();

                ViewStub viewStub = (ViewStub) findViewById(R.id.view_stub);
                if (viewStub != null) {
                    View inflatedView = viewStub.inflate();
                    listView = (ListView) inflatedView.findViewById(R.id.bus_line_lv);

                    String title;
                    List<BusLineResult.BusStation> stations = result.getStations();
                    List<String> datas=new ArrayList<>();
                    for (BusLineResult.BusStation busStation : stations) {
                        title = busStation.getTitle();
                        datas.add(title);
                    }
                    listView.setAdapter(new ArrayAdapter<String>(BusSearchActivity.this, android.R.layout.simple_list_item_1, datas));
                }
            }
        }
    };

    private class MyBuslineOverlay extends BusLineOverlay {
        public MyBuslineOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        /**
         * 站点点击事件
         */
        @Override
        public boolean onBusStationClick(int index) {
            MarkerOptions options = (MarkerOptions) getOverlayOptions().get(index);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(options.getPosition()));
            return true;
        }
    }
}
