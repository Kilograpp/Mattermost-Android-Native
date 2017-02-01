package com.kilogramm.mattermost.tools;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

/**
 * Created by Evgeny on 13.10.2016.
 */
public class FileUtil {

    private Context mContext;

    private static FileUtil ourInstance;

    public static FileUtil getInstance() {
        return ourInstance;
    }

    public static void createInstance(Context context) {
        ourInstance = new FileUtil(context);
    }

    private FileUtil(Context context) {
        this.mContext = context;
    }

    public String getPath(final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(mContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(mContext, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(mContext, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(mContext, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public String getFileNameByUri(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = getFileExtensionFromUrl(url, false);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

    public void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getDownloadedFilesDir() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost";
    }

    public String getFileNameFromIdDecoded(String fileId) {
        Pattern pattern = Pattern.compile("\\/.*\\/(.*)");
        Matcher matcher = pattern.matcher(fileId);
        if (matcher.matches()) {
            try {
                return URLDecoder.decode(matcher.group(1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return fileId;
            }
        }
        return fileId;
    }

    public Intent createOpenFileIntent(String path) {
        File file = new File(path);
        String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
                ? "*/*" : mimeType);
        return intent;
    }

    private Intent createOpenMattertestFileIntent(String fileName) {
        String path = getDownloadedFilesDir() + File.separator + fileName;
        File file = new File(path);
        String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
                ? "*/*" : mimeType);
        return intent;
    }

    public void startOpenFileIntent(Context context, String fileName) {
        Intent intent = createOpenMattertestFileIntent(fileName);
        if (intent != null && intent.resolveActivityInfo(MattermostApp.getSingleton()
                .getApplicationContext().getPackageManager(), 0) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context,
                    context.getString(R.string.no_suitable_app),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String convertFileSize(long bytes) {
        if (bytes > 1024 * 1024) {
            return String.format("%.2f Mb", ((float) bytes) / 1024 / 1024);
        } else if (bytes > 1024) {
            return String.format("%.2f Kb", ((float) bytes) / 1024);
        } else if (bytes < 0) {
            return null;
        } else {
            return String.format("%d b", bytes);
        }
    }

    public Observable<Bitmap> getBitmap(String filePath, int inSampleSize) {
        return Observable.create(subscriber -> {
            File file = new File(filePath);
            if (!file.exists()) {
                subscriber.onError(new Throwable("File does not exist"));
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;
                final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        });
    }

    public String getFileByUri(Uri imageUri) {
        String path;
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
            if(!dir.exists()){
                dir.mkdirs();
            }

            path = dir.getAbsolutePath() + File.separator + "img_" + System.currentTimeMillis() + ".jpg";
            InputStream input = mContext.getContentResolver().openInputStream(imageUri);
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            byte[] buffer = new byte[32 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, read);
            }
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }

    public String getFileExtensionFromUrl(String url, boolean withDot) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            if (!filename.isEmpty()) {
                Pattern pattern = Pattern.compile("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+");
                Matcher matcher = pattern.matcher(filename);
                if (matcher.find()) {
                    int dotPos = filename.lastIndexOf('.');
                    if (0 <= dotPos) {
                        if (withDot) return filename.substring(dotPos);
                        else return filename.substring(dotPos + 1);
                    }
                }
            }
        }
        return "";
    }
}
