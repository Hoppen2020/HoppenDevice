package com.hoppen.device_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hoppen.device.HoppenDevice;
import com.hoppen.device.OnDeviceListener;
import com.hoppen.device.SerialPortDevice;
import com.hoppen.device.UsbMonitor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SerialPortDevice serialPortDevice = new SerialPortDevice.Builder()
                .setVendorId(1000)
                .setProductId(1000)
                .Build(this);

        SerialPortDevice serialPortDevice2 = new SerialPortDevice.Builder()
                .setVendorId(6790)
                .setProductId(29987)
                .setBaudRate(9600)
                .setOnDeviceListener(new OnDeviceListener() {
                    @Override
                    public void onConnect() {
                        Log.e("onConnect","onConnect");
                    }

                    @Override
                    public void onDisDisconnect() {
                        Log.e("onDisDisconnect","onDisDisconnect");
                    }
                }).Build(this);
        List<HoppenDevice> list = new ArrayList<>();
        list.add(serialPortDevice);
        list.add(serialPortDevice2);
        UsbMonitor usbMonitor = new UsbMonitor(this,list);



        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("asdasd","asd");
                byte[] config={(byte) 0xAA, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00,  (byte) 0x01};
                serialPortDevice2.sendData(encryption(config));
            }
        });
    }

    public static byte[] USB_DEVICE_CODE(){
        byte[] linePackage= {(byte) 0xAA, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(linePackage);
    }

    private static byte[] encryption(byte[] data) {
        byte[] returnData = new byte[data.length + 1];
        try {
            byte a = 0;
            for (int i = 0; i < data.length; i++) {
                returnData[i] =data[i];
                if (i!=0){
                    a ^=data[i];
                }
            }
            returnData[data.length] = a;
        } catch (Exception e) {
        }
        return returnData;
    }
}