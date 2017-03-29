package com.phoenix.firstmap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.PoiOverlay;
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

public class PoiSearchActivity extends BaseActivity implements View.OnClickListener {
    //城市文本框，关键字文本框
    private EditText editCityEt, editSearchKeyEt;
    // 城市检索，区域检索，周边检索，下一组数据 按钮
    private Button citySearchBtn, boundSearchBtn, nearbySearchBtn, nextDataBtn;

    // 记录检索类型
    private int type;
    private int page = 1;
    private int totalPage = 0;

    PoiSearch mPoiSearch = null;

    private double latitude = 39.963175;
    private double longitude = 116.400244;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);

        editCityEt = (EditText) findViewById(R.id.city_et);
        editSearchKeyEt = (EditText) findViewById(R.id.searchKey_et);
        citySearchBtn = (Button) findViewById(R.id.city_search_btn);
        boundSearchBtn = (Button) findViewById(R.id.bound_search_btn);
        nearbySearchBtn = (Button) findViewById(R.id.nearby_search_btn);
        nextDataBtn = (Button) findViewById(R.id.next_data_btn);
        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
//        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        nextDataBtn.setEnabled(false);
        //创建POI检索实例
        mPoiSearch = PoiSearch.newInstance();
        //设置POI检索监听者
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

        citySearchBtn.setOnClickListener(this);
        boundSearchBtn.setOnClickListener(this);
        nearbySearchBtn.setOnClickListener(this);
        nextDataBtn.setOnClickListener(this);

        editSearchKeyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                citySearchBtn.setEnabled(true);
                boundSearchBtn.setEnabled(true);
                nearbySearchBtn.setEnabled(true);
                nextDataBtn.setEnabled(false);
                page = 1;
                totalPage = 0;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.city_search_btn:
                type = 0;
                page = 1;
                citySearchBtn.setEnabled(false);
                boundSearchBtn.setEnabled(true);
                nearbySearchBtn.setEnabled(true);
                nextDataBtn.setEnabled(true);
                citySearch(page);
                break;
            case R.id.bound_search_btn:
                type = 1;
                page = 1;
                citySearchBtn.setEnabled(true);
                boundSearchBtn.setEnabled(false);
                nearbySearchBtn.setEnabled(true);
                nextDataBtn.setEnabled(true);
                boundSearch(page);
                break;
            case R.id.nearby_search_btn:
                type = 2;
                page = 1;
                citySearchBtn.setEnabled(true);
                boundSearchBtn.setEnabled(true);
                nearbySearchBtn.setEnabled(false);
                nextDataBtn.setEnabled(true);
                nearbySearch(page);
                break;
            case R.id.next_data_btn:
                if (++page <= totalPage) {
                    switch (type) {
                        case 0:
                            citySearch(page);
                            break;
                        case 1:
                            boundSearch(page);
                            break;
                        case 2:
                            nearbySearch(page);
                            break;
                    }
                } else {
                    Toast.makeText(PoiSearchActivity.this, "已经是最后一页啦~", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    //城市检索
    private void citySearch(int page){
        //设置检索参数，发起检索请求
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(editCityEt.getText().toString().trim())// 城市
                .keyword(editSearchKeyEt.getText().toString().trim())// 关键字
                .pageNum(page)// 分页编号
                .pageCapacity(10));// 默认每页10条
    }

    //区域（范围）检索
    private void boundSearch(int page) {
        LatLng westSouth = new LatLng(latitude - 0.01, longitude - 0.012);// 西南
        LatLng eastNorth = new LatLng(latitude + 0.01, longitude + 0.012);// 东北
        LatLngBounds bounds = new LatLngBounds.Builder().include(westSouth).include(eastNorth).build();// 得到一个地理范围对象
        // 发起poi范围检索请求
        mPoiSearch.searchInBound((new PoiBoundSearchOption())
                .bound(bounds)// 设置poi检索范围
                .keyword(editSearchKeyEt.getText().toString())// 检索关键字
                .pageNum(page));
    }

    //附近检索
    private void nearbySearch(int page) {
        // 发起附近检索请求
        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                .location(new LatLng(latitude, longitude))
                .keyword(editSearchKeyEt.getText().toString())
                .radius(1000)// 检索半径，单位是米
                .pageNum(page));
    }

    //创建POI检索监听者
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
        public void onGetPoiResult(PoiResult result){
            //获取POI检索结果
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果，并提示用户
                Toast.makeText(PoiSearchActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            // 检索结果正常返回
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                //创建PoiOverlay
                PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                //设置overlay可以处理标注点击事件
                mBaiduMap.setOnMarkerClickListener(overlay);
                //设置PoiOverlay数据
                overlay.setData(result);
                //添加PoiOverlay到地图中
                overlay.addToMap();
                overlay.zoomToSpan();
                totalPage = result.getTotalPageNum();// 获取总分页数
                Toast.makeText(PoiSearchActivity.this, "总共查到" + result.getTotalPoiNum() + "个兴趣点, 分为" + totalPage + "页", Toast.LENGTH_LONG).show();
                return;
            }
        }
        public void onGetPoiDetailResult(PoiDetailResult result){
            //获取Place详情页检索结果
            if (result.error != SearchResult.ERRORNO.NO_ERROR) {//如果不是检索结果正常返回
                Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
            } else {
                // 正常返回结果的时候，此处可以获得很多相关信息，这里只打印了名称和地址
                Toast.makeText(PoiSearchActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            //获取POI室内检索结果
        }
    };

    //构造自定义 PoiOverlay 类
    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(index);
            // 检索poi详细信息
            mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid));
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        super.onDestroy();
    }
}
