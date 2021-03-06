package andmy.integration.wx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import andmy.integration.wx.iml.WxShareResponseListener;

import static andmy.integration.wx.WxShare.ShareTo.FAVORITE;
import static andmy.integration.wx.WxShare.ShareTo.SESSION;
import static andmy.integration.wx.WxShare.ShareTo.TIME_LINE;

/**
 * Created by Lucio on 17/5/10.
 * 微信分享
 */
public class WxShare {

    /**
     * 缩略图大小
     */
    private static final int THUMB_SIZE = 150;

    /**
     * 分享目标类别
     */
    @IntDef({TIME_LINE, FAVORITE, SESSION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareTo {
        /**
         * 朋友圈
         */
        int TIME_LINE = SendMessageToWX.Req.WXSceneTimeline;

        /**
         * 收藏
         */
        int FAVORITE = SendMessageToWX.Req.WXSceneFavorite;

        /**
         * 好友会话
         */
        int SESSION = SendMessageToWX.Req.WXSceneSession;
    }

    /**
     * 添加分享监听
     *
     * @param listener
     */
    public static void addShareListener(WxShareResponseListener listener) {
        WxCallBackDelegate.CallBackHolder.getInstance().addShareListener(listener);
    }

    /**
     * 移除分享监听
     *
     * @param listener
     */
    public static void removeShareListener(WxShareResponseListener listener) {
        WxCallBackDelegate.CallBackHolder.getInstance().removeShareListener(listener);
    }

    /**
     * 生成唯一标识
     *
     * @param type
     * @return
     */
    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 创建分享消息
     *
     * @param mediaObject
     * @param title
     * @param desc
     * @return
     */
    private static WXMediaMessage buildMediaMessage(final WXMediaMessage.IMediaObject mediaObject, final String title, final String desc) {
        WXMediaMessage msg = new WXMediaMessage(mediaObject);
        msg.title = title;
        msg.description = desc;
        return msg;
    }

    /**
     * 创建消息
     *
     * @param mediaObject
     * @param title
     * @param desc
     * @param thumbData
     * @return
     */
    private static WXMediaMessage buildMediaMessage(final WXMediaMessage.IMediaObject mediaObject, final String title, final String desc, final byte[] thumbData) {
        WXMediaMessage msg = buildMediaMessage(mediaObject, title, desc);
        msg.thumbData = thumbData;
        return msg;
    }

    private static WXMediaMessage buildMediaMessage(final WXMediaMessage.IMediaObject mediaObject, final String title, final String desc, final Bitmap thumbBmp, final boolean needRecycleThumb) {
        WXMediaMessage msg = buildMediaMessage(mediaObject, title, desc);
        msg.setThumbImage(thumbBmp);
        if (needRecycleThumb && thumbBmp != null && !thumbBmp.isRecycled()) {
            thumbBmp.recycle();
        }
        return msg;
    }

    /**
     * 创建请求
     *
     * @param type
     * @param shareTo
     * @param msg
     * @return
     */
    private static SendMessageToWX.Req buildReq(String type, @ShareTo int shareTo, WXMediaMessage msg) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //唯一标志一个请求
        req.transaction = buildTransaction(type); //
        req.message = msg;
        req.scene = shareTo;
        return req;
    }


    /**
     * 分享文本
     *
     * @param text    文本内容
     * @param shareTo 分享到
     */
    public static SendMessageToWX.Req buildTextShareReq(String text, @ShareTo int shareTo) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        WXMediaMessage msg = buildMediaMessage(textObj, null, text);// new WXMediaMessage();
        return buildReq("text", shareTo, msg);
    }

