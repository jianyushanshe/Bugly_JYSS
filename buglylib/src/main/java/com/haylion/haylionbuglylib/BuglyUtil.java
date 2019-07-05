package com.haylion.haylionbuglylib;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.widget.Toast;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Locale;

/**
 * Author:wangjianming
 * Time:2018/11/20 11:20
 * Description:BuglyUtil bug实时上报和热修复工具
 */
public class BuglyUtil {

    public interface TinkerHotFixPathcListener {
        /**
         * 补丁文件路径
         *
         * @param patchFile
         */
        void onPatchURL(String patchFile);

        /**
         * 补丁下载进度
         *
         * @param savedLength
         * @param totalLength
         */
        void onDownloadProgress(long savedLength, long totalLength);

        /**
         * 补丁下载成功
         */
        void onDownloadSuccess();

        /**
         * 补丁下载失败
         */
        void onDownloadFailure();

        /**
         * 补丁应用成功
         */
        void onApplySuccess();

        /**
         * 补丁应用失败
         */
        void onApplyFailure();

        /**
         * 补丁应用失败
         */
        void onPatchRollback();
    }

    private static TinkerHotFixPathcListener tinkerHotFixPathcListener;

    /**
     * 初始化bugly
     *
     * @param app          当前的application对象
     * @param appid        bugly平台申请的appid
     * @param isTest       调试时，将第三个参数改为true
     * @param version      版本号
     * @param isNeedTinker 是否需要热更新功能
     */
    public static void init(final Application app, String appid, boolean isTest, String version, boolean isNeedTinker, TinkerHotFixPathcListener listener) {
        tinkerHotFixPathcListener = listener;
        //设置是否启用热修复
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = false;
        // 补丁回调接口
        Beta.betaPatchListener = new BetaPatchListener() {
            @Override
            public void onPatchReceived(String patchFile) {
//                Toast.makeText(app, "补丁下载地址" + patchFile, Toast.LENGTH_SHORT).show();
                tinkerHotFixPathcListener.onPatchURL(patchFile);
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
                tinkerHotFixPathcListener.onDownloadProgress(savedLength, totalLength);
//                Toast.makeText(app,
//                        String.format(Locale.getDefault(), "%s %d%%",
//                                Beta.strNotificationDownloading,
//                                (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)),
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadSuccess(String msg) {
                tinkerHotFixPathcListener.onDownloadSuccess();
                //Toast.makeText(app, "补丁下载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFailure(String msg) {
                tinkerHotFixPathcListener.onDownloadFailure();
                //Toast.makeText(app, "补丁下载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApplySuccess(String msg) {
                tinkerHotFixPathcListener.onApplySuccess();
                //Toast.makeText(app, "补丁应用成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApplyFailure(String msg) {
                tinkerHotFixPathcListener.onApplyFailure();
                //Toast.makeText(app, "补丁应用失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPatchRollback() {
                tinkerHotFixPathcListener.onPatchRollback();
                //Toast.makeText(app, "补丁应用失败", Toast.LENGTH_SHORT).show();
            }
        };


        // 设置开发设备，默认为false，上传补丁如果下发范围指定为“开发设备”，需要调用此接口来标识开发设备
        Bugly.setIsDevelopmentDevice(app, isTest); //在发布正式版本的时候需要关掉,不然发布新的patch包的时候,测试都没法去测试!

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
