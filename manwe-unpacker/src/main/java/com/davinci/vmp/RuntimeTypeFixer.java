package com.davinci.vmp;

import java.util.*;
import java.util.stream.Collectors;

public class RuntimeTypeFixer {
    public static Map<String, String> realTypeMap = new HashMap<>();

    private static final String DESC_BYTE = "B";
    private static final String DESC_CHAR = "C";
    private static final String DESC_DOUBLE = "D";
    private static final String DESC_FLOAT = "F";
    private static final String DESC_INT = "I";
    private static final String DESC_LONG = "J";
    private static final String DESC_SHORT = "S";
    private static final String DESC_BOOLEAN = "Z";
    private static final String DESC_STRING = wrap("java/lang/String");

    private static String wrap(String clazz) {
        return "L" + clazz + ";";
    }

    public static final String DESC_NOTFOUND = wrap("com/davinci/LovePdd");

    static {
        add("android/accounts/Account", "name", DESC_STRING);
        add("android/accounts/Account", "type", DESC_STRING);
        add("android/content/pm/ActivityInfo", "enabled", DESC_BOOLEAN);
        add("android/content/pm/ActivityInfo", "exported", DESC_BOOLEAN);
        add("android/content/pm/ActivityInfo", "name", DESC_STRING);
        add("android/content/pm/ActivityInfo", "packageName", DESC_STRING);
        add("android/content/pm/ActivityInfo", "permission", DESC_STRING);
        add("android/content/pm/ApplicationInfo", "flags", DESC_INT);
        add("android/content/pm/ApplicationInfo", "sourceDir", DESC_STRING);
        add("android/content/pm/ApplicationInfo", "uid", DESC_INT);
        add("android/content/pm/ApplicationInfo", "targetSdkVersion", DESC_INT);
        add("android/content/pm/PackageInfo", "versionCode", DESC_INT);
        add("android/content/pm/PackageInfo", "versionName", DESC_STRING);
        add("android/content/pm/ResolveInfo", "activityInfo", wrap("android/content/pm/ActivityInfo"));
        add("android/content/pm/ResolveInfo", "resolvePackageName", DESC_STRING);
        add("android/content/pm/ResolveInfo", "serviceInfo", wrap("android/content/pm/ServiceInfo"));
        add("android/content/pm/ServiceInfo", "metaData", wrap("android/os/Bundle"));
        add("android/content/pm/ServiceInfo", "name", DESC_STRING);
        add("android/content/pm/ServiceInfo", "packageName", DESC_STRING);
        add("android/graphics/Bitmap$Config", "ARGB_8888", wrap("android/graphics/Bitmap$Config"));
        add("android/os/Build$VERSION", "SDK_INT", DESC_INT);
        add("android/os/Build$VERSION", "SECURITY_PATCH", DESC_STRING);
        add("android/os/Build", "MANUFACTURER", DESC_STRING);
        add("android/os/Build", "PRODUCT", DESC_STRING);
        add("android/os/Build", "BOARD", DESC_STRING);
        add("android/os/Build", "DISPLAY", DESC_STRING);
        add("android/os/Build", "FINGERPRINT", DESC_STRING);
        add("android/os/Build", "MODEL", DESC_STRING);
        add("android/os/Build", "SERIAL", DESC_STRING);
        add("android/os/Build", "BRAND", DESC_STRING);
        add("android/os/Build", "DEVICE", DESC_STRING);
        add("android/os/Build$VERSION", "RELEASE", DESC_STRING);
        add("android/app/ActivityManager$MemoryInfo", "availMem", DESC_LONG);
        add("android/os/Bundle", "EMPTY", wrap("android/os/Bundle"));
        add("android/os/Message", "arg1", DESC_INT);
        add("android/os/Message", "what", DESC_INT);
        add("android/util/Pair", "first", wrap("java/lang/Object"));
        add("android/util/Pair", "second", wrap("java/lang/Object"));
        add("android/view/WindowManager$LayoutParams", "dimAmount", DESC_FLOAT);
        add("android/view/WindowManager$LayoutParams", "flags", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "format", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "gravity", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "height", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "packageName", DESC_STRING);
        add("android/view/WindowManager$LayoutParams", "token", wrap("android/os/IBinder"));
        add("android/view/WindowManager$LayoutParams", "type", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "width", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "windowAnimations", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "x", DESC_INT);
        add("android/view/WindowManager$LayoutParams", "y", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/message/Message0", "name", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/message/Message0", "payload", wrap("org/json/JSONObject"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/threadpool/ThreadBiz", "CS", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/threadpool/ThreadBiz"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "IRREGULAR_PROCESS_START", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "PROCESS_START", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "name", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/b", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/b", "c", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/c", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/c", "c", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/e", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/e", "c", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/f", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/powersave/internal/f", "c", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/b", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/c", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/c", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/f", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/f", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/g", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/buildin/stepInfo/internal/g", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/e", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/e", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/h", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/h", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/i", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/i", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/j", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/j", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_impl_interface/utils/AliveStartUpConstants", "KEY_SCENE", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_impl_interface/utils/AliveStartUpConstants", "KEY_START_ACC_BY_RIVAN", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_impl_interface/utils/AliveStartUpConstants", "KEY_USE_SETTINGS_ACCOUNT", DESC_NOTFOUND);
        add("java/lang/Integer", "TYPE", wrap("java/lang/Class"));
        add("java/lang/Long", "TYPE", wrap("java/lang/Class"));
        add("java/nio/charset/StandardCharsets", "UTF_8", DESC_STRING);
        add("java/util/Locale", "ENGLISH", wrap("java/util/Locale"));
        add("java/util/concurrent/TimeUnit", "MILLISECONDS", wrap("java/util/concurrent/TimeUnit"));
        add("java/util/concurrent/TimeUnit", "SECONDS", wrap("java/util/concurrent/TimeUnit"));
        add("java/util/concurrent/TimeUnit", "MICROSECONDS", wrap("java/util/concurrent/TimeUnit"));
        add("com/xunmeng/pinduoduo/alive/unify/ability/interfaces/schema/ryze/RyzeRequest", "uri", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/g", "b", DESC_STRING);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/g", "a", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "SCREEN_ON", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "USER_PRESENT", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "SCREEN_OFF", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "FP_PERM_READY", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType"));
        add("java/io/File", "separator", DESC_STRING);
        add("java/io/File", "separatorChar", DESC_CHAR);
        add("android/content/pm/ProviderInfo", "name", DESC_STRING);
        add("java/util/concurrent/TimeUnit", "HOURS", wrap("java/util/concurrent/TimeUnit"));
        add("java/util/concurrent/TimeUnit", "NANOSECONDS", wrap("java/util/concurrent/TimeUnit"));
        add("android/os/Message", "obj", wrap("java/lang/Object"));
        add("android/view/WindowManager$LayoutParams", "alpha", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "mode", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "recoverEnable", DESC_BOOLEAN);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "PDD_ID_CONFIRM", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "blackScene", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "blackResultType", wrap("com.xunmeng.pinduoduo.alive.strategy.interfaces.adapter.intf.msc.BlackResultType"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/msc/BlackResultType", "TRUE", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/msc/BlackResultType"));
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "specificCmptList", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "mainProcPullUpCmptBlackList", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "pullUpCmptBlackList", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/biz/boush/BoushConfig", "trackExpKey", wrap("java/lang/String"));
        add("java/util/Locale", "CHINA", wrap("java/util/Local"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "START_SKY_CASTLE", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "DPPL_EVENT", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "ITDM_EVENT", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("android/content/pm/PackageInfo", "applicationInfo", wrap("android/content/pm/ApplicationInfo"));
        add("android/content/pm/ActivityInfo", "applicationInfo", wrap("android/content/pm/ApplicationInfo"));
        add("android/content/pm/PackageInfo", "packageName", DESC_STRING);
        add("android/content/pm/PackageInfo", "activities", "[Landroid/content/pm/ActivityInfo");
        add("android/content/pm/PackageInfo", "services", "[Landroid/content/pm/ServiceInfo");
        add("android/content/pm/PackageInfo", "receivers", "[Landroid/content/pm/ActivityInfo");
        add("android/content/pm/PackageInfo", "signatures", "[Landroid/content/pm/Signature");
        add("android/content/pm/PackageInfo", "requestedPermissions", "[java/lang/String");
        add("android/content/pm/PackageInfo", "firstInstallTime", DESC_LONG);
        add("android/content/pm/ApplicationInfo", "metaData", wrap("android/app/Bundle"));
        add("android/net/wifi/ScanResult", "SSID", DESC_STRING);
        add("android/net/wifi/ScanResult", "BSSID", DESC_STRING);
        add("android/net/wifi/ScanResult", "level", DESC_INT);
        add("android/net/wifi/WifiConfiguration", "SSID", DESC_STRING);
        add("android/net/wifi/WifiConfiguration", "BSSID", DESC_STRING);
        add("android/net/wifi/WifiConfiguration", "FQDN", DESC_STRING);
        add("android/net/wifi/WifiConfiguration", "hiddenSSID", DESC_BOOLEAN);
        add("android/net/wifi/WifiConfiguration", "providerFriendlyName", DESC_STRING);
        add("android/content/res/Configuration", "locale", wrap("java/util/Locale"));
        add("android/app/ActivityManager$RunningServiceInfo", "process", DESC_STRING);
        add("android/content/pm/PackageInfo", "providers", "[Landroid/content/pm/ProviderInfo");
        add("android/content/pm/ProviderInfo", "authority", DESC_STRING);
        add("android/content/pm/ProviderInfo", "packageName", DESC_STRING);
        add("android/content/pm/ProviderInfo", "metaData", DESC_STRING);
        add("android/content/pm/ProviderInfo", "readPermission", DESC_STRING);
        add("android/content/pm/ProviderInfo", "processName", DESC_STRING);
        add("android/content/pm/ProviderInfo", "grantUriPermissions", DESC_BOOLEAN);
        add("com/xunmeng/pinduoduo/galaxy/plugin/location_info/tracker/VivoTracker", "fpSceneSet", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "ON_FOREGROUND", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "STARTUP_IDLE", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "DIEL_EVENT", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "STOP_SKY_CASTLE", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("android/content/pm/ServiceInfo", "permission", DESC_STRING);
        add("android/content/pm/ServiceInfo", "enabled", DESC_BOOLEAN);
        add("android/content/pm/ServiceInfo", "exported", DESC_BOOLEAN);
        add("android/graphics/Rect", "top", DESC_INT);
        add("android/graphics/Rect", "left", DESC_INT);
        add("android/content/pm/ApplicationInfo", "packageName", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "BACKGROUND_1MIN_TIMER", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("android/app/ActivityManager$RunningAppProcessInfo", "processName", DESC_STRING);
        add("android/app/ActivityManager$RunningAppProcessInfo", "importance", DESC_INT);
        add("android/app/ActivityManager$RunningAppProcessInfo", "pid", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "SCREEN_RECORD_START", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "SCREEN_RECORD_STOP", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("java/lang/System", "out", wrap("java.io.PrintStream"));
        add("android/view/WindowManager$LayoutParams", "layoutInDisplayCutoutMode", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "delayToNextSceneInMs", DESC_INT);
        add("android/os/Message", "replyTo", wrap("android/os/Messenger"));
        add("android/app/Notification", "contentIntent", wrap("android/content/Intent"));
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/ability/ReportInfoItem", "infoType", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/ability/ReportInfoItem", "dataField", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/ability/ReportInfoItem", "isDataArray", DESC_BOOLEAN);
        add("android/app/Notification", "extras", wrap("android/app/Bundle"));
        add("android/graphics/Bitmap$CompressFormat", "PNG", DESC_INT);
        add("android/graphics/Bitmap$CompressFormat", "JPEG", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "ON_BACKGROUND", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/threadpool/ThreadBiz", "BC", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/adapter/intf/threadpool/ThreadBiz"));
        add("android/os/Environment", "DIRECTORY_DOWNLOADS", DESC_STRING);
        add("com/xunmeng/pinduoduo/galaxy/plugin/location_info/tracker/XiaomiTracker", "fpSceneSet", wrap("java/util/Set"));
        add("android/content/pm/ApplicationInfo", "nativeLibraryDir", DESC_STRING);
        add("android/content/pm/ApplicationInfo", "publicSourceDir", DESC_STRING);
        add("android/content/pm/ResolveInfo", "providerInfo", wrap("android/content/pm/ProviderInfo"));
        add("com/xunmeng/pinduoduo/galaxy/plugin/location_info/tracker/HonorTracker", "fpSceneSet", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "retryIfScreenOff", DESC_BOOLEAN);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "checkResultDelayInMs", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "retryDelayInMs", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "silentIntervalWhenFailInMin", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "maxStartLimitInSilentInterval", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "silentIntervalInMin", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "acceptedTriggerEvents", DESC_INT);
        add("android/app/ActivityManager$RunningAppProcessInfo", "uid", DESC_INT);
        add("com/xunmeng/pinduoduo/galaxy/plugin/location_info/tracker/OppoTracker", "fpSceneSet", wrap("java/util/Set"));
        add("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType", "POWER_CONNECTED", wrap("com/xunmeng/pinduoduo/alive/strategy/interfaces/event/TriggerEventType")); //TODO
        add("com/xunmeng/pinduoduo/alive/strategy/biz/plugin/purgeV2/ProviderSceneConfig", "retryCnt", DESC_INT);
        add("com/xunmeng/pinduoduo/alive/unify/ability/framework/schema/common/IntentRequest", "caller", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/config/CollectorConfigItem", "infoType", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/config/CollectorConfigItem", "dataField", DESC_STRING);
        add("com/xunmeng/pinduoduo/alive/unify/ability/dynamic/abilities/dataCollect/config/CollectorConfigItem", "isDataArray", DESC_BOOLEAN);
        add("android/net/DhcpInfo", "gateway", DESC_INT);
        add("android/provider/ContactsContract$CommonDataKinds$Phone", "CONTENT_URI", wrap("android/net/Uri"));
        add("com/xunmeng/pinduoduo/alive_adapter_sdk/BotMMKV$BotMMKVModuleSource", "HX", wrap("com/xunmeng/pinduoduo/alive_adapter_sdk/BotMMKV$BotMMKVModuleSource"));
        add("com/xunmeng/pinduoduo/alive_adapter_sdk/BotMMKV$BotProcessMode", "multiProcess", wrap("com/xunmeng/pinduoduo/alive_adapter_sdk/BotMMKV$BotProcessMode"));
        add("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType", "SHORTCUT", wrap("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType"));
        add("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType", "WIDGET", wrap("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType"));
        add("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType", "MINUS_SCREEN_DETECT", wrap("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType"));
        add("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType", "LAUNCHER", wrap("com/xunmeng/pinduoduo/market_common/plugin/IPluginAbility$AbilityType"));
        add("com/xunmeng/pinduoduo/smart_widget_plugin/minus_screen/vivo/VivoMinusScreenDetectV28", "mContext", wrap("android/content/Context"));
        add("com/xunmeng/pinduoduo/smart_widget_plugin/minus_screen/vivo/VivoMinusScreenDetect", "isInMinusScreen", DESC_BOOLEAN);
        add("android/content/pm/LauncherApps$PinItemRequest", "CREATOR", wrap("android/os/Parcelable$CREATOR"));
        add("android/appwidget/AppWidgetProviderInfo", "provider", wrap("android/content/ComponentName"));
        add("com/xunmeng/pinduoduo/smart_widget_plugin/minus_screen/xm/XmMinusScreenDetect", "isInMinusScreen", DESC_BOOLEAN);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/f", "b", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/f", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/k", "a", DESC_NOTFOUND);
        add("com/xunmeng/pinduoduo/android_pull_ability_comp/pullstartup/k", "b", DESC_NOTFOUND);
    }


    public static void parseVmpClazz(ManweVmpClazz manweVmpClazz) {
        String declaringClazzName = manweVmpClazz.clazzName;
        manweVmpClazz.fieldMap.forEach((k, v) -> add(declaringClazzName, k, v.fieldType));
    }

    private static void add(String clazzName, String fieldName, String description) {
        realTypeMap.put(clazzName + ':' + fieldName, description);
    }

    public static String resolve(String clazzName, String fieldName) {
        String ret = realTypeMap.getOrDefault(clazzName + ':' + fieldName, null);
        if (ret == null) {
            realTypeMap.put(clazzName + ':' + fieldName, DESC_NOTFOUND);
            System.out.println("WARNING: type not found for " + clazzName + ':' + fieldName);
            ret = realTypeMap.getOrDefault(clazzName + ':' + fieldName, null);
        }
        return ret;
    }

    public static ManweVmpConstantPoolItem[] addStringToPool(ManweVmpConstantPool pool) {
        ManweVmpConstantPoolItem[] records = pool.records;
        Set<String> toBeAdded = new HashSet<>(realTypeMap.values());
        Set<String> currentPool = Arrays.stream(records)
                .filter(x -> x.type == ManweVmpConstantPoolItem.CONSTANT_UTF8)
                .map(ManweVmpConstantPoolItem::asString)
                .collect(Collectors.toSet());
        // 去重，找到需要补充的
        toBeAdded.removeAll(currentPool);

        Object[] tailAppendList = toBeAdded.stream()
                .map(x -> new ManweVmpConstantPoolItem(null, ManweVmpConstantPoolItem.CONSTANT_UTF8, x))
                .toArray();

        ManweVmpConstantPoolItem[] finalArray = new ManweVmpConstantPoolItem[records.length + tailAppendList.length];
        System.arraycopy(records, 0, finalArray, 0, records.length);
        System.arraycopy(tailAppendList, 0, finalArray, records.length, tailAppendList.length);
        return finalArray;
    }

    public static ManweVmpConstantPoolItem[] addSingleStringToPool(ManweVmpConstantPool pool, String target) {
        ManweVmpConstantPoolItem[] records = pool.records;
        Set<String> toBeAdded = new HashSet<>();
        toBeAdded.add(target);

        Object[] tailAppendList = toBeAdded.stream()
                .map(x -> new ManweVmpConstantPoolItem(null, ManweVmpConstantPoolItem.CONSTANT_UTF8, x))
                .toArray();

        ManweVmpConstantPoolItem[] finalArray = new ManweVmpConstantPoolItem[records.length + tailAppendList.length];
        System.arraycopy(records, 0, finalArray, 0, records.length);
        System.arraycopy(tailAppendList, 0, finalArray, records.length, tailAppendList.length);
        return finalArray;
    }
}
