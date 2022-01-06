import 'package:flutter/material.dart';

import 'package:bdmap_flutter_plugin_qxd_java/bdmap_flutter_plugin_qxd_java.dart';

import 'app_data.dart';
import 'map_page.dart';

class Bottom extends StatefulWidget {
  @override
  _BottomState createState() => _BottomState();
}

class _BottomState extends State<Bottom> {
  double _distance = -1.0;
  LatLng _coordinate;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        RaisedButton(
          onPressed: () async {
            _coordinate = await Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => MapPage(center: myLocation)),
            );
            if (_coordinate != null) {
              try {
                _distance = await getDistance(
                    _coordinate.latitude,
                    _coordinate.longitude,
                    authorLocation.latitude,
                    authorLocation.longitude);
              } on Exception catch (e) {
                // TODO
              }
            }
            setState(() {

            });
          },
          child: Text("打开地图"),
        ),
        Text(_coordinate?.toString() ?? "打开地图获取坐标"),
        if (_distance > 0) Text("距离作者${_distance.toString()}米"),
      ],
    );
  }
}
