package com.haylion.haylionbugly;

import android.app.Application;

import com.haylion.haylionbuglylib.BuglyUtil;

/**
 * Author:wangjianming
 * Time:2018/11/21 10:31
 * Description:MyApp
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化bugly
        //参数1：当前application
        //参数2：在bugly平台申请的App Id
        //参数3：是否是测试
        //参数4：App的版本号
        //参数5：是否需要tinker热修复功能
        BuglyUtil.init(this, BuildConfig.buglyId, BuildConfig.IS_DEBUG, BuildConfig.VERSION_NAME, true);
    }
}
