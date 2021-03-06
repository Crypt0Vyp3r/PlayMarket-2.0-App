package com.blockchain.store.playmarket.check_transation_status_beta;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.util.Pair;

import com.blockchain.store.playmarket.BuildConfig;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.AppUpdateNotification;
import com.blockchain.store.playmarket.data.entities.PlayMarketUpdateNotification;
import com.blockchain.store.playmarket.notification.NotificationManager;
import com.blockchain.store.playmarket.repositories.MyAppsRepository;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.MyPackageManager;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

/**
 * Created by Crypton04 on 17.10.2018.
 */

public class CheckUpdateJobService extends JobService {
    private static final String TAG = "CheckUpdateJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Check update job service started");
        String s = MyPackageManager.prepareApplicationInfoForRequest();
        new MyAppsRepository().getApps()
                .subscribe(result -> onGetAppsReady(result, params),
                        throwable -> onGetAppsFailed(throwable, params));
        return true;
    }

    private void onGetAppsReady(Pair<ArrayList<App>, Integer> result, JobParameters params) {
        if (!result.first.isEmpty() && result.second != 0) {
            for (App app : result.first) {
                if (app.packageName.equalsIgnoreCase(getBaseContext().getPackageName())
                        && Integer.parseInt(app.version) > BuildConfig.VERSION_CODE) {
                    onPlayMarketNewVersionAvailable(app);
                }
            }

            onAppsUpdateAvailable(result.second);
        }
        jobFinished(params, false);
    }

    private void onGetAppsFailed(Throwable throwable, JobParameters params) {

        jobFinished(params, true);
        Log.d(TAG, "onGetAppsFailed() called with: throwable = [" + throwable + "], params = [" + params + "]");
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    private void onPlayMarketNewVersionAvailable(App app) {
        if (Hawk.get(Constants.SETTINGS_SHOW_PLAYMARKET_UPDATE_NOTIFICATION, true)) {
            PlayMarketUpdateNotification playMarketUpdateNotification = new PlayMarketUpdateNotification(app);
            NotificationManager.getManager().registerNewNotification(playMarketUpdateNotification);
        }


    }

    private void onAppsUpdateAvailable(int countOfAppsHasUpdate) {
        if (Hawk.get(Constants.SETTINGS_SHOW_UPDATE_NOTIFICATION, true)) {
            AppUpdateNotification playMarketUpdateNotification = new AppUpdateNotification(countOfAppsHasUpdate);
            NotificationManager.getManager().registerNewNotification(playMarketUpdateNotification);
        }

    }


}
