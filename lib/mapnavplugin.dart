
import 'dart:async';

import 'package:flutter/services.dart';

class Mapnavplugin {
  static const MethodChannel _channel =
      const MethodChannel('plugins.mapnavplugin');

  //设置楼层
  static Future setFloorNum(int floor) async {
    await _channel.invokeMethod('switchFloor',<String,dynamic>{'floor':floor});
  }
}
