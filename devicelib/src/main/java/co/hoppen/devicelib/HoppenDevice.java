package co.hoppen.devicelib;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public abstract class HoppenDevice implements OnUsbStatusListener{

    protected abstract void asySendInstructions(byte [] bytes);
    protected abstract void closeDevice();
}
