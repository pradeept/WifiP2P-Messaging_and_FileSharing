package github.nullnet.wifip2p.ChatCode;

import java.io.Serializable;

public class Content implements Serializable {
    private String type;
    private  String message;
    private byte[] data;
    private String fileName;



    public Content(){}
    public Content(String type, String message, byte[] data, String fileName) {
        this.type = type;
        this.message = message;
        this.data = data;
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