    /**
     * @param context 上下文
     * @param imageId drawable 资源id
     * @param shareTo 分享到
     * @return
     */
    public static SendMessageToWX.Req buildImageShareReq(Context context, @DrawableRes int imageId, @ShareTo int shareTo) {
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), imageId);
        return buildImageShareReq(bmp, shareTo);
    }

    /**
     * @param bmp     bitmap
     * @param shareTo 分享到
     * @param limitThumbSize  限制的缩略图大小
     * @return
     */
    public static SendMessageToWX.Req buildImageShareReq(Bitmap bmp, @ShareTo int shareTo,int limitThumbSize) {
        WXImageObject imgObj = new WXImageObject(bmp);
        Bitmap thumbBmp = null;
        boolean needRecycle = false;
        if (bmp.getWidth() > THUMB_SIZE || bmp.getHeight() > THUMB_SIZE) {
            thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            needRecycle = true;
        } else {
            thumbBmp = bmp;
        }
        return buildImageShareReq(imgObj, shareTo, thumbBmp, needRecycle);
    }

    /**
     * 创建图片分享（缩略图限制150大小，如需改变缩略图大小，见重构方法）
     * @param bmp     bitmap
     * @param shareTo 分享到
     * @return
     */
    public static SendMessageToWX.Req buildImageShareReq(Bitmap bmp, @ShareTo int shareTo) {
        return buildImageShareReq(bmp, shareTo,THUMB_SIZE);
    }

    /**
     * @param imagePath 图片路径（本地文件路径或网络图片url）
     * @param shareTo   分享到
     * @return
     */
    public static SendMessageToWX.Req buildImageShareReq(String imagePath, @ShareTo int shareTo, byte[] thumbData) {
        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(imagePath);
        return buildImageShareReq(imgObj, shareTo, thumbData);
    }


    /**
     * @param imageData 图片数据
     * @param shareTo   分享到
     * @param thumbData 缩略图数据
     * @return
     */
    public static SendMessageToWX.Req buildImageShareReq(byte[] imageData, @ShareTo int shareTo, byte[] thumbData) {
        WXImageObject imgObj = new WXImageObject(imageData);
        return buildImageShareReq(imgObj, shareTo, thumbData);
    }

    /**
     * @param imgObj    imageObj
     * @param shareTo   分享到
     * @param thumbData 缩略图
     * @return
     */
    private static SendMessageToWX.Req buildImageShareReq(WXImageObject imgObj, @ShareTo int shareTo, byte[] thumbData) {
        WXMediaMessage msg = buildMediaMessage(imgObj, null, null, thumbData);
        return buildReq("img", shareTo, msg);
    }

    private static SendMessageToWX.Req buildImageShareReq(WXImageObject imgObj, @ShareTo int shareTo, Bitmap thumbBmp, boolean needRecycleThumbBmp) {
        WXMediaMessage msg = buildMediaMessage(imgObj, null, null, thumbBmp, needRecycleThumbBmp);
        return buildReq("img", shareTo, msg);
    }

    /**
     * 分享音乐
     *
     * @param musicUrl  音乐链接
     * @param title     标题
     * @param desc      描述
     * @param thumbData 缩略图
     * @param shareTo   分享到
     * @return
     */
    public static SendMessageToWX.Req buildMusicShareReq(String musicUrl, String title, String desc, byte[] thumbData, @ShareTo int shareTo) {
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = musicUrl;
        WXMediaMessage msg = buildMediaMessage(music, title, desc, thumbData);
        return buildReq("music", shareTo, msg);
    }

    public static SendMessageToWX.Req buildMusicShareReq(String musicUrl, String title, String desc, Bitmap thumbData, boolean needRecycleThumbBmp, @ShareTo int shareTo) {
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = musicUrl;
        WXMediaMessage msg = buildMediaMessage(music, title, desc, thumbData, needRecycleThumbBmp);
        return buildReq("music", shareTo, msg);
    }

    public static SendMessageToWX.Req buildVideoShareReq(String videoUrl, String title, String desc, @ShareTo int shareTo) {
        return buildVideoShareReq(videoUrl, title, desc, null, shareTo);
    }

    /**
     * 分享视频
     *
     * @param videoUrl
     * @param title
     * @param desc
     * @param thumbData
     * @param shareTo
     * @return
     */
    public static SendMessageToWX.Req buildVideoShareReq(String videoUrl, String title, String desc,@Nullable byte[] thumbData, @ShareTo int shareTo) {
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = videoUrl;

        WXMediaMessage msg = buildMediaMessage(video, title, desc, thumbData);
        return buildReq("video", shareTo, msg);
    }

    public static SendMessageToWX.Req buildVideoShareReq(String videoUrl, String title, String desc, Bitmap thumbData, boolean needRecycleThumbBmp, @ShareTo int shareTo) {
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = videoUrl;

        WXMediaMessage msg = buildMediaMessage(video, title, desc, thumbData, needRecycleThumbBmp);
        return buildReq("video", shareTo, msg);
    }

    public static SendMessageToWX.Req buildWebPageShare(String webPageUrl, String title, String desc, byte[] thumbData, @ShareTo int shareTo) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = webPageUrl;
        WXMediaMessage msg = buildMediaMessage(webpage, title, desc, thumbData);
        return buildReq("webpage", shareTo, msg);
    }

    public static SendMessageToWX.Req buildWebPageShare(String webPageUrl, String title, String desc, Bitmap thumbData, boolean needRecycleThumbBmp, @ShareTo int shareTo) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = webPageUrl;
        WXMediaMessage msg = buildMediaMessage(webpage, title, desc, thumbData, needRecycleThumbBmp);
        return buildReq("webpage", shareTo, msg);
    }

    /**
     * 创建小程序参数
     * @param webPageUrl 低于6.5.6的将打开该网页
     * @param userName 小程序的原始Id
     * @param path 小程序的path
     * @return
     */
    private static WXMiniProgramObject buildMiniProgramMsg(String webPageUrl, String userName, String path) {
        WXMiniProgramObject miniProgram = new WXMiniProgramObject();
        miniProgram.webpageUrl = webPageUrl;
        miniProgram.userName = userName;
        miniProgram.path = path;
        return miniProgram;
    }

    /**
     * 分享小程序
     * 要求发起分享的App与小程序属于同一微信开放平台帐号。
     * 目前仅支持分享小程序类型消息至会话。
     * 若客户端版本低于6.5.6，小程序类型分享将自动转成网页类型分享。开发者必须填写网页链接字段，确保低版本客户端能正常打开网页链接。
     * 小程序的原始ID获取方法：登录小程序后台-设置-基本设置-帐号信息
     *
     * @param webPageUrl 低于6.5.6的将打开该网页
     * @param userName   小程序的原始Id
     * @param path       小程序的path
     * @return
     */
    public static SendMessageToWX.Req buildMiniProgramShare(String webPageUrl, String userName, String path, String title, String desc, byte[] thumbData,@ShareTo int shareTo) {
        WXMiniProgramObject miniProgramObject = buildMiniProgramMsg(webPageUrl, userName, path);
        WXMediaMessage msg = buildMediaMessage(miniProgramObject, title, desc, thumbData);
        return buildReq("webpage", shareTo, msg);
    }

    public static SendMessageToWX.Req buildMiniProgramShare(String webPageUrl, String userName, String path, String title, String desc, Bitmap thumbData, boolean needRecycleThumbBmp,@ShareTo int shareTo) {
        WXMiniProgramObject miniProgramObject = buildMiniProgramMsg(webPageUrl, userName, path);
        WXMediaMessage msg = buildMediaMessage(miniProgramObject, title, desc, thumbData, needRecycleThumbBmp);
        return buildReq("webpage", shareTo, msg);
    }

    /**
     * 发送请求
     * @param context
     * @param appId
     * @param req
     * @return true 请求已发送
     * @throws WxNotInstalledException 微信未安装
     */
    public static boolean senReq(Context context, String appId, BaseReq req) throws WxNotInstalledException {
        return Wx.senReq(context, appId, req);
    }
}
