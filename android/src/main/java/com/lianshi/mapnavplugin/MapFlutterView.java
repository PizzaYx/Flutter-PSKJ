package com.lianshi.mapnavplugin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.brtbeacon.map.map3d.BRTMapEnvironment;
import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map.map3d.entity.BRTPoi;
import com.brtbeacon.map.map3d.entity.BRTPoiEntity;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.route.BRTDirectionalHint;
import com.brtbeacon.map.map3d.route.BRTMapRouteManager;
import com.brtbeacon.map.map3d.route.BRTRoutePart;
import com.brtbeacon.map.map3d.route.BRTRouteResult;
import com.brtbeacon.map.map3d.utils.BRTConvert;
import com.brtbeacon.map.map3d.utils.BRTSearchAdapter;
import com.brtbeacon.mapsdk.RoutePart;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;

import java.util.Calendar;
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
    private BRTSearchAdapter searchAdapter = null;//查询
    private BRTMapRouteManager routeManager = null;//导航
    public static final String ARG_MAP_BUNDLE = "arg_map_bundle";
    private  final String buildingid = "00280019";
    private  final String appkey = "ab487b0bd7184f14abc5a6304d4236a5";

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
            mapView.init(buildingid, appkey);
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

            routeManager = new BRTMapRouteManager(context, mapView.getBuilding(), appkey, mapView.getFloorList(), true);
            routeManager.addRouteManagerListener(routeManagerListener);
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

    //  路径规划监听器
    private BRTMapRouteManager.BRTRouteManagerListener routeManagerListener =
            new BRTMapRouteManager.BRTRouteManagerListener(){

                @Override
                public void didSolveRouteWithResult(BRTMapRouteManager brtMapRouteManager, BRTRouteResult brtRouteResult) {
                    mactivity.runOnUiThread(new Runnable( ) {
                        @Override
                        public void run() {
                            //路径规划成功执行完成
                            mapView.setRouteResult(brtRouteResult);
                            //导航
                            startProcessWalk();
                        }
                    });
                }

                @Override
                public void didFailSolveRouteWithError(BRTMapRouteManager brtMapRouteManager, BRTMapRouteManager.BRTRouteException e) {
                    mactivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(e.getMessage());
                        }
                    });
                }
            };

    private static final List<String> permissionsNeedCheck;

    static {
        permissionsNeedCheck = new LinkedList<>( );
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    //检查权限
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
            break;
            case "simulateNavigation":{
                String startPoi = call.argument("startPoi");
                String endPoi = call.argument("endPoi");
                requestSimulationPath(getPoiInfo(startPoi),getPoiInfo(endPoi));
            }
            break;
        }
    }

    //根据poi查询point
    public BRTPoint getPoiInfo(String poi){
        BRTPoint brtPoint = null;
        List<BRTPoiEntity> entityList = searchAdapter.querySql("select * from POI where POI_ID = '" + poi+"'");
        if(entityList.size() > 0)
            brtPoint = entityList.get(0).getPoint();
        return brtPoint;
    }
    //路径规划


    //路径规划
    public void requestSimulationPath(BRTPoint startPoint,BRTPoint endPoint){
        if(startPoint==null || endPoint == null)
        {
            showToast("startPoint=  "+startPoint + "   endPoint=  " + endPoint);
            return;
        }

        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);
        routeManager.requestRoute(startPoint, endPoint);


    }

    //提示
    private Toast mToast;
    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    //模拟导航
    private BRTRoutePart walkPart = null;
    private double walkPartLength = 0.0;
    private long lastUpdateMillis = 0;
    private double walkSpeed = 10; //10 Meter Per Second
    private Handler handler = new Handler();

    private void startProcessWalk() {
        lastUpdateMillis = Calendar.getInstance().getTimeInMillis();
        walkPart = mapView.getRouteResult().getAllRouteParts().get(0);
        walkPartLength = 0.0;
        processNextWalk();
    }

    private void stopProcessWalk() {
        handler.removeCallbacks(walkTimeTask);

    }

    private void processNextWalk() {
        handler.postDelayed(walkTimeTask, 30);
    }

    private Runnable walkTimeTask = new Runnable() {
        @Override
        public void run() {
            if (walkPart == null)
                return;

            if (mactivity.isFinishing())
                return;

            long currentTimeMillis = Calendar.getInstance().getTimeInMillis();
            long timePeriod = currentTimeMillis - lastUpdateMillis;
            lastUpdateMillis = currentTimeMillis;
            double walkLength = walkSpeed * timePeriod / 1000.0;
            walkPartLength += walkLength;
            RoutePart jtsRoutePart = walkPart.getJtsRoutePart();
            com.vividsolutions.jts.geom.LineString jtsRoute = jtsRoutePart.getRoute();
            double partLength = jtsRoute.getLength();

            while(walkPartLength > partLength) {
                if (walkPart.isLastPart()) {
                    walkPartLength = partLength;
                    break;
                } else {
                    walkPartLength -= partLength;
                    walkPart = walkPart.getNextPart();
                    jtsRoutePart = walkPart.getJtsRoutePart();
                    jtsRoute = jtsRoutePart.getRoute();
                    partLength = jtsRoute.getLength();
                }
            }

            if (walkPart.getFloorInfo().getFloorNumber() != mapView.getCurrentFloor().getFloorNumber()) {
                mapView.setFloorByNumber(walkPart.getFloorInfo().getFloorNumber());
            }


            LengthLocationMap lengthLocationMap = new LengthLocationMap(jtsRoute);
            LinearLocation linearLocation = lengthLocationMap.getLocation(walkPartLength);
            Coordinate coordinate = linearLocation.getCoordinate(jtsRoute);
            LatLng latLng = BRTConvert.toLatLng(coordinate.x, coordinate.y);
            BRTPoint location = new BRTPoint(walkPart.getFloorInfo().getFloorNumber(), latLng);
            mapView.setLocation(location);



            if (walkPartLength < partLength) {
                processNextWalk();
            } else {
                stopProcessWalk();
                showToast("导航结束");
            }
            showCurrentHint(location);
        }
    };

    //显示数据
    private void showCurrentHint(BRTPoint lp) {
        BRTRouteResult routeResult = mapView.getRouteResult();
        BRTRoutePart part = routeResult.getNearestRoutePart(lp);
        if (part != null) {
            List<BRTDirectionalHint> hints = part.getRouteDirectionalHint();
            BRTDirectionalHint hint = part.getDirectionalHintForLocationFromHints(lp, hints);
            if (hint != null) {
//                tvHint.setText(getString(R.string.hint_direction) + hint.getDirectionString() + hint.getRelativeDirection() + "\n"
//                        + getString(R.string.hint_part_length) + String.format("%.2f", hint.getLength()) + "\n"
//                        + getString(R.string.hint_part_angle) + String.format("%.2f", hint.getCurrentAngle()) + "\n"
//                        + getString(R.string.hint_route_left_and_length) + String.format("%.2f", routeResult.distanceToRouteEnd(lp)) + "\n"
//                        + "/" + String.format("%.2f", routeResult.length));

                mapView.lookAt(lp, hint, 500);
                mapView.processDeviceRotation(90 - hint.getCurrentAngle());
            }
        }
    }
}
