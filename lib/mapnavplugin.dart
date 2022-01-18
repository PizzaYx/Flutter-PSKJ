import 'dart:async';

import 'package:flutter/services.dart';

class Mapnavplugin {
  static const MethodChannel _channel =
      const MethodChannel('plugins.mapnavplugin');

  //设置楼层
  static Future setFloorNum(int floor) async {
    await _channel
        .invokeMethod('switchFloor', <String, dynamic>{'floor': floor});
  }

  //搜索
  static Future<List<String>> setSearchData(String searchData,
      {String floor = '0'}) async {
    final List<String> listData = await _channel.invokeListMethod('searchData',
        <String, dynamic>{'searchData': searchData, 'floor': floor});

    for (int i = 0; i < listData.length; i++) {
      print('返回数据====${listData[i]}');
    }

    return listData;
  }

  //选择搜索内容 导航
  static Future chooseIndex(int index) async {
    await _channel
        .invokeMethod('chooseIndex', <String, dynamic>{'index': index});
  }
}
