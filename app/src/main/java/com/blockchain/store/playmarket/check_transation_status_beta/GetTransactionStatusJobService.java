package com.blockchain.store.playmarket.check_transation_status_beta;

import android.app.job.JobParameters;
import android.os.PersistableBundle;
import android.util.Log;

import com.blockchain.store.playmarket.data.entities.TransactionNotification;
import com.blockchain.store.playmarket.notification.NotificationManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.TransactionPrefsUtil;
import com.orhanobut.hawk.Hawk;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.blockchain.store.playmarket.api.RestApi.BASE_URL_INFURA;

public class GetTransactionStatusJobService extends android.app.job.JobService {
    private static final String TAG = "TransactionJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        if (Hawk.get(Constants.SETTINGS_SHOW_TRANSACTION_UPDATE_NOTIFICATION, true)) {
            NotificationManager.getManager().registerNewNotification(getNotification(params));
        }
        PersistableBundle extras = params.getExtras();
        String transactionHash = extras.getString(Constants.JOB_HASH_EXTRA);
        Web3j build = Web3jFactory.build(new HttpService(BASE_URL_INFURA));
        build.ethGetTransactionReceipt(transactionHash).observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onTransactionReady(result, params),
                        throwable -> onTransactionError(throwable, params));

        return true;
    }

    private void onTransactionReady(EthGetTransactionReceipt result, JobParameters params) {
        if (result.hasError()) {
            return;
        }
        if (result.getTransactionReceipt() != null) {
            Log.d(TAG, "onTransactionReady: with result " + result.getTransactionReceipt().getStatus());
            try {
                TransactionPrefsUtil.updateModel(result.getTransactionReceipt());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String secondTransaction = params.getExtras().getString(Constants.JOB_SECOND_RAW_TX, null);
            Log.d(TAG, "onTransactionReady: check for second transaction: " + secondTransaction);
            if (secondTransaction != null) {
                sendSecondTransaction(secondTransaction, params);
            } else {
                if (result.getTransactionReceipt().getStatus().contains("1")) {
                    NotificationManager.getManager().downloadCompleteWithoutError(getNotification(params));
                } else {
                    NotificationManager.getManager().downloadCompleteWithError(getNotification(params), new Exception(""));
                }
            }
            jobFinished(params, false);
        } else {
            jobFinished(params, true);
        }

    }

    private void sendSecondTransaction(String secondTransaction, JobParameters params) {
        Log.d(TAG, "sendSecondTransaction() called with: secondTransaction = [" + secondTransaction + "], params = [" + params + "]");
        Web3j build = Web3jFactory.build(new HttpService(BASE_URL_INFURA));
        if (!secondTransaction.startsWith("0x")) {
            secondTransaction = "0x" + secondTransaction;
        }

        build.ethSendRawTransaction(secondTransaction).observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onSecondTransactionReady(result, params),
                        throwable -> onTransactionError(throwable, params));
    }

    private void onSecondTransactionReady(EthSendTransaction result, JobParameters params) {
        Log.d(TAG, "onSecondTransactionReady() called with: result = [" + result + "], params = [" + params + "]");
        String secondTransaction = params.getExtras().getString(Constants.JOB_SECOND_HASH_EXTRA, null);
        if (secondTransaction != null) {
            int transactionType = params.getExtras().getInt(Constants.JOB_TRANSACTION_TYPE_ORDINAL);
            JobUtils.scheduleSecondTransactionJob(this, secondTransaction, transactionType);
        }
    }

    private TransactionNotification getNotification(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        int transactionType = extras.getInt(Constants.JOB_TRANSACTION_TYPE_ORDINAL);
        return new TransactionNotification(params.getJobId(), transactionType);
    }

    private void onTransactionError(Throwable throwable, JobParameters params) {
        Log.d(TAG, "onTransactionError() called with: throwable = [" + throwable + "], params = [" + params + "]");
        jobFinished(params, true);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
