package com.webview.filereader;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.ValueCallback;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterX5Plugin
 */
public class FlutterFileReaderPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {

    private int x5LoadStatus = -1; // -1 Unloaded state 5 success 10 failure

    public static final String channelName = "wv.io/FileReader";
    private Context ctx;
    private MethodChannel methodChannel;
    private NetBroadcastReceiver netBroadcastReceiver;
    private FlutterPluginBinding pluginBinding;
    private QbSdkPreInitCallback preInitCallback;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                if (methodChannel != null) {
                    methodChannel.invokeMethod("onLoad", isLoadX5());
                }
            }
            return false;
        }
    });


    private void init(Context context, BinaryMessenger messenger) {
        ctx = context;
        methodChannel = new MethodChannel(messenger, channelName);
        methodChannel.setMethodCallHandler(this);
        // initX5(context);
        netBroadcastRegister(context);
    }

    public FlutterFileReaderPlugin() {

    }

    private void onDestory() {
        Log.e("FileReader", "destroy");
        if (netBroadcastReceiver != null && ctx != null) {
            ctx.unregisterReceiver(netBroadcastReceiver);
        }
        preInitCallback = null;
        ctx = null;
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler = null;
        methodChannel = null;
        pluginBinding = null;
    }


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        FlutterFileReaderPlugin plugin = new FlutterFileReaderPlugin();
        plugin.init(registrar.context(), registrar.messenger());
        registrar.platformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(registrar.messenger(), registrar.activity(), plugin));
    }


    public void netBroadcastRegister(final Context context) {
        //Instantiate the IntentFilter object
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver(new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                // -1 No internet
                if (x5LoadStatus != 5 && status != -1) {
                    initX5(context);
                }
            }
        });
        //Register for broadcast reception
        context.registerReceiver(netBroadcastReceiver, filter);


    }


    public void initX5(final Context context) {
        Log.e("FileReader", "Initialize X5");
        if (!QbSdk.canLoadX5(context)) {
            //important
            QbSdk.reset(context);
        }
        preInitCallback = new QbSdkPreInitCallback();
        // Initialize when calling TBS、Make the following configuration before creating WebView
        // To open the optimization plan
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setNeedInitX5FirstTime(true);
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.e("FileReader", "TBS download complete");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.e("FileReader", "TBS installation is complete");

            }

            @Override
            public void onDownloadProgress(int i) {
                Log.e("FileReader", "TBS download progress:" + i);
            }
        });

        QbSdk.initX5Environment(context, preInitCallback);


    }

    @Override
    public void onMethodCall(MethodCall methodCall, final MethodChannel.Result result) {
        if ("isLoad".equals(methodCall.method)) {
            result.success(isLoadX5());
        } else if ("openFileByMiniQb".equals(methodCall.method)) {
            String filePath = (String) methodCall.arguments;
            result.success(openFileByMiniQb(filePath));
        }
    }

    public boolean openFileByMiniQb(String filePath) {
        if (ctx != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("style", "1");
            params.put("local", "false");
            QbSdk.openFileReader(ctx, filePath, params, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d("FileReader", "openFileReader->" + s);
                }
            });
        }
        return true;
    }


    private void onX5LoadComplete() {
        mainHandler.sendEmptyMessage(100);
    }


    int isLoadX5() {

        if (ctx != null && QbSdk.canLoadX5(ctx)) {
            x5LoadStatus = 5;
        }
        return x5LoadStatus;
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        Log.e("FileReader", "onAttachedToEngine");

        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.e("FileReader", "onDetachedFromEngine");
        onDestory();
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        Log.e("FileReader", "onAttachedToActivity");
        init(pluginBinding.getApplicationContext(), pluginBinding.getBinaryMessenger());
        pluginBinding.getPlatformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(pluginBinding.getBinaryMessenger(), binding.getActivity(), this));
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.e("FileReader", "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        Log.e("FileReader", "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.e("FileReader", "onDetachedFromActivity");
    }


    class QbSdkPreInitCallback implements QbSdk.PreInitCallback {

        @Override
        public void onCoreInitFinished() {
            Log.e("FileReader", "TBS kernel initialization ends");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            if (ctx == null) {
                return;
            }
            if (b) {
                x5LoadStatus = 5;
                Log.e("FileReader", "TBS kernel initialized successfully" + "--" + QbSdk.canLoadX5(ctx));
            } else {
                x5LoadStatus = 10;
                resetQbSdkInit();
                Log.e("FileReader", "TBS kernel initialization failed" + "--" + QbSdk.canLoadX5(ctx));
            }
            onX5LoadComplete();
        }
    }


    ///Reflection Reset the initialization state (loading fails when there is no network)
    private void resetQbSdkInit() {
        try {
            Field field = QbSdk.class.getDeclaredField("s");
            field.setAccessible(true);
            field.setBoolean(null, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}

