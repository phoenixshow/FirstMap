package com.phoenix.firstmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.phoenix.firstmap.utils.DisplayUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextureMapView mMapView = null;
//    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Marker marker = null;
    private boolean isOpen = false;

    private boolean isGrant = true;
    private final int SDK_PERMISSION_REQUEST = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
//        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();

        /*//普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //卫星地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //空白地图, 基础地图瓦片将不会被渲染。在地图类型中设置为NONE，将不会使用流量下载基础地图瓦片图层。使用场景：与瓦片图层一起使用，节省流量，提升自定义瓦片图下载速度。
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);*/

        /*//开启交通图
        mBaiduMap.setTrafficEnabled(true);
        //城市热力图
        mBaiduMap.setBaiduHeatMapEnabled(true);*/

        //定义Maker坐标点
        LatLng point = new LatLng(39.963175, 116.400244);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .zIndex(9)  //设置marker所在层级
                .draggable(true)  //设置手势拖拽
                .alpha(0.5f) //给Marker设置透明度
                .animateType(MarkerOptions.MarkerAnimateType.grow);// grow生长动画，drop掉下
        //在地图上添加Marker，并显示
        marker = (Marker) mBaiduMap.addOverlay(option);
        //调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                //拖拽中
            }
            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束
                Log.e("TAG", "拖拽结束，当前经纬度："+marker.getPosition().longitude+":"+marker.getPosition().latitude);
                //构造MapStatusUpdate，并设置结束点为中心点
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                mBaiduMap.animateMapStatus(msu);
            }
            public void onMarkerDragStart(Marker marker) {
                //开始拖拽
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker arg0) {
                if (isOpen){
                    mBaiduMap.hideInfoWindow();
                    isOpen = false;
                }else {
                    //创建InfoWindow展示的view
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);
                    //因为我们通过一个Button来展示InfoWindow，所以还可以设置文字
                    button.setText("展示弹出窗");
                    //设置文字颜色
                    button.setTextColor(Color.BLUE);
                    //定义用于显示该InfoWindow的坐标点//坐标定义为跟标注相同
                    LatLng pt = new LatLng(39.963175, 116.400244);
                    //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
//                    InfoWindow mInfoWindow = new InfoWindow(button, pt, DisplayUtil.dip2px(MainActivity.this, -47));
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(button);
                    InfoWindow mInfoWindow = new InfoWindow(bitmapDescriptor, pt,
                            DisplayUtil.dip2px(MainActivity.this, -47), new InfoWindow.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick() {
                            mBaiduMap.hideInfoWindow();
                            isOpen = false;
                        }
                    });
                    //显示InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                    isOpen = true;
                }
                return false;
            }
        });

        /*//定义文字所显示的坐标点
        LatLng llText = new LatLng(39.86923, 116.397428);
        //构建文字Option对象，用于在地图上添加文字
        OverlayOptions textOption = new TextOptions()
                .bgColor(0xAAFFFF00)
                .fontSize(24)
                .fontColor(0xFFFF00FF)
                .text("百度地图SDK")
                .rotate(-30)
                .position(llText);
        //在地图上添加该文字对象并显示
        mBaiduMap.addOverlay(textOption);*/

        // after andrioid m,must request Permiision on runtime
        getPersimmions();
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SDK_PERMISSION_REQUEST:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 允许
                    Toast.makeText(this,getString(R.string.permisstion_grant),Toast.LENGTH_SHORT).show();
                }else{
                    // 不允许
                    isGrant = false;
                    Toast.makeText(this,getString(R.string.permisstion_deny),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void deleteMarker(View view) {
        if (marker != null) {
            //调用Marker对象的remove方法实现指定marker的删除
            marker.remove();
        }
    }

    public void openPOISearch(View view) {
        Intent intent = new Intent(MainActivity.this, PoiSearchActivity.class);
        startActivity(intent);
    }

    public void busSearch(View view) {
        Intent intent = new Intent(MainActivity.this, BusSearchActivity.class);
        startActivity(intent);
    }

    public void routePlanSearch(View view) {
        Intent intent = new Intent(MainActivity.this, RoutePlanSearchActivity.class);
        startActivity(intent);
    }

    public void geoCode(View view) {
        Intent intent = new Intent(MainActivity.this, GeoCodeActivity.class);
        startActivity(intent);
    }

    public void location(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
        }else {
            Toast.makeText(this, "请先授权再开始定位", Toast.LENGTH_LONG).show();
        }
    }
}
