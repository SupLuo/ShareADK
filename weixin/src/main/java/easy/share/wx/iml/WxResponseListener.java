package easy.share.wx.iml;

import android.app.Activity;

/**
 * Created by Lucio on 17/5/12.
 */

interface WxResponseListener {

    /**
     * @param activity WxCallBackDelegate中包含的activity，通常指包名wxapi目录下定义的微信所需的activity
     * @param code
     * @param msg
     */
    void onWxCallBackError(Activity activity, int code, String msg);
}
