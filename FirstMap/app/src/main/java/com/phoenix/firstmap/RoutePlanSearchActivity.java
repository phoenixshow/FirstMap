package com.phoenix.firstmap;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.BusInfo;
import com.baidu.mapapi.search.core.CoachInfo;
import com.baidu.mapapi.search.core.PlaneInfo;
import com.baidu.mapapi.search.core.PriceInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.TaxiInfo;
import com.baidu.mapapi.search.core.TrainInfo;
import com.baidu.mapapi.search.core.TransitResultNode;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.List;

public class RoutePlanSearchActivity extends BaseActivity {
    private static final int MASS_ROUTE = 0;
    private static final int TRANSIT_ROUTE = 1;
    private static final int DRIVING_ROUTE = 2;
    private static final int WALKING_ROUTE = 3;
    private static final int BIKING_ROUTE = 4;
    private int DEFAULT_ROUTE = MASS_ROUTE;

    //城市文本框，关键字文本框
    private EditText fromCityEt, fromSearchKeyEt, toCityEt, toSearchKeyEt;
    private RadioGroup rg;

    RoutePlanSearch mSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan_search);

        fromCityEt = (EditText) findViewById(R.id.from_city_et);
        fromSearchKeyEt = (EditText) findViewById(R.id.from_searchKey_et);
        toCityEt = (EditText) findViewById(R.id.to_city_et);
        toSearchKeyEt = (EditText) findViewById(R.id.to_searchKey_et);
        rg = (RadioGroup) findViewById(R.id.rg);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.mass_route:
                        DEFAULT_ROUTE = MASS_ROUTE;
                        break;
                    case R.id.transit_route:
                        DEFAULT_ROUTE = TRANSIT_ROUTE;
                        break;
                    case R.id.driving_route:
                        DEFAULT_ROUTE = DRIVING_ROUTE;
                        break;
                    case R.id.walking_route:
                        DEFAULT_ROUTE = WALKING_ROUTE;
                        break;
                    case R.id.biking_route:
                        DEFAULT_ROUTE = BIKING_ROUTE;
                        break;
                }
            }
        });

        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
//        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        //创建公交线路规划检索实例；
        mSearch = RoutePlanSearch.newInstance();
        //设置公交线路规划检索监听者；
        mSearch.setOnGetRoutePlanResultListener( routeListener );
    }

    //创建公交线路规划检索监听者；
    OnGetRoutePlanResultListener routeListener = new OnGetRoutePlanResultListener(){
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            //获取步行线路规划结果
            if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(walkingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            //获取公交换乘路径规划结果
            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //result.getSuggestAddrInfo()
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(transitRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
            //获取跨城综合公共交通线路规划结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //未找到结果
                return;
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //result.getSuggestAddrInfo()
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                MassTransitRouteLine route = result.getRouteLines().get(0);
                //创建公交路线规划线路覆盖物
                MassTransitRouteOverlay overlay = new MyMassTransitRouteOverlay(mBaiduMap);
                //设置公交路线规划数据
                overlay.setData(route);
                //将公交路线规划覆盖物添加到地图中
                overlay.addToMap();
                overlay.zoomToSpan();

                TransitResultNode origin = result.getOrigin();//起点信息
                TransitResultNode destination = result.getDestination();//终点信息
                TaxiInfo massTaxiInfo = result.getTaxiInfo();
                int total = result.getTotal();//总路线数目
                SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();//建议起终点信息
                Log.e("TAG", "**************************总路线数:"+total+"**************************");
                List<MassTransitRouteLine> list = result.getRouteLines();//换乘方案
                for (int i=0; i<list.size(); i++) {
                    MassTransitRouteLine mRoutelines = list.get(i);
                    int distance = mRoutelines.getDistance();//距离（单位：米）
                    int duration = mRoutelines.getDuration();//耗时 (单位：秒)
                    String arriveTime = mRoutelines.getArriveTime();//本线路预计到达时间：格式举例：2016-09-29 13:48:00
                    double price = mRoutelines.getPrice();//本线路价格（元）
                    Log.e("TAG", "-----------------------换乘方案"+(i+1)+"-----------------------");
                    List<PriceInfo> priceInfo = mRoutelines.getPriceInfo();//票价详情（起终点为大陆地区同城时，此字段有值；其他情况，此字段为空）
                    List<List<MassTransitRouteLine.TransitStep>> newSteps = mRoutelines.getNewSteps();//一条线路中的步骤（step）
                    for (int j=0; j<newSteps.size(); j++){
                        Log.e("TAG", "------------步骤"+(j+1)+"------------");
                        List<MassTransitRouteLine.TransitStep> newStep = newSteps.get(j);
                        for (int k=0; k<newStep.size(); k++){
                            Log.e("TAG", "-------详情"+(k+1)+"-------");
                            MassTransitRouteLine.TransitStep step = newStep.get(k);
                            int stepDistance = step.getDistance();//距离
                            int stepDuration = step.getDuration();//耗时
                            List<MassTransitRouteLine.TransitStep.TrafficCondition> trafficConditions = step.getTrafficConditions();//路况状态
                            LatLng startLocation = step.getStartLocation();//起点
                            LatLng endLocation = step.getEndLocation();//终点
                            String instructions = step.getInstructions();//换乘说明
                            MassTransitRouteLine.TransitStep.StepVehicleInfoType vehileType = step.getVehileType();//交通工具类型（火车，飞机，大巴，公交，驾车，步行)
                            // 交通工具为驾车或步行时，无详情；为其他4种方式，则对应以下4种工具详情）
                            TrainInfo trainInfo = step.getTrainInfo();//火车详情
                            PlaneInfo planeInfo = step.getPlaneInfo();//飞机详情
                            CoachInfo coachInfo = step.getCoachInfo();//大巴详情
                            BusInfo busInfo = step.getBusInfo();//公交详情
                            Log.e("TAG","交通工具类型:"+vehileType+",距离:"+stepDistance+",耗时:"+stepDuration+",换乘说明:"+instructions);
                        }
                    }
                }
            }
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            //获取驾车线路规划结果
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
            //室内
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            //获取骑行线路规划结果
            if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(bikingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }
    };

    public void routePlanSearch(View view) {
        PlanNode stNode = PlanNode.withCityNameAndPlaceName(
                fromCityEt.getText().toString().trim(),
                fromSearchKeyEt.getText().toString().trim());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(
                toCityEt.getText().toString().trim(),
                toSearchKeyEt.getText().toString().trim());

        switch (DEFAULT_ROUTE){
            case MASS_ROUTE:
                mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stNode).to(enNode));
                break;
            case TRANSIT_ROUTE:
                mSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stNode)
                        .city(fromCityEt.getText().toString().trim())
                        .to(enNode));
                break;
            case DRIVING_ROUTE:
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(stNode)
                        .to(enNode));
                break;
            case WALKING_ROUTE:
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(stNode)
                        .to(enNode));
                break;
            case BIKING_ROUTE:
                mSearch.bikingSearch((new BikingRoutePlanOption())
                        .from(stNode)
                        .to(enNode));
                break;
        }
    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {
        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }
}
