import 'dart:async';

import 'package:platform_interface/platform_interface.dart';
export 'package:platform_interface/platform_interface.dart' show LatLng;
export 'src/baidu_map.dart';
export 'src/controller.dart';
export 'src/baidu_location.dart';

class BdmapFlutterPluginQxdJava {
  static Future<String> get platformVersion async {
    final String version = await BaiduLocationPlatform.instance.platformVersion;
    return version;
  }
}
