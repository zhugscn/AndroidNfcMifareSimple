package cn.zhuguangsheng.android.nfcm1demo;

/**
 * Created by zhuguangsheng on 2017/9/28.
 */

public class M1PasswordHolder {
    private static String Password = "FFFFFFFFFFFF";    //在这里指定读取密码

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }
}
