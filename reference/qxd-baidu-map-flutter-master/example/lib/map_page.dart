import 'package:flutter/material.dart';

import 'package:bdmap_flutter_plugin_qxd_java/bdmap_flutter_plugin_qxd_java.dart';

class MapPage extends StatefulWidget {
  final LatLng center;

  MapPage({this.center});

  @override
  _MapPageState createState() => _MapPageState();
}

class _MapPageState extends State<MapPage> {
  BaiduMapController _controller;
  bool _isMapCreated = false;
  bool _isRequestingPermission = false;


  @override
  Widget build(BuildContext context) {
    List<Widget> actions = <Widget>[];

    if (_isMapCreated) {
      actions.add(
          Builder(
            builder: (context) {
              return IconButton(
                icon: Icon(Icons.my_location),
                onPressed: () async {
                  if (_isRequestingPermission) {
                    return;
                  }
                  _isRequestingPermission = true;
                  if (await requestPermission()) {
                    _controller.move2mine();
                  }
                  else {
                    Scaffold.of(context).showSnackBar(SnackBar(
                      content: Text("未获得定位权限2"),
                    ));
                  }
                  _isRequestingPermission = false;
                },
              );
            },
          )
      );
      actions.add(
          IconButton(
            icon: Icon(Icons.done),
            onPressed: () async {
              final LatLng latLng = await _controller.getLatLng();
              Navigator.pop(context, latLng);
            },
          )
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text("设置位置坐标"),
        actions: actions,
      ),
      body: Stack(
        alignment: AlignmentDirectional.center,
        children: <Widget>[
          BaiduMapView(
              onMapCreated: onMapCreated,
              center: widget.center
          ),
          IgnorePointer(
            child: Icon(Icons.location_on),
          ),
          //Image.asset("assets/location.png"),
        ],
      ),
    );
  }

  void onMapCreated(BaiduMapController controller) {
    print("[MapPage] onMapCreated");
    setState(() {
      _controller = controller;
      _isMapCreated = true;
    });
  }
}
