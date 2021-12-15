package co.hoppen.devicelib;

import android.view.LayoutInflater;

import com.blankj.utilcode.util.LogUtils;

import java.lang.reflect.Constructor;

/**
 * Created by YangJianHui on 2021/9/27.
 */
class FloatingBuilder {

    public static <F extends FloatingView> F create(Class viewClass,LayoutInflater inflater){
        try {

            Constructor constructor = viewClass.getConstructor(LayoutInflater.class);
            Object o = constructor.newInstance(inflater);
            if (o instanceof  FloatingView){
                return (F) o;
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
