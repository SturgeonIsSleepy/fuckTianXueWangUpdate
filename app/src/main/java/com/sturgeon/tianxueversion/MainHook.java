package com.sturgeon.tianxueversion;

import android.content.pm.PackageInfo;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "[TianXueVersionSpoof]";
    private static final String TARGET_PACKAGE = "com.up366.mobile";
    private static final int FAKE_VERSION_CODE = 2_100_000_000;

    private static volatile boolean spoofLogged = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + " loaded in " + lpparam.processName);

        Class<?> apm = XposedHelpers.findClass(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader
        );

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object result = param.getResult();
                if (!(result instanceof PackageInfo)) {
                    return;
                }

                PackageInfo info = (PackageInfo) result;
                if (!TARGET_PACKAGE.equals(info.packageName)) {
                    return;
                }

                int oldVersionCode = info.versionCode;
                info.versionCode = FAKE_VERSION_CODE;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.versionCodeMajor = 0;
                }

                if (!spoofLogged) {
                    spoofLogged = true;
                    XposedBridge.log(TAG + " spoofed versionCode "
                            + oldVersionCode + " -> " + FAKE_VERSION_CODE);
                }
            }
        };

        XposedBridge.hookAllMethods(apm, "getPackageInfo", hook);
        XposedBridge.hookAllMethods(apm, "getPackageInfoAsUser", hook);
    }
}
