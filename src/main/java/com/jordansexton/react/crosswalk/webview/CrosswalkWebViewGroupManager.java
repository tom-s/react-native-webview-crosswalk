package com.jordansexton.react.crosswalk.webview;

import android.app.Activity;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkView;

import javax.annotation.Nullable;
import java.util.Map;

public class CrosswalkWebViewGroupManager extends ViewGroupManager<CrosswalkWebView> {

    public static final int GO_BACK = 1;

    public static final int GO_FORWARD = 2;

    public static final int RELOAD = 3;

    public static final int SEND_TO_BRIDGE = 4;

    @VisibleForTesting
    public static final String REACT_CLASS = "CrosswalkWebView";

    private Activity activity;

    private static final String BLANK_URL = "about:blank";

    public CrosswalkWebViewGroupManager (Activity _activity) {
        activity = _activity;
    }

    @Override
    public String getName () {
        return REACT_CLASS;
    }

    @Override
    public CrosswalkWebView createViewInstance (ThemedReactContext context) {
        CrosswalkWebView crosswalkWebView = new CrosswalkWebView(context, activity);
        context.addLifecycleEventListener(crosswalkWebView);
        return crosswalkWebView;
    }

    @Override
    public void onDropViewInstance(CrosswalkWebView view) {
        super.onDropViewInstance(view);
        ((ThemedReactContext) view.getContext()).removeLifecycleEventListener((CrosswalkWebView) view);
        view.onDestroy();
    }

    @ReactProp(name = "source")
    public void setSource(final CrosswalkWebView view, @Nullable ReadableMap source) {
      if (source != null) {
        if (source.hasKey("html")) {
          final String html = source.getString("html");
          activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
              view.load(null, html);
            }
          });
          return;
        }
        if (source.hasKey("uri")) {
          final String url = source.getString("uri");
          activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
              view.load(url, null);
            }
          });
          return;
        }
      }
      setUrl(view, BLANK_URL);
    }


    @ReactProp(name = "injectedJavaScript")
    public void setInjectedJavaScript (XWalkView view, @Nullable String injectedJavaScript) {
        ((CrosswalkWebView) view).setInjectedJavaScript(injectedJavaScript);
    }

    @ReactProp(name = "url")
    public void setUrl (final CrosswalkWebView view, @Nullable final String url) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                view.load(url, null);
            }
        });
    }

    @ReactProp(name = "localhost")
    public void setLocalhost (CrosswalkWebView view, Boolean localhost) {
        view.setLocalhost(localhost);
    }

    @Override
    public
    @Nullable
    Map<String, Integer> getCommandsMap () {
        return MapBuilder.of(
            "goBack", GO_BACK,
            "goForward", GO_FORWARD,
            "reload", RELOAD,
            "sendToBrigde", SEND_TO_BRIDGE
        );
    }

    @Override
    public void receiveCommand (CrosswalkWebView view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case GO_BACK:
                view.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                break;
            case GO_FORWARD:
                view.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
                break;
            case RELOAD:
                view.reload(XWalkView.RELOAD_NORMAL);
                break;
           case SEND_TO_BRIDGE:
                sendToBridge(view, args.getString(0));
                break;
        }
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants () {
        return MapBuilder.of(
            NavigationStateChangeEvent.EVENT_NAME,
            MapBuilder.of("registrationName", "onNavigationStateChange")
        );
    }

    private void sendToBridge(CrosswalkWebView view, String message) {
        String script = "window.CrosswalkWebViewBridge.onMessage('" + message + "');";
        view.evaluateJavascript(script, null);
    }
}
