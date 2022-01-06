
import 'dart:async';

import 'package:flutter/services.dart';

class Mapnavplugin {
  static const MethodChannel _channel =
      const MethodChannel('plugins.mapnavplugin');

  static Future setFloorNum(int floor) async {
    final String version = await _channel.invokeMethod('switchFloor',<String,dynamic>{'floor':floor});

  }
}
