import 'dart:async';

import 'package:flutter/material.dart';

import 'package:bdmap_flutter_plugin_qxd_java/bdmap_flutter_plugin_qxd_java.dart';
import 'package:platform_interface/platform_interface.dart';

/// Callback method for when the map is ready to be used.
///
/// Pass to [GoogleMap.onMapCreated] to receive a [GoogleMapController] when the
/// map is created.
typedef void MapCreatedCallback(BaiduMapController controller);

const LatLng defaultTarget = const LatLng(39.914935, 116.403119);
//const LatLng defaultTarget = const LatLng(30.249209, 120.189571);

class BaiduMapView extends StatelessWidget {
  //final Completer<BaiduMapController> _controller = Completer<BaiduMapController>();
  
  /// Callback method for when the map is ready to be used.
  ///
  /// Used to receive a [GoogleMapController] for this [GoogleMap].
  final MapCreatedCallback onMapCreated;
  final LatLng center;

  BaiduMapView({this.onMapCreated, center}) : this.center = center ?? defaultTarget;

  @override
  Widget build(BuildContext context) {
    final Map<String, dynamic> creationParams = <String, dynamic>{}; // 传给平台端的参数
    print(center);
    if (center != null) {
      creationParams["lat"] = center.latitude;
      creationParams["lng"] = center.longitude;
    }

    return BdmapPlatform.instance.buildView(creationParams, onPlatformViewCreated);
  }

  Future<void> onPlatformViewCreated(int id) async {
    print("the platform view has been created [$id]");
    final BaiduMapController controller = await BaiduMapController.init(id);
    //_controller.complete(controller);
    if (onMapCreated != null) {
      onMapCreated(controller);
    }
  }
}
