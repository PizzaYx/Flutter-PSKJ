//library platformchannel;
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'package:platform_interface/platform_interface.dart';

class MethodChannelBdmap extends BdmapPlatform{
  // Keep a collection of id -> channel
  // Every method call passes the int mapId
  final Map<int, MethodChannel> _channels = {};

  /// Accesses the MethodChannel associated to the passed mapId.
  MethodChannel channel(int mapId) {
    return _channels[mapId];
  }

  /// This method builds the appropriate platform view where the map
  /// can be rendered.
  /// The `mapId` is passed as a parameter from the framework on the
  /// `onPlatformViewCreated` callback.
  @override
  Widget buildView(
      Map<String, dynamic> creationParams,
      PlatformViewCreatedCallback onPlatformViewCreated) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins.waixingjiandie.xyz/baidu_maps',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'plugins.waixingjiandie.xyz/baidu_maps',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
    return Text(
        '$defaultTargetPlatform is not yet supported by the maps plugin');
  }

  /// Initializes the platform interface with [id].
  ///
  /// This method is called when the plugin is first initialized.
  @override
  Future<void> init(int mapId) {
    MethodChannel channel;
    if (!_channels.containsKey(mapId)) {
      channel = MethodChannel('plugins.waixingjiandie.xyz/baidu_maps_$mapId');
      _channels[mapId] = channel;
      print("chanel[$mapId]已建立");
    }
    return channel.invokeMethod<void>('map#waitForMap');
  }

  @override
  Future<LatLng> getLatLng({@required int mapId}) async {
    final Map latLng = await channel(mapId)
        .invokeMethod<Map>('map#getLatLng');
    return LatLng(latLng["lat"], latLng["lng"]);
  }

  @override
  Future<void> move2mine({@required int mapId}) {
    return channel(mapId).invokeMethod<void>("map#move2mine");
  }
}

class MethodChannelBaiduLocation extends BaiduLocationPlatform {
  static const MethodChannel _locChannel = const MethodChannel('bdmap_view_method');

  @override
  Future<Map<String, Object>> locate() async {
    final Map location = await _locChannel.invokeMethod<Map>("locate", {"coorType": "bd09ll"});
    return location.cast<String, Object>();
  }

  @override
  Future<double> getDistance(double p1lat, double p1lng, double p2lat, double p2lng) {
    return _locChannel
        .invokeMethod<double>("getDistance", [p1lat, p1lng, p2lat, p2lng]);
}

  @override
  Future<String> get platformVersion async {
    final String version = await _locChannel.invokeMethod("getPlatformVersion");
    return version;
  }
}

/// 新建库自带的
/// A Calculator.
class Calculator {
  /// Returns [value] plus 1.
  int addOne(int value) => value + 1;
}
