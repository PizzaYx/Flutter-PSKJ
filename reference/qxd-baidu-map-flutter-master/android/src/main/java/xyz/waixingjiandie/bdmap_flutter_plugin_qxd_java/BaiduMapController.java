package xyz.waixingjiandie.bdmap_flutter_plugin_qxd_java;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

final class BaiduMapController
    implements DefaultLifecycleObserver,
        ActivityPluginBinding.OnSaveInstanceStateListener,
        MethodChannel.MethodCallHandler,
        PlatformView {

  private static final String TAG = "BaiduMapController";
  //private final int id;
  private final Context context; // 百度定位需要用到
  private final MethodChannel methodChannel;
  private final TextureMapView mapView;
  private BaiduMap baiduMap;
  //private final Activity activity;
  private LocationClient mLocationClient = null;

  private boolean disposed = false;
  private MethodChannel.Result mapReadyResult;

  BaiduMapController(
      int id,
      Context context,
      BinaryMessenger binaryMessenger, // 用于创建 MethodChannel
      Activity activity, // 创建 TextureMapView 需要用到
      BaiduMapOptions options) {
    this.context = context; // 来自 PlatformViewFactory 的 create
    //this.id = id;
    //this.activity = activity;
    this.methodChannel = new MethodChannel(binaryMessenger, "plugins.waixingjiandie.xyz/baidu_maps_" + id);
    methodChannel.setMethodCallHandler(this);
    this.mapView = new TextureMapView(activity, options);
    this.baiduMap = mapView.getMap();
    // 设置地图加载完成回调，该接口需要在地图加载到页面之前调用，否则不会触发回调
    baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
      @Override
      public void onMapLoaded() {
        Log.d(TAG, "baidu map loaded");
        if (mapReadyResult != null) {
          mapReadyResult.success(null);
          mapReadyResult = null;
        }
      }
    });
  }

  /**
   * 开始定位
   */
  private void startLocation() {
    if(null != mLocationClient) {
      mLocationClient.start();
    }
  }

  /**
   * 停止定位
   */
  private void stopLocation() {
    if (null != mLocationClient) {
      mLocationClient.stop();
      mLocationClient = null;
    }
  }

  /**
   * 在地图上定位到自己的位置
   */
  private void locate(android.content.Context context) {
    if (null == mLocationClient) {
      mLocationClient = new LocationClient(context);
    }
    mLocationClient.registerLocationListener(new MapLocationListener());

    LocationClientOption option = new LocationClientOption();
    option.setOpenGps(true); // 打开gps
    option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
    option.setCoorType("bd09ll"); // 设置坐标类型，需要和地图的坐标类型一致。
    mLocationClient.setLocOption(option);

    startLocation();
  }

  public class MapLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
      //mapView 销毁后不再处理新接收的位置（抄来的）
      if (location == null || mapView == null){
        return;
      }
      //MapStatus.Builder builder = new MapStatus.Builder();
      LatLng center = new LatLng(location.getLatitude(), location.getLongitude()); // 得到定位的坐标
      //builder.target(center);
      MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(center);
      baiduMap.setMapStatus(mapStatusUpdate); // 更新中心坐标
      stopLocation();
    }
  }

  @Override
  public View getView() {
    return mapView;
  }

  @Override
  public void dispose() {
    if (disposed) {
      return;
    }
    disposed = true;
    methodChannel.setMethodCallHandler(null);
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    switch (call.method) {
      case "map#waitForMap": { // 通知Flutter地图是否已建立
        if (baiduMap != null) {
          result.success(null);
          return;
        }
        mapReadyResult = result;
        break;
      }
      case "map#getLatLng": {
        final LatLng target = baiduMap.getMapStatus().target; // 获取地图当前中心的坐标
        final Map<String, Double> data = new HashMap<>(2);
        data.put("lat", target.latitude);
        data.put("lng", target.longitude);
        result.success(data);
        break;
      }
      case "map#move2mine": {
        locate(this.context);
        break;
      }
      default:
        result.notImplemented();
    }
  }

  // DefaultLifecycleObserver and OnSaveInstanceStateListener
  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onCreate");
    if (disposed) {
      return;
    }

    mapView.onCreate(null, null);
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onStart");
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onResume");
    if (disposed) {
      return;
    }
    mapView.onResume();
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onPause");
    if (disposed) {
      return;
    }
    mapView.onPause();
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onStop");
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onDestroy");
    if (disposed) {
      return;
    }
    stopLocation();
    mapView.onDestroy();
  }

  @Override
  public void onSaveInstanceState(Bundle bundle) {
    Log.d(TAG, "onSaveInstanceState");
    if (disposed) {
      return;
    }
    mapView.onSaveInstanceState(bundle);
  }

  @Override
  public void onRestoreInstanceState(Bundle bundle) {
    Log.d(TAG, "onRestoreInstanceState");
    if (disposed) {
      return;
    }
    mapView.onCreate(null,bundle);
  }
}
