package co.hoppen.devicelib;


/**
 * Created by YangJianHui on 2021/3/17.
 */
public interface Controller {

        void close();

        void selectHandle(String typeCode);

        void setHandleStart(int mode ,int strength,int time);

        void setHandleStop(int mode ,int strength,int time);

        void setHandleConfig(int mode ,int strength,int time);

        void enterHandleRate();

        void exitHandleRate();

        void setHandleRate(int data);

        void getDeviceCode();

        void getUsbVerInfo();

        void customInstruction(byte [] instruction);

}
