package co.hoppen.devicelib;

import android.hardware.usb.UsbDevice;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public interface OnUsbStatusListener {

    void onConnecting(UsbDevice usbDevice, DeviceType type);

    void onDisconnect(UsbDevice usbDevice, DeviceType type);

}
