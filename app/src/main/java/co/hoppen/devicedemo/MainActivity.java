package co.hoppen.devicedemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import co.hoppen.devicelib.DeviceController;
import co.hoppen.devicelib.HoppenDeviceHelper;
import co.hoppen.devicelib.OnDeviceListener;
import co.hoppen.devicelib.UsbInstructionUtils;


public class MainActivity extends AppCompatActivity implements OnDeviceListener {
    private TextView tv_callback;
    private ScrollView scrollview;
    private DeviceController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.getInstance().init();
        setContentView(R.layout.activity_main);
        initView();
        controller = HoppenDeviceHelper.createController(this, false);
        controller.setOnInstructionListener(callback -> {
            send(callback);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected() {
        send("设备已连接");
    }

    @Override
    public void onDisconnect() {
        send("设备已断开");
    }

    private void initView() {
        scrollview = findViewById(R.id.scrollview);
        tv_callback = findViewById(R.id.tv_callback);
    }

    private void send(String send){
        tv_callback.setText(send + "\n"+ tv_callback.getText().toString());
        scrollview.fullScroll(View.FOCUS_UP);
    }

    private int add(int num){
        return num+1;
    }

    private int sub(int num){
        if (num==0)return num;
        return num-1;
    }


    public void time(View view) {
        TextView tv_time = findViewById(R.id.tv_time);
        int time = Integer.parseInt(tv_time.getText().toString());
        switch (view.getId()){
            case R.id.btn_time_add:
                time = add(time);
                break;
            case R.id.btn_time_sub:
                time = sub(time);
                break;
        }
        tv_time.setText(time+"");
    }

    public void strength(View view) {
        TextView tv_strength =  findViewById(R.id.tv_strength);
        int strength = Integer.parseInt(tv_strength.getText().toString());
        switch (view.getId()){
            case R.id.btn_strength_add:
                strength = add(strength);
                break;
            case R.id.btn_strength_sub:
                strength = sub(strength);
                break;
        }
        tv_strength.setText(strength+"");
    }

    public void mode(View view) {
        TextView tv_mode =  findViewById(R.id.tv_mode);
        int mode = Integer.parseInt(tv_mode.getText().toString());
        switch (view.getId()){
            case R.id.btn_mode_add:
                mode = add(mode);
                break;
            case R.id.btn_mode_sub:
                mode = sub(mode);
                break;
        }
        tv_mode.setText(mode+"");
    }

    public void select(View view) {
        EditText et_code = findViewById(R.id.et_code);
        if (controller!=null)controller.selectHandle(et_code.getText().toString());
    }

    public void function(View view) {
        if (controller==null)return;
        TextView tv_time =  findViewById(R.id.tv_time);
        TextView tv_strength =  findViewById(R.id.tv_strength);
        TextView tv_mode =  findViewById(R.id.tv_mode);
        int time = Integer.parseInt(tv_time.getText().toString()) * 60;
        int strength =  Integer.parseInt(tv_strength.getText().toString());
        int mode = Integer.parseInt(tv_mode.getText().toString());
        switch (view.getId()){
            case R.id.btn_set:
                controller.setHandleConfig(mode,strength,time);
                break;
            case R.id.btn_start:
                controller.setHandleStart(mode,strength,time);
                break;
            case R.id.btn_stop:
                controller.setHandleStop(mode,strength,time);
                break;
        }
    }

    public void enter(View view) {
        if (controller==null)return;
        controller.enterHandleRate();
    }

    public void exit(View view) {
        if (controller==null)return;
        controller.exitHandleRate();
    }

    public void setRate(View view) {
        try {
            if (controller==null)return;
            EditText et_rate = findViewById(R.id.et_rate);
            int rate = Integer.parseInt(et_rate.getText().toString());
            controller.setHandleRate(rate);
        }catch (Exception e){
        }
    }

    public void wskt001(View view){
        //if (controller!=null)controller.getDeviceCode();
        if (controller!=null)controller.customInstruction(new byte[]{1});
    }


    public void wskt006(View view){
        if (controller!=null)controller.selectHandle("WSKT006");
    }

    public void wskt010(View view){
        if (controller!=null)controller.selectHandle("WSKT010");
    }

    public void wskt002(View view){
        if (controller!=null)controller.getUsbVerInfo();
    }

}