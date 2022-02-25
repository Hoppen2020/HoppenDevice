package co.hoppen.devicelib;

/**
 * Created by Administrator on 2018/4/4.
 */

public class UsbInstructionUtils {
    public static byte[] USB_DEVICE_CODE(){
        byte[] linePackage= {(byte) 0xAA, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(linePackage);
    }

    public static byte[] USB_VER_INFO(){
        byte[] bytes = {(byte) 0xAA, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(bytes);
    }

    public static byte[] USB_SYS_ONLINE(){
        byte[] linePackage= {(byte) 0xAA, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};
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

    public static byte[] USB_HANDLE_START(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x10, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_HANDLE_STOP(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x11, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_HANDLE_SET(String typeName){
        byte handle=0;
        if (typeName.equals("WSKT001")){
            handle =(byte) 0x01;
        }else if (typeName.equals("WSKT002")){
            handle =(byte)0x0A;
        }else if (typeName.equals("WSKT003")){
            handle =(byte)0x05;
        }else if (typeName.equals("WSKT004")){
            handle =(byte)0x03;
        }else if (typeName.equals("WSKT005")){
            handle =(byte)0x0F;
        }else if (typeName.equals("WSKT006")){
            handle =(byte)0x04;
        }else if (typeName.equals("WSKT007")){
            handle =(byte)0x06;
        }else if (typeName.equals("WSKT008")){
            handle =(byte)0x07;
        }else if (typeName.equals("WSKT009")){
            handle =(byte)0x11;
        }else if (typeName.equals("WSKT010")){
            handle =(byte)0x09;
        }else if (typeName.equals("WSKT011")){
            handle =(byte)0x0B;
        }else if (typeName.equals("WSKT012")){
            handle =(byte)0x10;
        }else if (typeName.equals("WSKT013")){
            handle =(byte)0x02;
        }else if (typeName.equals("WSKT014")){
            handle =(byte)0x0C;
        }else if (typeName.equals("WSKT015")){
            handle =(byte)0x0D;
        }else if (typeName.equals("WSKT016")){
            handle =(byte)0x0E;
        }else if (typeName.equals("WSKT017")){
            handle =(byte)0x08;
        }else if (typeName.equals("WSKT019")){
            handle =(byte)0x12;
        }else if (typeName.equals("WSKT020")){
            handle =(byte)0x13;
        }else if (typeName.equals("WSKT021")){
            handle =(byte)0x14;
        }else if (typeName.equals("WSKT022")){
            handle =(byte)0x15;
        }else if (typeName.equals("WSKT023")){
            handle =(byte)0x16;
        }else if (typeName.equals("WSKT024")){
            handle =(byte)0x17;
        }else if (typeName.equals("WSKT025")){
            handle =(byte)0x18;
        }else if (typeName.equals("WSKT026")){
            handle =(byte)0x19;
        } else if (typeName.equals("WSKT027")){
            handle =(byte)0x20;
        } else if (typeName.equals("WSKT028")){
            handle =(byte)0x21;
        } else if (typeName.equals("WSKT029")){
            handle =(byte)0x22;
        } else if (typeName.equals("WSKT030")){
            handle =(byte)0x30;
        }

        byte[] config={(byte) 0xAA, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00,  handle};
        return encryption(config);
    }

    public static byte[] USB_ENTER_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_EXIT_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x03, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_SET_RATE(int data){
        int a = (data >>8 );
        int b = (data & 0xff);
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x02, (byte) 0x00, (byte) a, (byte) b};
        return encryption(config);
    }

    public static byte[] USB_HANDLE_CONFIG(int modePosition, int strength, int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = time==0?(byte)0:(byte)(time/256);
        byte mtime_2 = time==0?(byte)0:(byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x12, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

}
