package com.blockchain.store.playmarket.installer;

import android.content.Context;
import android.util.Log;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.utilities.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class QueuedInstallation {
    private static final String TAG = "SAIQueuedInstallation";
    private static final String FILE_NAME_PAST = "testkey.past";
    private static final String FILE_NAME_PRIVATE_KEY = "testkey.pk8";

    private Context mContext;
    private File mZipWithApkFiles;
    private boolean mShouldExtractZip;
    private List<File> mApkFiles;
    private File mCacheDirectory;
    private long mId;

    QueuedInstallation(Context c, List<File> apkFiles, long id) {
        mContext = c;
        mApkFiles = apkFiles;
        mId = id;
    }

    QueuedInstallation(Context c, File zipWithApkFiles, long id) {
        mContext = c;
        mZipWithApkFiles = zipWithApkFiles;
        mShouldExtractZip = true;
        mId = id;
    }

    long getId() {
        return mId;
    }

    List<File> getApkFiles() throws Exception {
        if (mShouldExtractZip)
            extractZip();

        return mApkFiles;
    }

    void clear() {
        if (mCacheDirectory != null) {
            deleteFile(mCacheDirectory);
        }
    }

    private void deleteFile(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles())
                deleteFile(child);
        }
        f.delete();
    }

    private void extractZip() throws Exception {
        createCacheDir();

        ZipFile zipFile = new ZipFile(mZipWithApkFiles);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        mApkFiles = new ArrayList<>(zipFile.size());

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if (entry.isDirectory() || !entry.getName().endsWith(".apk"))
                throw new IllegalArgumentException(mContext.getString(R.string.installer_error_zip_contains_non_apks));


            File tempApkFile = new File(mCacheDirectory, entry.getName());

            FileOutputStream outputStream = new FileOutputStream(tempApkFile);
            InputStream inputStream = zipFile.getInputStream(entry);
            IOUtils.copyStream(inputStream, outputStream);

            outputStream.close();
            inputStream.close();

            mApkFiles.add(tempApkFile);
        }
        zipFile.close();
    }

    private void checkAndPrepareSigningEnvironment() throws Exception {
        File signingEnvironment = getSigningEnvironmentDir();
        File pastFile = new File(signingEnvironment, FILE_NAME_PAST);
        File privateKeyFile = new File(signingEnvironment, FILE_NAME_PRIVATE_KEY);

        if (pastFile.exists() && privateKeyFile.exists())
            return;

        Log.d(TAG, "Preparing signing environment...");
        signingEnvironment.mkdir();

        IOUtils.copyFileFromAssets(mContext, FILE_NAME_PAST, pastFile);
        IOUtils.copyFileFromAssets(mContext, FILE_NAME_PRIVATE_KEY, privateKeyFile);
    }

    private File getSigningEnvironmentDir() {
        return new File(mContext.getFilesDir(), "signing");
    }

    private void createCacheDir() {
        mCacheDirectory = new File(mContext.getCacheDir(), String.valueOf(System.currentTimeMillis()));
        mCacheDirectory.mkdirs();
    }
}
