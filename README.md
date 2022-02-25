# HoppenDevice
#### 准备工作
```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
	}
}
```
```
dependencies {
    implementation 'com.github.Hoppen2020:HoppenDevice:1.0.2-1'
}
```
#### 使用
***在Activity或者在继承LifecycleOwner中直接使用（已遵循android Lifecycle）***
```
HoppenController controller =
            HoppenCameraHelper.createController(Activity, OnDeviceListener);
      
new OnDeviceListener(){
     @Override
     public void onConnected() {
       // 设备连接上
     }

     @Override
     public void onDisconnect() {
       // 设备断开
     }
}            
        
```
#### 设置指令回调监听
```
controller.setOnInstructionListener(callback -> {
    //callback指令回调
});
```
#### 设备控制(必须：先selectHandle)
```
 //typeCode:WSKT001、WSKT002、WSKT003、WSKT004、WSKT005、WSKT006、WSKT007、WSKT008、WSKT009、WSKT010
 //         WSKT011、WSKT012、WSKT013、WSKT014、WSKT015、WSKT016、WSKT017、WSKT019、WSKT020、WSKT021
            WSKT022、WSKT023、WSKT024、WSKT025、WSKT026		
 
  /**
     * @param mode 模式
     * @param strength 强度
     * @param time 时间
     */
controller.selectHandle(String typeCode);
controller.setHandleConfig(int mode, int strength, int time);
controller.setHandleStart(int mode, int strength, int time);
controller.setHandleStop(int mode, int strength, int time);

```
#### 设置设备频率(必须：enter→set→exit)
```
controller.enterHandleRate();
controller.setHandleRate(int rate);
controller.exitHandleRate();
```
#### OnInstructionListener监听说明

###### 设备主动发送指令到app

|文字|说明|
|---|:---:|
|Function-ON-RetCmm|功能设备开启|
|Function-OFF-RetCmm|功能设备关闭|
|WasteWater-Overflow|功能设备废水瓶溢出|
|Stuck-BubblePen|清洁笔按键卡死|
|Stuck-SkinScrubber|铲皮刀按键卡死|
|Stuck-Oxygen|喷枪按键卡死|
|Stuck-EyesRF|眼部射频按键卡死|
|Stuck-EMS|EMS按键卡死|
|Stuck-Cold|冰导按键卡死|

###### app触发指令返回
|文字|说明|
|---|:---:|
|Function-ON|功能设备开启|
|Function-OFF|功能设备关闭|
|Set-Freq-Val|设定频率参数|
|Quick-FreqSet|退出频率设定|
|Select-Oxygen|注氧喷枪|
|Select-SkinScrubber|角质铲|
|Select-SkinScrubber-FreqSet|角质铲->带调频功能|
|Select-EyesSonic|眼部超声波|
|Select-EyesSonic-FreqSet|眼部超声波->带调频功能|
|Select-EyesRF|眼部射频|
|Select-FaceSonic|面部超声波|
|Select-FaceSonic-FreqSet|面部超声波->带调频功能|
|Select-IonClip|离子夹|
|Select-IonRoller|离子滚轮|
|Select-ElectricalClip|电流夹|
|Select-Hot&Cold|冷热导入|
|Select-VacuumCleaner|吸笔|
|Select-Cold|制冷(冰封头)|
|Select-Ilisya|眼贴|
|Select-RadioFrequency|高周波|
|Select-Mask|面罩|
|Select-Scalp-Sonic|头皮超声波|
|Select-Heat|热导入|
|Select-BIO|BIO微电|
|Select-Eyeshade|眼罩|
|Select-GirlLine|少女线|
|Select-WaterOxygen|水氧|
|Select-OxygenInjection|注氧（非喷枪）|
|Select-EMS|EMS|

###### 调频说明 例：
|文字|说明|
|---|:---:|
|声波：FreqSet-2-1-(1-9)|默认值：2  步进值：1 范围：1~9|

###### 其他或错误
|文字|说明|
|---|:---:|
|Check-Sum-Err|校验出错|
|Command-Invalid|无效的指令（校验正确）|
|FunctionModel-Err|护理模式不正确的返回例如在小气泡护理模式下，发送非小气泡功能对应的指令（模式切换指令除外）|
|Communicate-Err|系统通讯出错|
