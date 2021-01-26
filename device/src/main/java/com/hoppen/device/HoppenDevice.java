package com.hoppen.device;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;

/**
 * Created by YangJianHui on 2021/1/25.
 */
public abstract class HoppenDevice{

    private UsbManager usbManager;

    private OnDeviceListener onDeviceListener;

    private int productId;

    private int vendorId;

    private String deviceName="";//usb path

    private String productName = "";

    private boolean isOpen = false;

    protected HoppenDevice(DeviceBuilder builder) throws Exception{
        this.productId = builder.productId;
        this.vendorId = builder.vendorId;
        if (builder.onDeviceListener!=null){
            this.onDeviceListener = builder.onDeviceListener;
        }
        if (productId==0||vendorId==0) throw new Exception("productId or vendorId is null");
    }

    public void setOnDeviceListener(OnDeviceListener onDeviceListener) {
        this.onDeviceListener = onDeviceListener;
    }

//    public void setUsbDevice(UsbDevice usbDevice) {
//        this.usbDevice = usbDevice;
//    }

    public OnDeviceListener getOnDeviceListener() {
        return onDeviceListener;
    }

    abstract void onOpen(UsbDevice usbDevice);

    abstract void onClose(UsbDevice usbDevice);

    public void open(UsbDevice usbDevice){
        LogUtils.e("isOpen " + isOpen);
        if (!isOpen){
            onOpen(usbDevice);
            if (onDeviceListener!=null)onDeviceListener.onConnect();
        }
    }

    public void close(UsbDevice usbDevice){
        LogUtils.e("close " + isOpen);
        if (isOpen){
            onClose(usbDevice);
            if (onDeviceListener!=null)onDeviceListener.onDisDisconnect();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getProductId() {
        return productId;
    }

    public int getVendorId() {
        return vendorId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setUsbManager(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    public UsbManager getUsbManager() {
        return usbManager;
    }
}
