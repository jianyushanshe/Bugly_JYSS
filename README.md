**Android基于Bugly实现异常实时上报和热修复集成文档**

|版本号  | 时间 |
|--|--|
|1.0.0  | 2018-11-22 |

|章节  |内容  |
|--|--|
| 1 | Tinker热修复原理 |
| 2 | bugly集成步骤	 |
| 3 | 生成热修复补丁 |
| 4 | 热修复补丁下发 |
| 5 | 热修复补丁生效 |
| 6 | 在bugly申请appId |
| 7 | 在bugly查看异常信息 |
| 8 | 注意事项 |
	

**1.Tinker热修复原理**
原理：通过新旧apk比较，使用gradle从插件生成.dex补丁文件（并不是真正的dex文件），补丁通过服务器下发后尝试对dex文件二路归并进行合并，最终生成全量的dex文件，与生成补丁互为逆过程，生成全量dex文件后进行optimize操作，最终生成odex文件。在Application中进行反射调用已经合成的dex文件。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122163915820.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

**2.Bugly集成步骤**

(1).在工程的build.gradle中添加以下代码

```
allprojects {
    repositories {
……
    maven { url 'https://jitpack.io' }
    }
}
```

(2).在工程的build.gradle中添加以下代码

```
dependencies {
    ……
    classpath "com.tencent.bugly:tinker-support:1.1.2"
}
```

(3).在app的build.gradle中添加以下代码

```
dependencies {
    ……
  implementation 'com.github.jianyushanshe:Bugly_JYSS:1.1'
}
```

(4).在app的build.gradle同级目录下创建tinker脚本文件tinker-support.gradle，拷贝以下代码到该文件。

```
apply plugin: 'com.tencent.bugly.tinker-support'
def bakPath = file("${buildDir}/bakApk/")
/**
 * 此处填写每次构建生成的基准包目录
 */
def baseApkDir = "app-1122-09-30-10"
/**
 * 对于插件各参数的详细解析请参考
 */
tinkerSupport {
    // 开启tinker-support插件，默认值true
    enable = true
    // tinkerEnable功能开关
    tinkerEnable = true
    // 指定归档目录，默认值当前module的子目录tinker
    autoBackupApkDir = "${bakPath}"
    // 是否启用覆盖tinkerPatch配置功能，默认值false
    // 开启后tinkerPatch配置不生效，即无需添加tinkerPatch
    overrideTinkerPatchConfiguration = true
    // 编译补丁包时，必需指定基线版本的apk，默认值为空
    // 如果为空，则表示不是进行补丁包的编译
    // @{link tinkerPatch.oldApk }
    baseApk = "${bakPath}/${baseApkDir}/app-release.apk"
    // 对应tinker插件applyMapping
    baseApkProguardMapping = "${bakPath}/${baseApkDir}/app-release-mapping.txt"
    // 对应tinker插件applyResourceMapping
    baseApkResourceMapping = "${bakPath}/${baseApkDir}/app-release-R.txt"
    // 构建基准包和补丁包都要指定不同的tinkerId，并且必须保证唯一性
    // tinkerId = "release-1.1.2" //
    tinkerId = "fix-1.1.2.1" //
    // 构建多渠道补丁时使用
    // buildAllFlavorsDir = "${bakPath}/${baseApkDir}"
    // 是否启用加固模式，默认为false.(tinker-spport 1.0.7起支持）
    // isProtectedApp = true
    // 是否开启反射Application模式
    enableProxyApplication = true
    // 是否支持新增非export的Activity（注意：设置为true才能修改AndroidManifest文件）
    supportHotplugComponent = true

}

/**
 * 一般来说,我们无需对下面的参数做任何的修改
 * 对于各参数的详细介绍请参考:
 * https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97
 */
tinkerPatch {
    //oldApk ="${bakPath}/${appName}/app-release.apk"
    ignoreWarning = false
    useSign = true
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex"]
        loader = []
    }
    lib {
        pattern = ["lib/*/*.so"]
    }
    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = []
        largeModSize = 100
    }
    packageConfig {
    }
    sevenZip {
        zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
//        path = "/usr/local/bin/7za"
    }
    buildConfig {
        keepDexApply = false
        //tinkerId = "1.0.1-base"
        //applyMapping = "${bakPath}/${appName}/app-release-mapping.txt" //  可选，设置mapping文件，建议保持旧apk的proguard混淆方式
        //applyResourceMapping = "${bakPath}/${appName}/app-release-R.txt" // 可选，设置R.txt文件，通过旧apk文件保持ResId的分配
    }
}

```

