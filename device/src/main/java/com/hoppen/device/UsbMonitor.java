package com.hoppen.device;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by YangJianHui on 2021/1/19.
 */
public class UsbMonitor implements LifecycleEventObserver {
    private final static String USB_PERMISSION = UsbMonitor.class.getName();
    private AppCompatActivity appCompatActivity;
    private BroadcastReceiver usbReceiver;
    private UsbManager usbManager;
    private List<HoppenDevice> devicesList = new ArrayList<>();

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.equals(Lifecycle.Event.ON_CREATE)){
            requestDeviceList();
        }else if (event.equals(Lifecycle.Event.ON_START)){
            register();
        }else if (event.equals(Lifecycle.Event.ON_STOP)){
            unregister();
        }
    }

    public UsbMonitor(AppCompatActivity activity,List<HoppenDevice> list){
        if (activity!=null){
            this.appCompatActivity = activity;
            activity.getLifecycle().addObserver(this);
            usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        }
        if (list!=null){
            Iterator<HoppenDevice> iterator = list.iterator();
            while (iterator.hasNext()){
                HoppenDevice next = iterator.next();
                if (next!=null){
                    next.setUsbManager(usbManager);
                    this.devicesList.add(next);
                }
            }
        }
    }

    private void register(){
        if (appCompatActivity!=null){
            initUsbReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(USB_PERMISSION);
            appCompatActivity.registerReceiver(usbReceiver,filter);
        }
    }

    private void initUsbReceiver() {
        if (usbReceiver==null){
            usbReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            ||action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            ||action.equals(USB_PERMISSION)){
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        HoppenDevice hoppenDevice = deviceFilter(usbDevice);
                        if (hoppenDevice!=null){

                            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                                requestPermission(usbDevice);

                            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                                hoppenDevice.close(usbDevice);
                            }else if (action.equals(USB_PERMISSION)){
                                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                                    hoppenDevice.open(usbDevice);
                                }else {
                                    requestPermission(usbDevice);
                                }
                            }
                        }
                    }
                }
            };
        }
    }

    private void unregister(){
        if (appCompatActivity!=null){
            if (usbReceiver!=null){
                appCompatActivity.unregisterReceiver(usbReceiver);
            }
        }
    }

    private boolean requestPermission(UsbDevice usbDevice){
        if (usbDevice!=null){
                    if (!usbManager.hasPermission(usbDevice)){
                        PendingIntent pendingIntent =
                                PendingIntent.getBroadcast(appCompatActivity, 0, new Intent(USB_PERMISSION), 0);
                        usbManager.requestPermission(usbDevice,pendingIntent);
                        return true;
                    }
        }
        return false;
    }

    private List<UsbDevice> requestDeviceList(){
        ArrayList<UsbDevice> usbDevices = new ArrayList<>(usbManager.getDeviceList().values());
        Iterator<UsbDevice> iterator = usbDevices.iterator();
        while (iterator.hasNext()){
            UsbDevice next = iterator.next();
            HoppenDevice hoppenDevice = deviceFilter(next);
            if (hoppenDevice==null) {
                usbDevices.remove(next);
            }else {
                boolean need = requestPermission(next);
                if (!need)hoppenDevice.open(next);
            }
        }
        return usbDevices;
    }

    private HoppenDevice deviceFilter(UsbDevice usbDevice){
        Iterator<HoppenDevice> iterator = devicesList.iterator();
        while (iterator.hasNext()){
            HoppenDevice next = iterator.next();
            if (!next.getDeviceName().isEmpty()){
                if (next.getVendorId()==usbDevice.getVendorId()
                        && next.getProductId()==usbDevice.getProductId()){
                    return next;
                }
            }else {
                if (next.getVendorId()==usbDevice.getVendorId()
                        && next.getProductId()==usbDevice.getProductId()){
                    next.setDeviceName(usbDevice.getDeviceName());
                    return next;
                }
            }
        }
        return null;
    }

}
