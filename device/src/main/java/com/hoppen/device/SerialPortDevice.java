package com.hoppen.device;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Created by YangJianHui on 2021/1/25.
 */
public class SerialPortDevice extends HoppenDevice{

    private final static int BAUD_RATE = 9600;
    private final static int DATA_BITS = 8;
    private final static int STOP_BITS = 1;
    private final static int PARITY = 0;
    private final static int MAX_READ_BYTES = 128;


    public int baudRate = BAUD_RATE;
    public int dataBits = DATA_BITS;
    public int stopBits = STOP_BITS;
    public int parity = PARITY;
    public int maxReadBytes = MAX_READ_BYTES;

    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint epOut, epIn;

    private WeakReference<Context> weakReference;

    private Thread readDataThread;


    private Runnable readDataRunnable = new Runnable() {
        @Override
        public void run() {
            while (isOpen()&&weakReference.get()!=null){
                final byte[] data = new byte[maxReadBytes];
                int cnt = usbDeviceConnection.bulkTransfer(epIn, data, data.length, 2000);
                if (cnt!=-1){
                    byte[] bytes = Arrays.copyOfRange(data, 0, cnt);
                    LogUtils.e(Arrays.toString(bytes));
                }
            }
        }
    };

    private SerialPortDevice(Builder builder) throws Exception {
        super(builder);
        if (builder.baudRate!=0)baudRate = builder.baudRate;
        if (builder.dataBits!=0)dataBits = builder.dataBits;
        if (builder.stopBits!=0)stopBits = builder.stopBits;
        if (builder.parity!=0)parity = builder.parity;
        if (builder.maxReadBytes!=0)maxReadBytes = builder.maxReadBytes;
        if (builder.weakReference!=null)this.weakReference = builder.weakReference;
    }

