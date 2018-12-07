package com.haylion.haylionbuglylib;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Author:wangjianming
 * Time:2018/11/20 11:20
 * Description:BuglyUtil bug实时上报和热修复工具
 */
public class BuglyUtil {

    /**
     * 初始化bugly
     *
     * @param app          当前的application对象
     * @param appid        bugly平台申请的appid
     * @param isTest       调试时，将第三个参数改为true
     * @param version      版本号
     * @param isNeedTinker 是否需要热更新功能
     */
    public static void init(Application app, String appid, boolean isTest, String version, boolean isNeedTinker) {

        if (isNeedTinker) {
            // 安装MultiDex，tinker热修复必须
            MultiDex.install(app);
            // 安装tinker
            Beta.installTinker();
        }
        Bugly.init(app, appid, isTest);
        CrashReport.initCrashReport(app, appid, isTest);
        setVersion(app, version); //版本号--用于异常捕获
    }

    /**
     * 设置bugly的版本号
     *
     * @param context    当前的application对象
     * @param appVersion 版本号
     */
    public static void setVersion(Context context, String appVersion) {
        CrashReport.setAppVersion(context, appVersion);
    }

    /**
     * 设置启动禁用bugly
     *
     * @param isEnable
     */
    public static void setEnable(boolean isEnable) {
        CrashReport.enableBugly(isEnable);
    }
}
