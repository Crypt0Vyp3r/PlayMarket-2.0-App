package com.blockchain.store.playmarket.ui.experimental_settings_screen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.utilities.ipfs.IPFSDaemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExperimentalSettingsActivity extends AppCompatActivity {

    @BindView(R.id.download_ipfs_btn) Button downloadIpfs;
    IPFSDaemon ipfsDaemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental_settings);
        ButterKnife.bind(this);
        ipfsDaemon = new IPFSDaemon();
        setItems();
    }

    private void setItems() {
        if (ipfsDaemon.isDaemonDownloaded()) {
            downloadIpfs.setVisibility(View.GONE);
        } else {
            downloadIpfs.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.download_ipfs_btn)
    public void download_ipfs_btn() {
        ipfsDaemon.downloadDaemon();

    }

}