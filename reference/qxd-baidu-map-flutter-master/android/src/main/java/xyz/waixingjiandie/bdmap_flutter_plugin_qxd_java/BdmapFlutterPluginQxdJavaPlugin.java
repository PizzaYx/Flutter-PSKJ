package xyz.waixingjiandie.bdmap_flutter_plugin_qxd_java;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

/** BdmapFlutterPluginQxdJavaPlugin */
public class BdmapFlutterPluginQxdJavaPlugin implements FlutterPlugin, ActivityAware, DefaultLifecycleObserver {
  private static final String TAG = "BdmapFlutterPluginQxdJavaPlugin";

  private FlutterPluginBinding pluginBinding;
  private Lifecycle lifecycle;
  private MethodChannel methodChannel;
  private EventChannel eventChannel;

  private boolean isPermissionRequested;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    pluginBinding = flutterPluginBinding;
    // 申请动态权限
    //requestPermission(flutterPluginBinding.getApplicationContext(), binding.getActivity());

    // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
    SDKInitializer.initialize(flutterPluginBinding.getApplicationContext());
    // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
    // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
    SDKInitializer.setCoordType(CoordType.BD09LL);

    methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bdmap_view_method");
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "bdmap_location_stream");

    final MethodCallHandlerImpl methodCallHandler = new MethodCallHandlerImpl(flutterPluginBinding.getApplicationContext());
    methodChannel.setMethodCallHandler(methodCallHandler);
    eventChannel.setStreamHandler(methodCallHandler);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    methodChannel.setMethodCallHandler(null);
    methodChannel = null;
    eventChannel.setStreamHandler(null);
    eventChannel = null;
    pluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    Log.d(TAG, "onAttachedToActivity");
    HiddenLifecycleReference reference = (HiddenLifecycleReference) binding.getLifecycle();
    lifecycle = reference.getLifecycle();
    //lifecycle = (Lifecycle) binding.getLifecycle();
    lifecycle.addObserver(this);

    pluginBinding.getPlatformViewRegistry().registerViewFactory(
            "plugins.waixingjiandie.xyz/baidu_maps",
            new BaiduMapFactory(pluginBinding.getBinaryMessenger(), binding.getActivity(), lifecycle));
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.onDetachedFromActivity();
    Log.d(TAG, "onDetachedFromActivityForConfigChanges");
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    Log.d(TAG, "onReattachedToActivityForConfigChanges");
    HiddenLifecycleReference reference = (HiddenLifecycleReference) binding.getLifecycle();
    lifecycle = reference.getLifecycle();
    //lifecycle = binding.getLifecycle();
    lifecycle.addObserver(this);
  }

  @Override
  public void onDetachedFromActivity() {
    lifecycle.removeObserver(this);
    Log.d(TAG, "onDetachedFromActivity");
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onCreate");
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onStart");
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onResume");
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onPause");
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onStop");
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onDestroy");
  }

  private void requestPermission(Context context, Activity activity) {
    if (!isPermissionRequested) {
      isPermissionRequested = true;
      ArrayList<String> permissionsList = new ArrayList<>();
      String[] permissions = {
              Manifest.permission.ACCESS_NETWORK_STATE,
              Manifest.permission.INTERNET,
              //Manifest.permission.WRITE_EXTERNAL_STORAGE,
              //Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_WIFI_STATE,
      };

      for (String perm : permissions) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, perm)) {
          permissionsList.add(perm);
          // 进入到这里代表没有权限.
        }
      }

      if (!permissionsList.isEmpty()) {
        String[] strings = new String[permissionsList.size()];
        ActivityCompat.requestPermissions(activity, permissionsList.toArray(strings), 0);
      }
    }
  }
}