(5).在app的build.gradle中应用tinker插件脚本

```

apply from: 'tinker-support.gradle'
```

(6).在app的自定义Application中初始化bugly

```
@Override
public void onCreate() {
    super.onCreate();
      //初始化bugly
//参数1：当前application
//参数2：在bugly平台申请的App Id
//参数3：是否是测试
//参数4：App的版本号
//参数5：是否需要tinker热修复功能
//参数6：补丁下载，应用等状态回调
BuglyUtil.init(this, BuildConfig.buglyId, BuildConfig.LOG_DEBUG, BuildConfig.VERSION_NAME, true, new BuglyUtil.TinkerHotFixPathcListener() {
    @Override
    public void onPatchURL(String patchFile) {
    //补丁下载路径
    }
    @Override
    public void onDownloadProgress(long savedLength, long totalLength) {
    //补丁下载进度
    }
   @Override
    public void onDownloadSuccess() {
    //下载成功
    }
    @Override
    public void onDownloadFailure() {
    //下载失败
    }
    @Override
    public void onApplySuccess() {
    //应用成功
    }
    @Override
    public void onApplyFailure() {
    //应用失败
    }
    @Override
    public void onPatchRollback() {
    //应用失败
    }
});
    }
```

**3.生成热修复补丁**

要生成补丁，必须要有基准包。所以每次发布之后，要做好每个版本代码的备份。

（1）.用线上需要修复bug的版本的备份代码编译生成基准包，基准包路径见下图
	![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165209402.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
（2）.在备份代码里面修复bug

（3）.在tinker-support.gradle文件中配置tinker对应信息

3.1：配置基准包的目录，基准包目录见左侧，复制左侧的文件夹名称黏贴即可

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165243852.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

3.2：配置基准包中各文件的路径，负责对应文件的名称，覆盖路径最后绿色部分即可
见下图3-2-3。

3.3: 配置tinkerId。基准包和补丁包的tinkerId必须不同，且保持唯一性，否则无法生成补丁。（注：基准包在发版的时候，要配置一个tinkerId）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165329714.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
（图3-2-3）

正式发版包tinkerId命名规则：
release-a.b.c.d ——a.c.d为visonName。
补丁包tinkerId命名规则：
fix-a.b.c.d 命名规则 a.b.c,为visonName。d每次+1
例如：tinkerId = "fix-2.1.1.2" 


（图3-2-3）

（4）.在Gradle的tinker-support下，选定对应的版本，进行编译生成补丁。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165523932.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

（5）.在app的build--outputs-patch中获取补丁包patch_signed_7zip.apk

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165554230.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)


**4.热修复补丁下发**
在bugly平台，选择对应的产品，依次操作：应用升级——热更新——发布新补丁——选择文件——全量设备——输入备注——立即下发。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2018112216575796.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

**5.热修复补丁生效**
补丁下发后，需要10分钟左右可以下发成功。安装了对应该补丁版本的基准包App，在杀死进程重启后补丁生效。

**6.在bugly申请AppId** 
Bugly网址：https://bugly.qq.com/v2/index

6.1新建产品
![在这里插入图片描述](https://img-blog.csdnimg.cn/2018112216593065.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

6.2填写产品名称、平台选Android、类型选软件-实用工具、填写描述、图标确定
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165942189.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
6.3 获取App ID 去应用里面配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122165953678.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)


**7.在bugly查看异常上报信息**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122170118258.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122170139625.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122170157736.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181122170206816.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JlaWJhb2tvbmdtaW5n,size_16,color_FFFFFF,t_70)

**8.注意事项**

8.1：tinker与instant run的兼容问题？
在debug的时候，不能使用instant run功能，在设置中禁用，把所有的勾选项清楚即可。
在release的时候，可以正常使用instant run

8.2：sign签名注意事项：
接入热修复后，签名信息要在buildTypes下的debug,release等环境配置，不能再渠道productFlavors中配置签名，因为在用命令打包补丁的时候无法读取到渠道中的签名信息，这样会导致包的签名和补丁的签名不一致，无法实现热修复。

8.3：备份每个版本的正式发版包代码。生成补丁包，要在基准包的基础上生成。

8.4：需要集成升级SDK版本1.3.0以上版本才支持加固。经过测试的加固产品：腾讯乐固、爱加密、梆梆加固、360加固（SDK 1.3.1之后版本支持）其他产品需要大家进行验证。

8.5：Tinker无法动态更新一些问题。
无法更新AndroidManifest.xml，例如添加Android组件。
不支持一些带有os版android-21的三星机型。
由于Google Play开发者分发协议，我们无法动态更新我们的apk。



