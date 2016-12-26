package com.kilogramm.mattermost.model.entity.filetoattacth;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kepar on 17.10.16.
 */

public class DownloadFile extends AsyncTask<Uri, Void, String> {

    private Context mContext;
    private DownloadFileListener mDownloadFileListener;

    public DownloadFile(Context context, DownloadFileListener downloadFileListener) {
        this.mContext = context;
        this.mDownloadFileListener = downloadFileListener;
    }

    @Override
    protected String doInBackground(Uri... params) {
        if (params == null || params.length == 0) return null;
        Uri uri = params[0];
        String name = FileUtil.getInstance().getFileNameByUri(uri);
        String path;
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
            if (!dir.exists()) {
                if (!dir.mkdirs()){
                    return null;
                }
            }

            path = dir.getAbsolutePath() + File.separator + name;
            try (InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                 OutputStream output = new FileOutputStream(path)) {
                byte[] buffer = new byte[32 * 1024];
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }

    @Override
    protected void onPostExecute(String filePath) {
        super.onPostExecute(filePath);
        if (!TextUtils.isEmpty(filePath)) {
            mDownloadFileListener.onDownloadedFile(filePath);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_retrieving_data), Toast.LENGTH_LONG).show();
            mDownloadFileListener.onError();
        }
    }

    public interface DownloadFileListener {
        void onDownloadedFile(String filePath);
        void onError();
    }
}