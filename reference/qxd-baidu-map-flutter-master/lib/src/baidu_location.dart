import 'package:flutter/foundation.dart';
import 'package:permission_handler/permission_handler.dart';

import 'package:platform_interface/platform_interface.dart';

Future<bool> requestPermission() async {
  try {
    return Permission.location.request().isGranted;
  } on Exception catch (e) {
    return false;
  }
}

Future<Map<String, Object>> locate() {
  return BaiduLocationPlatform.instance.locate();
}

Future<double> getDistance(double p1lat, double p1lng, double p2lat, double p2lng) {
  return BaiduLocationPlatform.instance.getDistance(p1lat, p1lng, p2lat, p2lng);
}
