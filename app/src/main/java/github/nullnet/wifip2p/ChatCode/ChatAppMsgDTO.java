package github.nullnet.wifip2p.ChatCode;

import android.net.wifi.p2p.WifiP2pDevice;

public class ChatAppMsgDTO {

    public final static String MSG_TYPE_SENT = "MSG_TYPE_SENT";
    public final static String MSG_TYPE_RECEIVED = "MSG_TYPE_RECEIVED";
    //Image Url
    private String ImageUrl;

    // Message content.
    private String msgContent;
    // Message type.
    private String msgType;
    // Wifip2p device
    boolean IsImage;
    private WifiP2pDevice device;
    public ChatAppMsgDTO(String msgType, String msgContent, WifiP2pDevice device) {
        this.msgType = msgType;
        this.msgContent = msgContent;
        this.device = device;
        this.IsImage = false;
    }
    public ChatAppMsgDTO(String msgType, String msgContent, WifiP2pDevice device,boolean IsImage) {
        this.msgType = msgType;
        this.msgContent = msgContent;
        this.device = device;
        this.IsImage = IsImage;
    }
    public String getMsgContent() {
        return msgContent;
    }
    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }
    public String getMsgType() {
        return msgType;
    }
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    public String getDeviceName(){return device.deviceName;}
}
