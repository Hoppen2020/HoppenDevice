package co.hoppen.devicelib;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Created by YangJianHui on 2021/4/29.
 */
public class HoppenDeviceHelper implements LifecycleEventObserver,OnUsbStatusListener {
    private UsbMonitor usbMonitor;
    private AppCompatActivity appCompatActivity;
    private DeviceController controller;

    private HoppenDeviceHelper(AppCompatActivity activity,OnDeviceListener onDeviceListener){
        if (activity!=null){
            this.appCompatActivity = activity;
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            usbMonitor = new UsbMonitor(usbManager,this);
            controller = new DeviceController(usbManager,onDeviceListener);
            appCompatActivity.getLifecycle().addObserver(this);
        }
    }
    private DeviceController getController() {
        return controller;
    }

    public static DeviceController createController(AppCompatActivity activity,OnDeviceListener onDeviceListener){
        return new HoppenDeviceHelper(activity,onDeviceListener).getController();
    }


    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.equals(Lifecycle.Event.ON_CREATE)){
            usbMonitor.requestDeviceList(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_START)){
            usbMonitor.register(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_STOP)){
            usbMonitor.unregister(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
            controller.close();
        }
    }

    @Override
    public void onConnecting(UsbDevice usbDevice, DeviceType type) {
        if (controller!=null){
            if (type == DeviceType.MCU){
                controller.getMcuDevice().onConnecting(usbDevice,type);
            }
        }
    }

    @Override
    public void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (controller!=null){
            if (type == DeviceType.MCU){
                controller.getMcuDevice().onDisconnect(usbDevice,type);
            }
        }
    }
}
