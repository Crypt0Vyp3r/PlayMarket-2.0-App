package com.blockchain.store.playmarket;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.blockchain.store.playmarket.data.content.AppsDispatcher;
import com.blockchain.store.playmarket.data.content.AppsManager;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.orhanobut.hawk.Hawk;

import io.ethmobile.ethdroid.KeyManager;

/**
 * Created by Crypton04 on 24.01.2018.
 */

public class Application extends MultiDexApplication {
    public static KeyManager keyManager;
    private static AppsDispatcher appsDispatcher;
    private static AppsManager appsManager;
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MultiDex.install(this);
        ToastUtil.setContext(this);
        keyManager = KeyManager.newKeyManager(getFilesDir().getAbsolutePath());
        AccountManager.setKeyManager(keyManager);
        Hawk.init(this);

    }

    public static AppsDispatcher getAppsDispatcher() {
        if (appsDispatcher == null) {
            appsDispatcher = new AppsDispatcher();
        }
        return appsDispatcher;
    }

    public static AppsManager getAppsManager() {
        if (appsManager == null) {
            appsManager = new AppsManager();
        }
        return appsManager;
    }

    public static Application getInstance() {
        return instance;
    }
}
