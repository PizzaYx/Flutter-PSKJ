package xyz.waixingjiandie.bdmap_flutter_plugin_qxd_java;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.Lifecycle;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.model.LatLng;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class BaiduMapFactory extends PlatformViewFactory {
  private static final String TAG = "BaiduMapFactory";

  private final BinaryMessenger messenger;
  private final Activity activity;
  private final Lifecycle lifecycle;

  BaiduMapFactory(BinaryMessenger messenger, Activity activity, Lifecycle lifecycle) {
    super(StandardMessageCodec.INSTANCE);
    this.messenger = messenger;
    this.activity = activity;
    this.lifecycle = lifecycle;
  }

  @Override
  public PlatformView create(final Context context, int viewId, Object args) {
    Log.d(TAG, "create - " + viewId);
    Map<String, Object> params = (Map<String, Object>) args; // Flutter传来的创建地图的参数，主要用来设置初始中心位置
    final BaiduMapOptions options = new BaiduMapOptions();
    MapStatus.Builder mapStatusBuilder = new MapStatus.Builder();

    if (params.containsKey("lat") && params.containsKey("lng")) {
      LatLng center = new LatLng((double) params.get("lat"), (double) params.get("lng"));
      mapStatusBuilder.target(center); // 地图中心坐标属性存在于 MapStatus
    }

    options.mapStatus(mapStatusBuilder.build()); // 把 MapStatus 设置到地图参数

    final BaiduMapController controller = new BaiduMapController(viewId, context, messenger, activity, options);
    if (lifecycle != null) {
      lifecycle.addObserver(controller);
    }

    return controller;
  }
}
