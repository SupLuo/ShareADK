package easy.share.wx.model;

/**
 * Created by Lucio on 17/5/12.
 */

public class WxApiResult {

    public int errcode;

    public String errmsg;

    public boolean isSuccess() {
        return errcode == 0;
    }

}