package kr.co.oktax

import android.os.Bundle


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
import androidx.webkit.WebViewFeature
import kr.co.oktax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Creating the custom WebView Client Class

    // Invokes native android sharing
    private fun invokeShareIntent(message: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(this@MainActivity, shareIntent, null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val jsObjName = "jsObject"
        val allowedOriginRules = setOf("https://oktax.co.kr")

        // Configuring Dark Theme
        // *NOTE* : The force dark setting is not persistent. You must call the static
        // method every time your app process is started.
        // *NOTE* : The change from day<->night mode is a
        // configuration change so by default the activity will be restarted
        // (and pickup the new values to apply the theme). Take care when overriding this
        //  default behavior to ensure this method is still called when changes are made.
        val nightModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Check if the system is set to light or dark mode
        if (nightModeFlag == Configuration.UI_MODE_NIGHT_YES) {
            // Switch WebView to dark mode; uses default dark theme
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(
                    binding.webview.settings,
                    WebSettingsCompat.FORCE_DARK_ON
                )
            }

            /* Set how WebView content should be darkened. There are three options for how to darken
             * a WebView.
             * PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING- checks for the "color-scheme" <meta> tag.
             * If present, it uses media queries. If absent, it applies user-agent (automatic)
             * darkening DARK_STRATEGY_WEB_THEME_DARKENING_ONLY - uses media queries always, even
             * if there's no "color-scheme" <meta> tag present.
             * DARK_STRATEGY_USER_AGENT_DARKENING_ONLY - it ignores web page theme and always
             * applies user-agent (automatic) darkening.
             * More information about Force Dark Strategy can be found here:
             * https://developer.android.com/reference/androidx/webkit/WebSettingsCompat#setForceDarkStrategy(android.webkit.WebSettings,%20int)
             */
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                WebSettingsCompat.setForceDarkStrategy(
                    binding.webview.settings,
                    DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
                )
            }
        }


        // Set clients
        binding.webview.webViewClient = WebViewClient()
        binding.webview.setWebChromeClient(WebChromeClient())

        // Set Title
        title = getString(R.string.app_name)

        // Setup debugging; See https://developers.google.com/web/tools/chrome-devtools/remote-debugging/webviews for reference
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // Enable Javascript
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true

        // Create a JS object to be injected into frames; Determines if WebMessageListener
        // or WebAppInterface should be used
        createJsObject(
            binding.webview,
            jsObjName,
            allowedOriginRules
        ) { message -> invokeShareIntent(message) }

        // Load the content
        binding.webview.loadUrl("https://oktax.co.kr")
    }
}