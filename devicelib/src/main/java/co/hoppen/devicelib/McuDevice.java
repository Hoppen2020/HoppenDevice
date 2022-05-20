package co.hoppen.devicelib;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import co.hoppen.devicelib.queue.ConnectMcuDeviceTask;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public class McuDevice extends HoppenDevice{
    private UsbManager usbManager;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint epOut, epIn;
    private final static int DEFAULT_MAX_READ_BYTES = 128;
    private final static int DEFAULT_TIMEOUT = 500;
    private OnDeviceListener onDeviceListener;
    private OnInstructionListener onInstructionListener;
    private FloatingView floatingView;

    public void setFloatingView(FloatingView floatingView) {
        this.floatingView = floatingView;
    }

    public void setOnInstructionListener(OnInstructionListener onInstructionListener) {
        this.onInstructionListener = onInstructionListener;
//        byte[] bytes = UsbInstructionUtils.USB_DEVICE_CODE();
//        LogUtils.e(bytes.toString());
    }

    public void setOnDeviceListener(OnDeviceListener onDeviceListener) {
        this.onDeviceListener = onDeviceListener;
    }

    private Thread readDataThread;

    private Disposable disposable;

    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            while (usbDeviceConnection!=null){
                try {
                    byte[] bytes = readData();
                    if (bytes!=null){
                        String[] strings = decodingData(bytes);
                        if (strings!=null){
                            for (String data :strings){
                                if (!data.equals("System-OnLine")){
                                    if (onInstructionListener!=null){
                                        instructionCallbackForMainThread(data);
                                    }
                                }
                            }
                        }
                        //测试
//                        else {
//                            if (onInstructionListener!=null){
//                                instructionCallbackForMainThread(Arrays.toString(bytes));
//                            }
//                        }
                    }
                }catch (Exception e){
                }
            }
        }
    };

    public McuDevice(UsbManager usbManager){
        this.usbManager = usbManager;
    }

    public synchronized void onConnecting(ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo){
        usbDeviceConnection = connectMcuInfo.getUsbDeviceConnection();
        usbInterface = connectMcuInfo.getUsbInterface();
        epOut = connectMcuInfo.getEpOut();
        epIn = connectMcuInfo.getEpIn();
        if (onDeviceListener!=null)onDeviceListener.onConnected();
        disposable = Observable.interval(5, 5, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Throwable {
                asySendInstructions(UsbInstructionUtils.USB_SYS_ONLINE());
            }
        });
        readDataThread = new Thread(readRunnable);
        readDataThread.start();

    }


    @Override
    public synchronized void onConnecting(UsbDevice usbDevice, DeviceType type) {

    }

    @Override
    public synchronized void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (onDeviceListener!=null)onDeviceListener.onDisconnect();
        this.closeDevice();
    }

    @Override
    protected synchronized void asySendInstructions(byte[] bytes) {
        asySendInstructions(bytes, DEFAULT_TIMEOUT);
        if (floatingView!=null){
            ((FloatingBall)floatingView).addText( Arrays.toString(bytes));
        }
    }

    @Override
    protected void closeDevice() {
        if (usbDeviceConnection!=null){
            try {
                    if (disposable!=null)disposable.dispose();
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
    //异步
    private synchronized void asySendInstructions(byte [] data, final int timeOut){
        Observable.create(new ObservableOnSubscribe<byte []>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<byte[]> emitter) throws Throwable {
                boolean success = false;
                if (usbDeviceConnection!=null&&epOut!=null&&data!=null){
                    int i= usbDeviceConnection.bulkTransfer(epOut,data,data.length,timeOut);
                    success = i>0;
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
    //同步
    private synchronized boolean synSendInstructions(byte [] data){
        boolean success = false;
        if (usbDeviceConnection!=null&&epOut!=null&&data!=null){
            int i= usbDeviceConnection.bulkTransfer(epOut,data,data.length,0);
            success = i>0;
        }
        return success;
    }


    private byte[] readData(){
        final byte[] data = new byte[DEFAULT_MAX_READ_BYTES];
        int cnt = usbDeviceConnection.bulkTransfer(epIn, data, data.length, DEFAULT_TIMEOUT);
        if (cnt!=-1){
            byte[] bytes = Arrays.copyOfRange(data, 0, cnt);
            return bytes;
        }else return null;
    }

    private String[] decodingData(byte [] data) {
        String [] decodData = null;
        try {
            String stringData = new String(data);
            String[] split = stringData.split("]>");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].replace("<[","");
            }
            decodData = split;
        }catch (Exception e){
        }
        return decodData;
    }

    private void instructionCallbackForMainThread(String data){
        Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
            if (floatingView!=null)((FloatingBall)floatingView).addText(s);
            onInstructionListener.onInstruction(s);
        });
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


}
