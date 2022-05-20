package co.hoppen.devicelib;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;

import co.hoppen.devicelib.queue.ConnectMcuDeviceTask;
import co.hoppen.devicelib.queue.TaskQueue;

import static co.hoppen.devicelib.FloatingType.FLOATING_BALL;

/**
 * Created by YangJianHui on 2021/4/29.
 */
public class HoppenDeviceHelper implements LifecycleEventObserver,OnUsbStatusListener {
    private UsbMonitor usbMonitor;
    private AppCompatActivity appCompatActivity;
    private DeviceController controller;
    private FloatingView floatingView;
    private TaskQueue taskQueue;

    private HoppenDeviceHelper(AppCompatActivity activity,OnDeviceListener onDeviceListener,boolean debugWindow){
        if (activity!=null){
            this.appCompatActivity = activity;
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            usbMonitor = new UsbMonitor(usbManager,this);
            if (onDeviceListener==null && activity instanceof OnDeviceListener) onDeviceListener = (OnDeviceListener) activity;
            controller = new DeviceController(usbManager,onDeviceListener);
            appCompatActivity.getLifecycle().addObserver(this);
            if (debugWindow){
                floatingView = FloatingBuilder.create(FLOATING_BALL.getClassName(), LayoutInflater.from(activity));
                controller.setFloatingView(floatingView);
            }
            taskQueue = new TaskQueue();
        }
    }

    private HoppenDeviceHelper(AppCompatActivity activity,boolean debugWindow){
        this(activity,null,debugWindow);
    }

    private DeviceController getController() {
        return controller;
    }

    public static DeviceController createController(AppCompatActivity activity,boolean debugWindow,OnDeviceListener onDeviceListener){
        return new HoppenDeviceHelper(activity,onDeviceListener,debugWindow).getController();
    }

    public static DeviceController createController(AppCompatActivity activity,boolean debugWindow){
        return new HoppenDeviceHelper(activity,debugWindow).getController();
    }

    public static DeviceController createController(AppCompatActivity activity){
        return new HoppenDeviceHelper(activity,false).getController();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.equals(Lifecycle.Event.ON_CREATE)){
            if (floatingView!=null)floatingView.show();
            usbMonitor.requestDeviceList(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_START)){
            usbMonitor.register(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_STOP)){
            if (floatingView!=null)floatingView.gone();
            usbMonitor.unregister(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
            if (floatingView!=null)floatingView.dismiss();
            controller.close();
        }
    }

    @Override
    public void onConnecting(UsbDevice usbDevice, DeviceType type) {
        if (controller!=null){
            if (type == DeviceType.MCU){
                controller.getMcuDevice().onConnecting(usbDevice,type);
                    ConnectMcuDeviceTask connectMcuDeviceTask =
                            new ConnectMcuDeviceTask((UsbManager) appCompatActivity.getSystemService(Context.USB_SERVICE),usbDevice);
                    taskQueue.addTask(connectMcuDeviceTask, new TaskQueue.currentTaskFinish() {
                        @Override
                        public void onFinish() {
                            ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo = connectMcuDeviceTask.getConnectMcuInfo();
                            if (connectMcuInfo.isConform()){
                                controller.getMcuDevice().onConnecting(connectMcuInfo);
                            }
                        }
                    });
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
