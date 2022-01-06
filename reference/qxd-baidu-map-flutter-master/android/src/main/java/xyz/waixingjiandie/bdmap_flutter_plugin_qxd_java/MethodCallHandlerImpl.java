package xyz.waixingjiandie.bdmap_flutter_plugin_qxd_java;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler, EventChannel.StreamHandler {
  private static final String TAG = "MethodCallHandlerImpl";
  private Context mContext;
  private LocationClient mLocationClient = null;
  private EventChannel.EventSink mEventSink = null;
  private MethodChannel.Result locResult = null; // 为了能在onMethodCall函数之外将结果返回到Flutter端

  private boolean isPurporseLoc = false;
  private boolean isInChina = false;
  private boolean useFuture = false;

  MethodCallHandlerImpl(Context context) {
    this.mContext = context;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }
    else if (call.method.equals("initLocation")) {
      useFuture = false;
      try {
        initLocation((Map) call.arguments);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    else if (call.method.equals("startLocation")) {
      startLocation();
    }
    else  if (call.method.equals("stopLocation")) {
      stopLocation();
    }
    else if (call.method.equals("locate")) {
      locResult = result;
      useFuture = true; // 使用Future那个监听，我额外加的
      try {
        initLocation((Map) call.arguments);
      } catch (Exception e) {
        e.printStackTrace();
      }
      startLocation();
    }
    else if (call.method.equals("getDistance")) {
      final List data = (List) call.arguments;
      final LatLng p1 = new LatLng((double) data.get(0), (double) data.get(1));
      final LatLng p2 = new LatLng((double) data.get(2), (double) data.get(3));

      result.success(DistanceUtil.getDistance(p1, p2));
    }
    else {
      result.notImplemented();
    }
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    mEventSink = events;
  }

  @Override
  public void onCancel(Object arguments) {
    stopLocation();
  }

  /**
   * 准备定位，创建LocationClient，设置参数
   * @param arguments
   */
  private void initLocation(Map arguments) {
    if (null == mLocationClient) {
      mLocationClient = new LocationClient(mContext);
    }

    if (useFuture) {
      mLocationClient.registerLocationListener(new FutureLocationListener());
    }
    else {
      mLocationClient.registerLocationListener(new EventLocationListener());
    }

    // 判断是否启用国内外位置判断功能（没用到，从百度Flutter插件复制而来）
    if (arguments.containsKey("isInChina")) {
      isInChina = true;
      return;
    } else {
      isInChina =false;
    }

    LocationClientOption option = new LocationClientOption();
    parseOptions(option, arguments);
    option.setProdName("flutter"); // 设置Prod字段值（复制而来，不知道是啥）
    option.setIsNeedLocationDescribe(true); // 启用获取位置描述
    mLocationClient.setLocOption(option);
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
   * 我只需要以Future返回就好了，简单点
   */
  class  FutureLocationListener extends BDAbstractLocationListener {

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("latitude", bdLocation.getLatitude()); // 纬度
      result.put("longitude", bdLocation.getLongitude()); // 经度
      result.put("locationDetail", bdLocation.getLocationDescribe()); // 位置语义化描述
      Log.d(TAG, result.toString());
      locResult.success(result);
      stopLocation();
    }
  }

  /**
   * 实现监听接口，Flutter端以Stream方式接收
   */
  class EventLocationListener extends BDAbstractLocationListener {

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

      if (null == mEventSink) {
        return;
      }

      Map<String, Object> result = new LinkedHashMap<>();

      // 判断国内外获取结果
      if (isInChina) {
        if (bdLocation.getLocationWhere() == BDLocation.LOCATION_WHERE_IN_CN) {
          result.put("isInChina", 1); // 在国内
        } else {
          result.put("isInChina", 0); // 在国外
        }
        mEventSink.success(result);
        return;
      }

      // 场景定位获取结果
      if (isPurporseLoc) {
        result.put("latitude", bdLocation.getLatitude()); // 纬度
        result.put("longitude", bdLocation.getLongitude()); // 经度
        mEventSink.success(result);
        return;
      }
      result.put("callbackTime", formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
      if (null != bdLocation) {
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation
            || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation
            || bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {
          result.put("locType", bdLocation.getLocType()); // 定位结果类型
          result.put("locTime", bdLocation.getTime()); // 定位成功时间
          result.put("latitude", bdLocation.getLatitude()); // 纬度
          result.put("longitude", bdLocation.getLongitude()); // 经度
          if (bdLocation.hasAltitude()) {
            result.put("altitude", bdLocation.getAltitude()); // 高度
          }
          result.put("radius", Double.parseDouble(String.valueOf(bdLocation.getRadius()))); // 定位精度
          result.put("country", bdLocation.getCountry()); // 国家
          result.put("province", bdLocation.getProvince()); // 省份
          result.put("city", bdLocation.getCity()); // 城市
          result.put("district", bdLocation.getDistrict()); // 区域
          result.put("town", bdLocation.getTown()); // 城镇
          result.put("street", bdLocation.getStreet()); // 街道
          result.put("address", bdLocation.getAddrStr()); // 地址
          result.put("locationDetail", bdLocation.getLocationDescribe()); // 位置语义化描述
          if (null != bdLocation.getPoiList() && !bdLocation.getPoiList().isEmpty()) {

            List<Poi> pois = bdLocation.getPoiList();
            StringBuilder stringBuilder = new StringBuilder();

            if (pois.size() == 1) {
              stringBuilder.append(pois.get(0).getName()).append(",").append(pois.get(0).getTags())
                  .append(pois.get(0).getAddr());
            } else {
              for (int i = 0; i < pois.size() - 1; i++) {
                stringBuilder.append(pois.get(i).getName()).append(",").append(pois.get(i).getTags())
                    .append(pois.get(i).getAddr()).append("|");
              }
              stringBuilder.append(pois.get(pois.size()-1).getName()).append(",").append(pois.get(pois.size()-1).getTags())
                  .append(pois.get(pois.size()-1).getAddr());

            }

            result.put("poiList",stringBuilder.toString()); // 周边poi信息
//
          }
          if (bdLocation.getFloor() != null) {
            // 当前支持高精度室内定位
            String buildingID = bdLocation.getBuildingID();// 百度内部建筑物ID
            String buildingName = bdLocation.getBuildingName();// 百度内部建筑物缩写
            String floor = bdLocation.getFloor();// 室内定位的楼层信息，如 f1,f2,b1,b2
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(buildingID).append("-").append(buildingName).append("-").append(floor);
            result.put("indoor", stringBuilder.toString()); // 室内定位结果信息
            mLocationClient.startIndoorMode();// 开启室内定位模式（重复调用也没问题），开启后，定位SDK会融合各种定位信息（GPS,WI-FI，蓝牙，传感器等）连续平滑的输出定位结果；
          } else {
            mLocationClient.stopIndoorMode(); // 处于室外则关闭室内定位模式
          }
        } else {
          result.put("errorCode", bdLocation.getLocType()); // 定位结果错误码
          result.put("errorInfo", bdLocation.getLocTypeDescription()); // 定位失败描述信息
        }
      } else {
        result.put("errorCode", -1);
        result.put("errorInfo", "location is null");
      }
      mEventSink.success(result); // android端实时检测位置变化，将位置结果发送到flutter端
    }
  }


  /**
   * 解析定位参数
   * @param option
   * @param arguments
   */
  private void parseOptions(LocationClientOption option,Map arguments) {
    if (arguments != null) {

      // 可选，设置是否返回逆地理地址信息。默认是true
      if (arguments.containsKey("isNeedAddres")) {
        if (((boolean)arguments.get("isNeedAddres"))) {
          option.setIsNeedAddress(true);
        } else {
          option.setIsNeedAddress(false);
        }
      }

      // 可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
      if (arguments.containsKey("locationMode")) {
        if (((int)arguments.get("locationMode")) == 1) {
          option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 高精度模式
        } else if (((int)arguments.get("locationMode")) == 2) {
          option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors); // 仅设备模式
        } else if (((int)arguments.get("locationMode")) == 3) {
          option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving); // 仅网络模式
        }
      }

      // 可选，设置场景定位参数，包括签到场景、运动场景、出行场景
      if ((arguments.containsKey("LocationPurpose"))) {
        isPurporseLoc = true;
        if  (((int)arguments.get("LocationPurpose")) == 1) {
          option.setLocationPurpose(LocationClientOption.BDLocationPurpose.SignIn); // 签到场景
        } else if (((int)arguments.get("LocationPurpose")) == 2) {
          option.setLocationPurpose(LocationClientOption.BDLocationPurpose.Transport); // 运动场景
        } else if (((int)arguments.get("LocationPurpose")) == 3) {
          option.setLocationPurpose(LocationClientOption.BDLocationPurpose.Sport); // 出行场景
        }
      } else {
        isPurporseLoc = false;
      }

      // 可选，设置需要返回海拔高度信息
      if (arguments.containsKey("isNeedAltitude")) {
        if (((boolean)arguments.get("isNeedAltitude"))) {
          option.setIsNeedAddress(true);
        } else {
          option.setIsNeedAltitude(false);
        }
      }

      // 可选，设置是否使用gps，默认false
      if (arguments.containsKey("openGps")) {
        if(((boolean)arguments.get("openGps"))) {
          option.setOpenGps(true);
        } else {
          option.setOpenGps(false);
        }
      }

      // 可选，设置是否允许返回逆地理地址信息，默认是true
      if (arguments.containsKey("isNeedLocationDescribe")) {
        if(((boolean)arguments.get("isNeedLocationDescribe"))) {
          option.setIsNeedLocationDescribe(true);
        } else {
          option.setIsNeedLocationDescribe(false);
        }
      }

      // 可选，设置发起定位请求的间隔，int类型，单位ms
      // 如果设置为0，则代表单次定位，即仅定位一次，默认为0
      // 如果设置非0，需设置1000ms以上才有效
      if (arguments.containsKey("scanspan")) {
        option.setScanSpan((int)arguments.get("scanspan"));
      }
      // 可选，设置返回经纬度坐标类型，默认GCJ02
      // GCJ02：国测局坐标；
      // BD09ll：百度经纬度坐标；
      // BD09：百度墨卡托坐标；
      // 海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
      if (arguments.containsKey("coorType")) {
        option.setCoorType((String)arguments.get("coorType"));
      }

      // 设置是否需要返回附近的poi列表
      if (arguments.containsKey("isNeedLocationPoiList")) {
        if (((boolean)arguments.get("isNeedLocationPoiList"))) {
          option.setIsNeedLocationPoiList(true);
        } else {
          option.setIsNeedLocationPoiList(false);
        }
      }
      // 设置是否需要最新版本rgc数据
      if (arguments.containsKey("isNeedNewVersionRgc")) {
        if (((boolean)arguments.get("isNeedNewVersionRgc"))) {
          option.setIsNeedLocationPoiList(true);
        } else {
          option.setIsNeedLocationPoiList(false);
        }
      }
    }
  }

  /**
   * 格式化时间
   *
   * @param time
   * @param strPattern
   * @return
   */
  private String formatUTC(long time, String strPattern) {
    if (TextUtils.isEmpty(strPattern)) {
      strPattern = "yyyy-MM-dd HH:mm:ss";
    }
    SimpleDateFormat sdf = null;
    try {
      sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
      sdf.applyPattern(strPattern);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return sdf == null ? "NULL" : sdf.format(time);
  }
}
