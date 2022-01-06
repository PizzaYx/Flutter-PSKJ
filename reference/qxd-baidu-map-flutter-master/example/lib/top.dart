import 'package:flutter/material.dart';

import 'package:bdmap_flutter_plugin_qxd_java/bdmap_flutter_plugin_qxd_java.dart';
import 'package:flutter/services.dart';

import 'app_data.dart';

class Top extends StatefulWidget {
  @override
  _TopState createState() => _TopState();
}

class _TopState extends State<Top> {
  String _locationDetail;
  double _distance = -1.0;

  bool _isRequestingPermission = false;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        RaisedButton(
          onPressed: () async {
            if (_isRequestingPermission) {
              return;
            }
            _isRequestingPermission = true;
            if (await requestPermission()) {
              _isRequestingPermission = false;
              try {
                Map<String, Object> location = await locate();
                myLocation = LatLng(location["latitude"], location["longitude"]);
                _locationDetail = location["locationDetail"];
              } on PlatformException catch (e) {
                _locationDetail = e.message;
              }
              if (myLocation != null) {
                try {
                  _distance = await getDistance(
                      myLocation.latitude,
                      myLocation.longitude,
                      authorLocation.latitude,
                      authorLocation.longitude);
                } on PlatformException catch (e) {
                  _distance = -1.0;
                }
              }
              setState(() {

              });
            }
            else {
              Scaffold.of(context).showSnackBar(SnackBar(
                content: Text("未获得定位权限1"),
              ));
            }
            _isRequestingPermission = false;
          },
          child: Text("获取位置信息"),
        ),
        Text(myLocation?.toString() ?? "点击上面按钮获取定位"),
        Text(_locationDetail ?? "我是位置描述"),
        if (_distance > 0) Text("距离作者${_distance.toString()}米"),
      ],
    );
  }
}
