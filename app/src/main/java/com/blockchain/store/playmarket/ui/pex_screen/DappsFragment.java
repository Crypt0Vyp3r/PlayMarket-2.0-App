package com.blockchain.store.playmarket.ui.pex_screen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blockchain.store.dao.interfaces.Callbacks;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.dapps.Web3View;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.DialogManager;

import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Transaction;
import org.json.JSONObject;
import org.web3j.tx.TransactionManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;


public class DappsFragment extends Fragment {
    private static final String TAG = "DappsFragment";
    private static final String IS_OPEN_FOR_DEX_KEY = "is_open_for_dex";

    @BindView(R.id.web_view) Web3View webView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private boolean isOpenForDex = false;

    public static DappsFragment newInstance() {
        Bundle args = new Bundle();
        DappsFragment fragment = new DappsFragment();
        args.putBoolean(IS_OPEN_FOR_DEX_KEY, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dapps, container, false);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            isOpenForDex = getArguments().getBoolean(IS_OPEN_FOR_DEX_KEY, false);
        }
        setWebView();
        return view;
    }

    private void setWebView() {
        DWebView.setWebContentsDebuggingEnabled(true);
        webView.setChainId(4);
        webView.addJavascriptObject(new JsApi(getActivity()), "");
//        webView.loadUrl("https://dapps.playmarket.io/");

        if (isOpenForDex) {
            webView.loadUrl(Constants.PAX_URL);
        } else {
            webView.loadUrl("https://testdex.playmarket.io/");
        }
//        webView.loadUrl("http://192.168.88.230:8080/");
    }

//    @Override
//    public void onBackPressed() {
//        if (webView.canGoBack()) {
//            webView.goBack();
//        } else {
//            super.onBackPressed();
//        }
//    }

    public class JsApi {
        private Context context;

        public JsApi(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void getAccounts(Object abc, CompletionHandler handler) {
            Log.d(TAG, "getAccounts() called with: handler = [" + AccountManager.getAccount().getAddress().getHex() + "]" + abc);
            handler.complete(AccountManager.getAccount().getAddress().getHex());
        }


        @JavascriptInterface
        public void signTx(Object tx, CompletionHandler handler) {
            Log.d(TAG, "signTx() called with: tx = [" + tx + "], handler = [" + handler + "]");
            Toast.makeText(context, tx + " received", Toast.LENGTH_SHORT).show();
            /*
            1. show dialog;
            2. sign tx
            *
            * */

//

            handler.complete("0x7efebda7e647161a908c48aaa06243dff64fb20cd6b7ad4d377abad6e63419d2");
        }


        @JavascriptInterface
        public void sendTransaction(Object tx, CompletionHandler handler) {

            Log.d(TAG, "sendTransaction() called with: tx = [" + tx + "], handler = [" + handler + "]");
            JSONObject jsonObject = null;
            try {

                Transaction transaction2 = new Transaction(jsonObject.toString());
                Log.d(TAG, "sendTransaction: ");

            } catch (Exception e) {
                e.printStackTrace();
            }


            new DialogManager().showConfirmDialog(context, new Callbacks.PasswordCallback() {
                @Override
                public void onAccountUnlock(Boolean isUnlock) {

                }
            });

            handler.complete("0x7efebda7e647161a908c48aaa06243dff64fb20cd6b7ad4d377abad6e63419d2");

        }
    }
}
