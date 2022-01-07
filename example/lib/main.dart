import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:mapnavplugin/mapnavplugin.dart';
import 'package:mapnavplugin/search_bar.dart';
import 'package:material_floating_search_bar/material_floating_search_bar.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: pskjMapPage(),
    );
  }
}

class pskjMapPage extends StatefulWidget {
  const pskjMapPage({Key key}) : super(key: key);

  @override
  _pskjMapPageState createState() => _pskjMapPageState();
}

class _pskjMapPageState extends State<pskjMapPage> {
  //楼层显示宽度
  double floorWidth = 50;

  //总楼层
  int floorSum = 5;

  //当前楼层
  int floorIndex = 1;

  //楼层背景
  Color floorBg = Colors.blue.withOpacity(0.8);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Container(
        width: double.infinity,
        height: double.infinity,
        child: Stack(
          children: [
            //地图显示
            Positioned(
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                child: Platform.isAndroid
                    ? AndroidView(
                        viewType: 'indoorMapView',
                        creationParams: <String, String>{
                          "url": "https://www.baidu.com"
                        }, //向原生传递参数
                        creationParamsCodec: const StandardMessageCodec(),
                        // gestureRecognizers: <Factory<OneSequenceGestureRecognizer>>[
                        //   new Factory<OneSequenceGestureRecognizer>(
                        //     () => new EagerGestureRecognizer(),
                        //   ),
                        // ].toSet(),
                      )
                    : UiKitView(
                        viewType: 'testView',
                      )),
            //切换楼层
            Positioned(left: 10, top: 20, child: switchFloor()),
            //搜索查询
            Positioned(
              child: buildFloatingSearchBar(),
            ),
          ],
        ),
      ),
    );
  }

  Widget buildFloatingSearchBar() {
    final isPortrait =
        MediaQuery.of(context).orientation == Orientation.portrait;
    return FloatingSearchBar(
      hint: '搜索',
      scrollPadding: const EdgeInsets.only(top: 16, bottom: 56),
      transitionDuration: const Duration(milliseconds: 800),
      transitionCurve: Curves.easeInOut,
      physics: const BouncingScrollPhysics(),
      // 0.0 : -1.0,
      axisAlignment: isPortrait ? 0.0 : -1.0,
      openAxisAlignment: 0.0,
      maxWidth: isPortrait ? 600 : 500,
      debounceDelay: const Duration(milliseconds: 500),
      onQueryChanged: (query) {
        // Call your model, bloc, controller here.
      },
      // Specify a custom transition to be used for
      // animating between opened and closed stated.
      transition: CircularFloatingSearchBarTransition(),
      actions: [
        FloatingSearchBarAction(
          showIfOpened: false,
          child: CircularButton(
            icon: const Icon(Icons.search),
            onPressed: () {},
          ),
        ),
        FloatingSearchBarAction.searchToClear(
          showIfClosed: false,
        ),
      ],
      builder: (context, transition) {
        return ClipRRect(
          borderRadius: BorderRadius.circular(8),
          child: Material(
            color: Colors.white,
            elevation: 4.0,
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              Text('1'),
              Text('2'),
              Text('3'),
              Text('4'),
            ]),
          ),
        );
      },
    );
  }

  //切换楼层按钮
  Widget switchFloor() {
    return Container(
      width: floorWidth,
      height: floorWidth * (floorSum + 2),
      decoration: BoxDecoration(
          color: floorBg,
          borderRadius: BorderRadius.vertical(
              top: Radius.circular(25), bottom: Radius.circular(25))),
      child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          mainAxisSize: MainAxisSize.max,
          children: List.generate(floorSum + 2, (index) {
            return GestureDetector(
              child: floorCell(index),
              onTap: () {
                if ((floorIndex == 1 && index == 0) ||
                    (floorIndex == floorSum && index == floorSum + 1)) {
                  return;
                }
                if (index == 0)
                  floorIndex--;
                else if (index == floorSum + 1)
                  floorIndex++;
                else
                  floorIndex = index;
                //
                Mapnavplugin.setFloorNum(floorIndex);
                print(floorIndex);
                setState(() {});
              },
            );
          })),
    );
  }

  Widget floorCell(int floorText) {
    Widget childWidget;
    if (floorText == 0) {
      childWidget = Container(
          width: floorWidth,
          height: floorWidth,
          child: Icon(
            Icons.keyboard_arrow_up,
            color:
                (floorIndex - 1) == floorText ? Colors.white30 : Colors.black,
          ));
    } else if (floorText == floorSum + 1) {
      childWidget = Container(
          width: floorWidth,
          height: floorWidth,
          child: Icon(
            Icons.keyboard_arrow_down,
            color:
                (floorIndex + 1) == floorText ? Colors.white30 : Colors.black,
          ));
    } else
      childWidget = Container(
          width: floorWidth,
          height: floorWidth,
          color: floorIndex == floorText ? Colors.green : Colors.transparent,
          child: Center(child: Text('F' + floorText.toString())));
    return childWidget;
  }
}
