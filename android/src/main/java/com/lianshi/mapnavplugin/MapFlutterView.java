package com.lianshi.mapnavplugin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.brtbeacon.map.map3d.BRTMapEnvironment;
import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map.map3d.entity.BRTPoi;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.utils.BRTSearchAdapter;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

import java.util.LinkedList;
import java.util.List;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class MapFlutterView implements PlatformView,MethodChannel.MethodCallHandler{

    private static final int BRTMAP_PERMISSION_CODE = 9999;
    private BRTMapView mapView;
    private Context context;
    public View mNativeView;
    public Activity mactivity;
    private final MethodChannel methodChannel;
    private BRTSearchAdapter searchAdapter = null;


    MapFlutterView(Context context, Activity activity, BinaryMessenger m_messenger, Object args) {
        this.context = context;
        this.mactivity = activity;
        this.methodChannel = new MethodChannel(m_messenger,"plugins.mapnavplugin");
        methodChannel.setMethodCallHandler(this);
        BRTMapEnvironment.initMapEnvironment(context);
        mNativeView = LayoutInflater.from(context).inflate(R.layout.activity_main, null, true);
        mapView = (BRTMapView) mNativeView.findViewById(R.id.mapNavView);
       // floorView = mNativeView.findViewById(R.id.layout_floor);

        if (!checkNeedPermission( )) {
            mapView.init("00280019", "ab487b0bd7184f14abc5a6304d4236a5");
        }

        mapView.addMapListener(mapViewListener);
    }


    private BRTMapView.BRTMapViewListener mapViewListener = new BRTMapView.BRTMapViewListener( ) {

        @Override
        public void mapViewDidLoad(BRTMapView brtMapView, Error error) {
            if (error != null) {
                return;
            }

            /**
             * 地图加载成功后，初始化搜索引擎；
             * init Search Engine
             */
            searchAdapter = new BRTSearchAdapter(context, mapView.getBuilding().getBuildingID());
            //地图加载成功后，显示第一个楼层
            mapView.setFloor(mapView.getFloorList().get(0));
            mapView.setFloorByNumber(0);

            //缩放等级
            mapView.setZoom(19); // level: 0～22
            //旋转
            mapView.setBearing(90); // bearing: 0～360
            //倾斜
            mapView.setTilt(0); // tilt: 0～60
            //设置是否可见 1不可见
            mapView.setLogoVisible(1);
        }

        @Override
        public void onFinishLoadingFloor(BRTMapView brtMapView, BRTFloorInfo brtFloorInfo) {

        }

        @Override
        public void onClickAtPoint(BRTMapView brtMapView, BRTPoint brtPoint) {

        }

        @Override
        public void onPoiSelected(BRTMapView brtMapView, List<BRTPoi> list) {

        }
    };

    private static final List<String> permissionsNeedCheck;

    static {
        permissionsNeedCheck = new LinkedList<>( );
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean checkNeedPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断当前系统的SDK版本是否大于23
            List<String> permissionNeedRequest = new LinkedList<>( );
            for (String permssion : permissionsNeedCheck) {
                if (ActivityCompat.checkSelfPermission(context, permssion) != PackageManager.PERMISSION_GRANTED) {
                    permissionNeedRequest.add(permssion);
                }
            }
            if (!permissionNeedRequest.isEmpty( )) {
                ActivityCompat.requestPermissions(mactivity, permissionNeedRequest.toArray(new String[0]), BRTMAP_PERMISSION_CODE);
                return true;
            }
        }

        return false;
    }

    @Override
    public View getView() {
        return mNativeView;
    }

    @Override
    public void dispose() {
        mNativeView = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method){
            case "switchFloor":{
                int floornum = call.argument("floor");
                mapView.setFloorByNumber(floornum);
            }
        }
    }
}
