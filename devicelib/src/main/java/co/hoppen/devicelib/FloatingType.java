package co.hoppen.devicelib;

/**
 * Created by YangJianHui on 2021/9/27.
 */
public enum FloatingType {
    FLOATING_BALL(FloatingBall.class);

    private Class className;
    FloatingType(Class c) {
        this.className = c;
    }

    public Class getClassName() {
        return className;
    }
}
