package easy.share.alipay;

import android.app.Activity;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;

/**
 * Created by Lucio on 17/5/4.
 */
public final class AliPay {

    /**
     * 支付宝v1 版本支付
     * (如支付遇到 code ＝ 4000，服务器繁忙类似的错误，可以尝试在异步中调用此方法，如果还有错误则为其他原因)
     * 强烈建议商户直接依赖服务端的异步通知，忽略同步返回
     *
     * @param activity
     * @param orderInfo 必须来源于服务器， app支付请求参数字符串，主要包含商户的订单信息，key=value形式，以&#38;#38;连接。
     * @return <p>同步结果，https://doc.open.alipay.com/doc2/detail.htm?spm=0.0.0.0.xdvAU6&#38;#38;treeId=59&#38;#38;articleId=103665&#38;#38;docType=1</p>
     */
    @Deprecated
    public static AliPayResult payV1(Activity activity, String orderInfo) {
        return payV1(activity, orderInfo, true);
    }

    /**
     * 支付宝v1 版本支付
     * (如支付遇到 code ＝ 4000，服务器繁忙类似的错误，可以尝试在异步中调用此方法，如果还有错误则为其他原因)
     * 强烈建议商户直接依赖服务端的异步通知，忽略同步返回
     *
     * @param activity
     * @param orderInfo   必须来源于服务器， app支付请求参数字符串，主要包含商户的订单信息，key=value形式，以&#38;连接。
     * @param showLoading
     * @return 同步结果，https://doc.open.alipay.com/doc2/detail.htm?spm=0.0.0.0.xdvAU6&#38;treeId=59&#38;articleId=103665&#38;docType=1
     */
    @Deprecated
    public static AliPayResult payV1(Activity activity, String orderInfo, boolean showLoading) {
        PayTask payTask = new PayTask(activity);
        return AliPayResult.wrap(payTask.pay(orderInfo, showLoading));
    }

    /**
     * 支付宝v2 版本支付
     * (如支付遇到 code ＝ 4000，服务器繁忙类似的错误，可以尝试在异步中调用此方法，如果还有错误则为其他原因)
     * 商户可以将同步结果仅仅作为一个支付结束的通知（忽略执行校验），实际支付是否成功，完全依赖服务端异步通知。
     * 需要异步调用
     *
     * @param activity
     * @param orderInfo 必须来源于服务器， app支付请求参数字符串，主要包含商户的订单信息，key=value形式，以&#38;连接。
     * @return
     */
    public static AliPayResult payV2(Activity activity, String orderInfo) {
        return payV2(activity, orderInfo, true);
    }

    /**
     * 支付宝v2 版本支付
     * (如支付遇到 code ＝ 4000，服务器繁忙类似的错误，可以尝试在异步中调用此方法，如果还有错误则为其他原因)
     * 商户可以将同步结果仅仅作为一个支付结束的通知（忽略执行校验），实际支付是否成功，完全依赖服务端异步通知。
     *
     * @param activity
     * @param orderInfo   必须来源于服务器， app支付请求参数字符串，主要包含商户的订单信息，key=value形式，以&#38;连接。
     * @param showLoading 用户在商户app内部点击付款，是否需要一个loading做为在钱包唤起之前的过渡，
     *                    这个值设置为true，将会在调用pay接口的时候直接唤起一个loading，
     *                    直到唤起H5支付页面或者唤起外部的钱包付款页面loading才消失。（
     *                    建议将该值设置为true，优化点击付款到支付唤起支付页面的过渡过程。）
     * @return App支付同步通知参数
     * https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.xN1NnL&#38;treeId=204&#38;articleId=105302&#38;docType=1
     */
    public static AliPayResult payV2(Activity activity, String orderInfo, boolean showLoading) {
        PayTask payTask = new PayTask(activity);
        return AliPayResult.wrap(payTask.payV2(orderInfo, showLoading));
    }

    /**
     * 拦截支付宝h5支付
     *
     * @param activity
     * @param url      待过滤拦截的 URL
     * @param callback 支付回调
     */
    public static void h5PayUrlIntercept(Activity activity, String url, AliH5PayCallback callback) {
        h5PayUrlIntercept(activity, url, true, callback);
    }

    /**
     * 拦截支付宝h5支付
     *
     * @param activity
     * @param url              待过滤拦截的 URL
     * @param isShowPayLoading 是否出现loading
     * @param callback         支付回调
     */
    public static void h5PayUrlIntercept(final Activity activity, String url, boolean isShowPayLoading, final AliH5PayCallback callback) {
        if (!(url.startsWith("http") || url.startsWith("https"))) {
            callback.onNotAliH5PayResult(url);
            return;
        }

        /**
         * 推荐采用的新的二合一接口(h5PayUrlIntercept),只需调用一次
         */
        final PayTask task = new PayTask(activity);
        boolean isIntercepted = task.payInterceptorWithUrl(url, isShowPayLoading, new H5PayCallback() {
            @Override
            public void onPayResult(final H5PayResultModel result) {
                // 支付结果返回
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPayResult(AliH5PayResult.createFromH5PayResultModel(result));
                    }
                });
            }
        });

        /**
         * 判断是否成功拦截
         * 若成功拦截，则无需继续加载该URL；否则继续加载
         */
        if (!isIntercepted) {
            callback.onNotAliH5PayResult(url);
        }
    }

}
