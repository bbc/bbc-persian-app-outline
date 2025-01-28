package org.getoutline.pwa

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.getcapacitor.BridgeActivity
import mobileproxy.Mobileproxy
import mobileproxy.Proxy

// TODO: resize webview so the web content is not occluded by the device UI
class MainActivity : BridgeActivity() {
    private var proxy: Proxy? = null;
    private val homePageUrl: String = "https://www.bbc.com/persian"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bridge.webView.settings.builtInZoomControls = true

        onBackPressedDispatcher.addCallback {
            Log.d("talal", "current URL: ${bridge.webView.url}")
            if (bridge.webView.url == homePageUrl) {
                finishAffinity()
            } else if (bridge.webView.canGoBack()) {
                bridge.webView.goBack()
            } else {
                bridge.webView.loadUrl(homePageUrl)
                bridge.webView.clearHistory()
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            this.proxy = Mobileproxy.runProxy(
                "127.0.0.1:0",
                Mobileproxy.newSmartStreamDialer(
                    Mobileproxy.newListFromLines("www.bbc.com"),
                    "{\"dns\":[{\"https\":{\"name\":\"9.9.9.9\"}}],\"tls\":[\"\",\"split:1\",\"split:2\",\"tlsfrag:1\"]}",
                    Mobileproxy.newStderrLogWriter()
                )
            )

            // NOTE: this affects all requests in the application
            ProxyController.getInstance()
                .setProxyOverride(
                    ProxyConfig.Builder()
                        .addProxyRule(this.proxy!!.address())
                        .build(),
                    {
                        runOnUiThread {
                            // Capacitor does not expose a way to defer the loading of the webview,
                            // so we simply refresh the page
                            this.bridge.webView.reload()

                        }
                    },
                    {}
                )
        }
    }

    override fun onDestroy() {
        this.proxy?.stop(3000)
        this.proxy = null

        super.onDestroy()
    }

    private fun reloadWebView() {
        bridge.webView.reload()
    }
}