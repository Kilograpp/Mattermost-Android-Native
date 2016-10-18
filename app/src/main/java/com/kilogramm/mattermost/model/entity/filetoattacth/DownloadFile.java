package com.kilogramm.mattermost.model.entity.filetoattacth;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kepar on 17.10.16.
 */

public class DownloadFile extends AsyncTask<Uri,Void,String> {

    private Context context;
    private DownloadFileListener downloadFileListener;

    public DownloadFile(Context context, DownloadFileListener downloadFileListener) {
        this.context = context;
        this.downloadFileListener = downloadFileListener;
    }

    @Override
    protected String doInBackground(Uri... params) {
        if(params==null || params.length==0) return null;
        Uri uri = params[0];
        String name = FileUtil.getInstance().getFileNameByUri(uri);
        File test;
        try {
            test = FileUtil.getInstance().createTempFile(name);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            OutputStream output = new FileOutputStream(test);
            try {
                byte[] buffer = new byte[32 * 1024]; // or other buffer size
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } finally {
                output.close();
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return test.getPath();
    }

    @Override
    protected void onPostExecute(String filePath) {
        super.onPostExecute(filePath);
        if(!TextUtils.isEmpty(filePath)){
            downloadFileListener.onDownloadedFile(filePath);
        } else Toast.makeText(context,"An error occurred while retrieving data",Toast.LENGTH_LONG).show();
    }

    public interface DownloadFileListener{
        void onDownloadedFile(String filePath);
    }
}