    @Override
    void onOpen(UsbDevice usbDevice) {
        UsbManager usbManager = getUsbManager();
        if (usbManager!=null){
            usbDeviceConnection = usbManager.openDevice(usbDevice);
            int interfaceCount = usbDevice.getInterfaceCount();
            if (usbDeviceConnection!=null && interfaceCount>0){
                usbInterface = usbDevice.getInterface(interfaceCount - 1);
                boolean claimInterface = usbDeviceConnection.claimInterface(usbInterface, false);
                if (claimInterface){
                    //设置波特率等设置
                    setConfig(usbDeviceConnection,baudRate,dataBits,stopBits,parity);
                    for (int index = 0; index < usbInterface.getEndpointCount(); index++) {
                        UsbEndpoint ep = usbInterface.getEndpoint(index);
                        if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                                && (ep.getDirection() == UsbConstants.USB_DIR_OUT)) {
                            epOut = ep;
                        }
                        if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                                && (ep.getDirection() == UsbConstants.USB_DIR_IN)) {
                            epIn = ep;
                        }
                    }
                }
                    setOpen(true);
                    readDataThread = new Thread(readDataRunnable);
                    readDataThread.start();
                //打开读取接口
            }

        }
    }

    @Override
    void onClose(UsbDevice usbDevice) {
        if (usbDeviceConnection!=null){
            try {
                setOpen(false);
                usbDeviceConnection.releaseInterface(usbInterface);
                usbDeviceConnection.close();
                usbDeviceConnection=null;
                usbInterface = null;
                epOut = null;
                epIn = null;
            }catch (Exception e){
            }
        }
    }

    public boolean sendData(byte [] data){
        boolean success = false;
        if (isOpen()){
            if (usbDeviceConnection!=null&&epOut!=null&&data!=null){
                int timeOut = 1000;
                int i= usbDeviceConnection.bulkTransfer(epOut,data,data.length,timeOut);
                success = i>0;
            }
        }
        return  success;
    }

    private void setConfig(UsbDeviceConnection connection, int baudRate, int dataBit, int stopBit, int parity) {
        int value = 0;
        int index = 0;
        char valueHigh = 0, valueLow = 0, indexHigh = 0, indexLow = 0;
        switch (parity) {
            case 0: /* NONE */
                valueHigh = 0x00;
                break;
            case 1: /* ODD */
                valueHigh |= 0x08;
                break;
            case 2: /* Even */
                valueHigh |= 0x18;
                break;
            case 3: /* Mark */
                valueHigh |= 0x28;
                break;
            case 4: /* Space */
                valueHigh |= 0x38;
                break;
            default: /* None */
                valueHigh = 0x00;
                break;
        }

        if (stopBit == 2) {
            valueHigh |= 0x04;
        }

        switch (dataBit) {
            case 5:
                valueHigh |= 0x00;
                break;
            case 6:
                valueHigh |= 0x01;
                break;
            case 7:
                valueHigh |= 0x02;
                break;
            case 8:
                valueHigh |= 0x03;
                break;
            default:
                valueHigh |= 0x03;
                break;
        }

        valueHigh |= 0xc0;
        valueLow = 0x9c;

        value |= valueLow;
        value |= (int) (valueHigh << 8);

        switch (baudRate) {
            case 50:
                indexLow = 0;
                indexHigh = 0x16;
                break;
            case 75:
                indexLow = 0;
                indexHigh = 0x64;
                break;
            case 110:
                indexLow = 0;
                indexHigh = 0x96;
                break;
            case 135:
                indexLow = 0;
                indexHigh = 0xa9;
                break;
            case 150:
                indexLow = 0;
                indexHigh = 0xb2;
                break;
            case 300:
                indexLow = 0;
                indexHigh = 0xd9;
                break;
            case 600:
                indexLow = 1;
                indexHigh = 0x64;
                break;
            case 1200:
                indexLow = 1;
                indexHigh = 0xb2;
                break;
            case 1800:
                indexLow = 1;
                indexHigh = 0xcc;
                break;
            case 2400:
                indexLow = 1;
                indexHigh = 0xd9;
                break;
            case 4800:
                indexLow = 2;
                indexHigh = 0x64;
                break;
            case 9600:
                indexLow = 2;
                indexHigh = 0xb2;
                break;
            case 19200:
                indexLow = 2;
                indexHigh = 0xd9;
                break;
            case 38400:
                indexLow = 3;
                indexHigh = 0x64;
                break;
            case 57600:
                indexLow = 3;
                indexHigh = 0x98;
                break;
            case 115200:
                indexLow = 3;
                indexHigh = 0xcc;
                break;
            case 230400:
                indexLow = 3;
                indexHigh = 0xe6;
                break;
            case 460800:
                indexLow = 3;
                indexHigh = 0xf3;
                break;
            case 500000:
                indexLow = 3;
                indexHigh = 0xf4;
                break;
            case 921600:
                indexLow = 7;
                indexHigh = 0xf3;
                break;
            case 1000000:
                indexLow = 3;
                indexHigh = 0xfa;
                break;
            case 2000000:
                indexLow = 3;
                indexHigh = 0xfd;
                break;
            case 3000000:
                indexLow = 3;
                indexHigh = 0xfe;
                break;
            default: // default baudRate "9600"
                indexLow = 2;
                indexHigh = 0xb2;
                break;
        }

        index |= 0x88 | indexLow;
        index |= (int) (indexHigh << 8);

        connection.controlTransfer(
                (0x02 << 5) | 0x00 | 0x00, 0xA1, value, index, null,
                0, 2000);
    }

    public static class Builder extends DeviceBuilder<Builder,SerialPortDevice>{
        public int baudRate;
        public int dataBits;
        public int stopBits;
        public int parity;
        public int maxReadBytes;
        public WeakReference<Context> weakReference;

        public Builder setBaudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public Builder setDataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public Builder setStopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public Builder setParity(int parity) {
            this.parity = parity;
            return this;
        }

        public Builder setMaxReadBytes(int maxReadBytes) {
            this.maxReadBytes = maxReadBytes;
            return this;
        }

        @Override
        public SerialPortDevice Build(Context context) {
            try {
                weakReference = new WeakReference<>(context);
                return new SerialPortDevice(this);
            }catch (Exception e){
                return null;
            }
        }
    }




}
