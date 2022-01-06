import 'package:flutter/foundation.dart';

import 'package:platform_interface/platform_interface.dart';

class BaiduMapController {
  /// The mapId for this controller
  final int mapId;

  BaiduMapController._({
    @required this.mapId,
  }) : assert(BdmapPlatform.instance != null);

  /// Initialize control of a [GoogleMap] with [id].
  ///
  /// Mainly for internal use when instantiating a [GoogleMapController] passed
  /// in [GoogleMap.onMapCreated] callback.
  static Future<BaiduMapController> init(int id) async {
    assert(id != null);
    await BdmapPlatform.instance.init(id);
    return BaiduMapController._(
      mapId: id,
    );
  }

  Future<LatLng> getLatLng() {
    return BdmapPlatform.instance.getLatLng(mapId: mapId);
  }

  Future<void> move2mine() {
    return BdmapPlatform.instance.move2mine(mapId: mapId);
  }
}
