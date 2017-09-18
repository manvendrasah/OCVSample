package com.xseed.ocvsample.ocvsample;


/**
 * Created by Manvendra Sah on 17/06/17.
 */

public class FrameModel {

    private byte[] data;
    private int previewWidth;
    private int previewHeight;
    private int picFormat;
    private int frameCount;
    private int rotation;

    public FrameModel(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void releaseData() {
        data = null;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

}
