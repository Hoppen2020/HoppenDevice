package co.hoppen.devicedemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import co.hoppen.devicelib.DeviceController;
import co.hoppen.devicelib.HoppenDeviceHelper;
import co.hoppen.devicelib.OnDeviceListener;


public class MainActivity extends AppCompatActivity implements OnDeviceListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DeviceController controller = HoppenDeviceHelper.createController(this, this);
        controller.setOnInstructionListener(callback -> {

        });
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnect() {

    }

}