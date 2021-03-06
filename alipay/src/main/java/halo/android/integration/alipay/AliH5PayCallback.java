package halo.android.integration.alipay;

/**
 * Created by Lucio on 17/12/21.
 */
public interface AliH5PayCallback {

    /**
     * 不是支付宝 h5支付地址
     *
     * @param url 原url地址
     */
    void onNotAliH5PayResult(String url);

    void onPayResult(AliH5PayResult payResult);
}
