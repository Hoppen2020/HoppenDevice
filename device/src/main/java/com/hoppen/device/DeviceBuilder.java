package com.hoppen.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;

/**
 * Created by YangJianHui on 2021/1/25.
 */
public abstract class DeviceBuilder<B,T extends HoppenDevice> {
    public int productId;
    public int vendorId;
    public OnDeviceListener onDeviceListener;
    private B b;

    public DeviceBuilder(){
        b = (B) this;
    }

    public B setProductId(int productId) {
        this.productId = productId;
        return b;
    }

    public B setVendorId(int vendorId) {
        this.vendorId = vendorId;
        return b;
    }

    public B setOnDeviceListener(OnDeviceListener onDeviceListener) {
        this.onDeviceListener = onDeviceListener;
        return b;
    }

    public abstract T Build(Context context);

}
