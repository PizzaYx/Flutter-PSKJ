//library platform_interface;
export 'src/location.dart';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'package:platform_interface/src/location.dart';
import 'package:platform_interface/src/method_channel_bdmap.dart';

abstract class BdmapPlatform {
  static BdmapPlatform instance = MethodChannelBdmap();

  /// Returns a widget displaying the map view
  Widget buildView(
      Map<String, dynamic> creationParams,
      PlatformViewCreatedCallback onPlatformViewCreated) {
    throw UnimplementedError('buildView() has not been implemented.');
  }

  Future<void> init(int mapId) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<LatLng> getLatLng(
      {
        @required int mapId,
      }) {
    throw UnimplementedError('getLatLng() has not been implemented.');
  }

  Future<void> move2mine({@required int mapId}) {
    throw UnimplementedError('move2mine() has not been implemented.');
  }
}

abstract class BaiduLocationPlatform {
  static BaiduLocationPlatform instance = MethodChannelBaiduLocation();

  Future<Map> locate() {
    throw UnimplementedError("locate() has not been implemented");
  }

  Future<double> getDistance (double p1lat, double p1lng, double p2lat, double p2lng) {
    throw UnimplementedError('getDistance() has not been implemented.');
  }

  Future<String> get platformVersion {
    throw UnimplementedError("get platformVersion has not been implemented");
  }
}

/// 新建库自带的
/// A Calculator.
class Calculator {
  /// Returns [value] plus 1.
  int addOne(int value) => value + 1;
}
