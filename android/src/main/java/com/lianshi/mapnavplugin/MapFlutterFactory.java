package com.lianshi.mapnavplugin;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class MapFlutterFactory extends PlatformViewFactory {

    private final Activity m_activity;
    private final BinaryMessenger m_messenger;

    public MapFlutterFactory(BinaryMessenger binaryMessenger, Activity activity) {
        super(StandardMessageCodec.INSTANCE);
        this.m_activity = activity;
        this.m_messenger = binaryMessenger;
    }

    /**
     * 创建PlatformView
     *
     * @param context 上下文
     * @param viewId  视图的id
     * @param args    flutter端传回的参数
     * @return 返回一个PlatformView的实现类
     */

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new MapFlutterView(context,m_activity,m_messenger,args);
    }
}
