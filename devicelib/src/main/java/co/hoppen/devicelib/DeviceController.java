package co.hoppen.devicelib;

import android.hardware.usb.UsbManager;

/**
 * Created by YangJianHui on 2021/4/29.
 */
public class DeviceController implements Controller{
    private McuDevice mcuDevice;

    public void setFloatingView(FloatingView floatingView) {
        mcuDevice.setFloatingView(floatingView);
    }

    public DeviceController(UsbManager usbManager, OnDeviceListener onDeviceListener){
        mcuDevice = new McuDevice(usbManager);
        mcuDevice.setOnDeviceListener(onDeviceListener);
    }

    public McuDevice getMcuDevice() {
        return mcuDevice;
    }

    @Override
    public void close() {
        mcuDevice.closeDevice();
    }

    @Override
    public void selectHandle(String typeCode) {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_HANDLE_SET(typeCode));
    }

    @Override
    public void setHandleStart(int mode, int strength, int time) {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_HANDLE_START(mode,strength,time));
    }

    @Override
    public void setHandleStop(int mode, int strength, int time) {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_HANDLE_STOP(mode,strength,time));
    }

    @Override
    public void setHandleConfig(int mode, int strength, int time) {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_HANDLE_CONFIG(mode,strength,time));
    }

    @Override
    public void enterHandleRate() {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_ENTER_RATE());
    }

    @Override
    public void exitHandleRate() {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_EXIT_RATE());
    }

    @Override
    public void setHandleRate(int data) {
        mcuDevice.sendInstructions(UsbInstructionUtils.USB_SET_RATE(data));
    }

    public void setOnInstructionListener(OnInstructionListener onInstructionListener){
        mcuDevice.setOnInstructionListener(onInstructionListener);
    }

}
