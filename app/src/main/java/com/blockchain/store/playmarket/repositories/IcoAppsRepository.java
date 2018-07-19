package com.blockchain.store.playmarket.repositories;

import android.util.Log;

import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.AccountInfoResponse;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.BalanceIco;
import com.blockchain.store.playmarket.data.entities.PurchaseAppResponse;
import com.blockchain.store.playmarket.utilities.AccountManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IcoAppsRepository {
    private IcoAppsRepositoryCallback callback;

    public void getIcoApps(IcoAppsRepositoryCallback callback) {
        this.callback = callback;
        RestApi.getServerApi().getIcoApps()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .doOnSubscribe(()->this.callback.onIcoAppsSubscribed())
                .doOnTerminate(()->this.callback.onIcoAppsTerminated())
                .subscribe(this::onIcoAppsReady, this::onIcoAppsFailed);

//        RestApi.getServerApi().getIcoApps()
//                .concatMap(apps -> {
//                    //String[] icoAddressesArr = new String[apps.size()];
//                    //for (int i = 0; i<apps.size(); i++) { icoAddressesArr[i] = apps.get(i).adrICO; }
//                    String icoAddresses = "[\"" + apps.get(0).adrICO + "\", " + "\"" + apps.get(1).adrICO + "\"]";
//                    return RestApi.getServerApi().getBalanceOf(icoAddresses, AccountManager.getAddress().getHex());
//                })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::onIcoAppsReady, this::onIcoAppsFailed);
    }

    private void onIcoAppsReady(ArrayList<App> apps) {
        ArrayList<String> icoAddressesArr = new ArrayList<>();
        for (int i = 0; i<apps.size(); i++) { icoAddressesArr.add(apps.get(i).adrICO); }
        String icoAddressesStr = arrayToString(icoAddressesArr);
        RestApi.getServerApi().getBalanceOf(icoAddressesStr, AccountManager.getAddress().getHex())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    for (int i = 0; i<apps.size(); i++){ apps.get(i).balanceIco = result.get(i); }
                    callback.onIcoAppsReady(apps);
                },
                error -> callback.onIcoAppsFailed(error));
    }

    private String arrayToString(ArrayList<String> arr){
        String result = "";
        for (int i = 0; i < arr.size(); i++){
            if (i == 0) result = "[\"" + arr.get(i) + "\", ";
            else if (i == arr.size() - 1) result = result + "\"" + arr.get(i) + "\"]";
            else result = result + "\"" + arr.get(i) + "\", ";
        }
        return result;
    }

    private void onIcoAppsFailed(Throwable throwable) {
        callback.onIcoAppsFailed(throwable);
    }

    public interface IcoAppsRepositoryCallback {
        void onIcoAppsReady(ArrayList<App> apps);

        void onIcoAppsFailed(Throwable throwable);

        default void onIcoAppsSubscribed() {

        }

        default void onIcoAppsTerminated() {

        }


    }
}
