'use strict';

import React, { PropTypes } from 'react';
import ReactNative, { requireNativeComponent, View, StyleSheet } from 'react-native';

var {
    addons: { PureRenderMixin },
    NativeModules: { UIManager, CrosswalkWebViewManager: { JSNavigationScheme } }
} = ReactNative;

var resolveAssetSource = require('react-native/Libraries/Image/resolveAssetSource')

var WEBVIEW_REF = 'crosswalkWebView';

var CrosswalkWebView = React.createClass({
    mixins:    [PureRenderMixin],
    statics:   { JSNavigationScheme },
    propTypes: {
        localhost:               PropTypes.bool.isRequired,
        onNavigationStateChange: PropTypes.func,
        onError:                 PropTypes.func,
        url:                     PropTypes.string,
        injectedJavaScript:      PropTypes.string,
        onBridgeMessage:         PropTypes.func,
        source:                  PropTypes.oneOfType([
          PropTypes.shape({
           uri: PropTypes.string, // uri to load in webview
          }),
          PropTypes.shape({
           html: PropTypes.string, // static html to load in webview
          }),
           PropTypes.number, // used internally by packager
        ]),
        ...View.propTypes
    },
    getDefaultProps () {
        return {
            localhost: false
        };
    },
    componentWillMount: function() {
        const { onBridgeMessage } = this.props
        DeviceEventEmitter.addListener("crosswalkWebViewBridgeMessage", (body) => {
            const { message } = body
            if (onBridgeMessage) {
                onBridgeMessage(message)
            }
        })
    },
    render () {
      var source = this.props.source || {};
      if (this.props.url) {
        source.uri = this.props.url;
      }
      return (
          <NativeCrosswalkWebView
            { ...this.props }
            ref={ WEBVIEW_REF }
            source={resolveAssetSource(source)}
            onNavigationStateChange={ this.onNavigationStateChange }
            onError={ this.onError } />
      );
    },
    getWebViewHandle () {
        return React.findNodeHandle(this.refs[WEBVIEW_REF]);
    },
    onNavigationStateChange (event) {
        var { onNavigationStateChange } = this.props;
        if (onNavigationStateChange) {
            onNavigationStateChange(event.nativeEvent);
        }
    },
    onError (event) {
        var { onError } = this.props;
        if (onError) {
            onError(event.nativeEvent);
        }
    },
    goBack () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.NativeCrosswalkWebView.Commands.goBack,
            null
        );
    },
    goForward () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.NativeCrosswalkWebView.Commands.goForward,
            null
        );
    },
    reload () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.NativeCrosswalkWebView.Commands.reload,
            null
        );
    },
    sendToBridge (message) {
        const strMessage = JSON.stringify(message)
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.CrosswalkWebView.Commands.sendToBridge,
            [strMessage]
        )
    }
});

var NativeCrosswalkWebView = requireNativeComponent('CrosswalkWebView', CrosswalkWebView);

export default CrosswalkWebView;
