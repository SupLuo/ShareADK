package halo.android.integration.alipay;

import android.app.Activity;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Lucio on 17/5/4.
 * 支付宝支付，实现H5转Native支付
 */

public class AliH5PayWebViewClient extends WebViewClient {

    private Activity mActivity;

    public AliH5PayWebViewClient(Activity activity) {
        mActivity = activity;
    }

    @Override
    final public boolean shouldOverrideUrlLoading(final WebView view, String url) {
        AliPay.h5PayUrlIntercept(mActivity, url, new AliH5PayCallback() {
            @Override
            public void onNotAliH5PayResult(String url) {
                shouldOverrideUrlLoadingAfterAliPayCheck(view, url);
            }

            @Override
            public void onPayResult(AliH5PayResult payResult) {
                String returnUrl = null;
                if (payResult != null) {
                    returnUrl = payResult.getReturnUrl();
                }
                if (!TextUtils.isEmpty(returnUrl)) {
                    view.loadUrl(returnUrl);
                }
            }
        });
        return true;
    }

    public boolean shouldOverrideUrlLoadingAfterAliPayCheck(WebView view, String url) {
        if (url.startsWith("http") || url.startsWith("ftp") ) {
            view.loadUrl(url);
        }
        return true;
    }

}
