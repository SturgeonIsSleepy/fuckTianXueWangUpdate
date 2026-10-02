package com.sturgeon.tianxueversion;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.os.Build;

import java.util.concurrent.atomic.AtomicInteger;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "[TianXueVersionSpoof]";
    private static final String TARGET_PACKAGE = "com.up366.mobile";
    private static final int FAKE_VERSION_CODE = 2_100_000_000;
    private static final long FAKE_LONG_VERSION_CODE = 2_100_000_000L;

    private static volatile boolean packageInfoLogged = false;
    private static volatile boolean longVersionLogged = false;
    private static final AtomicInteger dialogCount = new AtomicInteger(0);

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + " loaded in " + lpparam.processName);

        hookVersionApis(lpparam);
        hookDialogShow();
    }

    private static void hookVersionApis(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> apm = XposedHelpers.findClass(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader
        );

        XC_MethodHook packageInfoHook = new XC_MethodHook() {
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
                    try {
                        XposedHelpers.setIntField(info, "versionCodeMajor", 0);
                    } catch (Throwable ignored) {
                    }
                }

                if (!packageInfoLogged) {
                    packageInfoLogged = true;
                    XposedBridge.log(TAG + " spoofed PackageInfo.versionCode "
                            + oldVersionCode + " -> " + FAKE_VERSION_CODE);
                }
            }
        };

        XposedBridge.hookAllMethods(apm, "getPackageInfo", packageInfoHook);
        XposedBridge.hookAllMethods(apm, "getPackageInfoAsUser", packageInfoHook);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            XposedBridge.hookAllMethods(PackageInfo.class, "getLongVersionCode",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            PackageInfo info = (PackageInfo) param.thisObject;
                            if (info == null || !TARGET_PACKAGE.equals(info.packageName)) {
                                return;
                            }

                            param.setResult(FAKE_LONG_VERSION_CODE);

                            if (!longVersionLogged) {
                                longVersionLogged = true;
                                XposedBridge.log(TAG
                                        + " spoofed PackageInfo.getLongVersionCode() -> "
                                        + FAKE_LONG_VERSION_CODE);
                            }
                        }
                    });
        }
    }

    private static void hookDialogShow() {
        XposedBridge.hookAllMethods(Dialog.class, "show", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                int n = dialogCount.incrementAndGet();
                if (n > 12) {
                    return;
                }

                Dialog dialog = (Dialog) param.thisObject;
                String dialogClass = dialog == null ? "null" : dialog.getClass().getName();

                XposedBridge.log(TAG + " Dialog.show #" + n
                        + " class=" + dialogClass
                        + "\n" + android.util.Log.getStackTraceString(new Throwable("Dialog.show trace")));
            }
        });
    }
}
