package com.kilogramm.mattermost.tools;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public String getFileType(String uri) {
        String filenameArray[] = uri.split("\\.");
        String extension = filenameArray[filenameArray.length - 1];
        return extension;
    }

    public String getPath(final Uri uri) {

        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(mContext, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://<span id=\"IL_AD1\" class=\"IL_AD\">downloads</span>/public_downloads"), Long.valueOf(id));

                return getDataColumn(mContext, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
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
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(mContext, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
//        return getRealPathFromURI(mContext, uri);

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

    private String getRealPathFromURI(Context context, Uri contentURI) {
        String result = null;
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
//                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                if (idx >= 0) {
                    result = cursor.getString(idx);
                }
                cursor.close();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFileNameByUri(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/Mattermost");
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                throw new IOException();
            }
        }

        // Can use:
        // File file = new File();
        // file.createNewFile();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",                          /* suffix */
                storageDir                       /* directory */
        );
    }

    public void removeFile(String path) {
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
    }

    public String getDownloadedFilesDir(){
        return  Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost";
    }

    public String getFileNameFromId(String fileId){
        Pattern pattern = Pattern.compile("\\/.*\\/(.*)");
        Matcher matcher = pattern.matcher(fileId);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    public String getFileNameFromIdDecoded(String fileId){
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

    public Intent createOpenFileIntent(String path){
        File file = new File(path);
        String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
                ? "*/*" : mimeType);
        return intent;
    }

    public String convertFileSize(long bytes){
        if (bytes > 1024 * 1024) {
            return String.format("%.2f Mb", ((float)bytes) / 1024 / 1024);
        } else if (bytes > 1024) {
            return String.format("%.2f Kb", ((float)bytes) / 1024);
        } else if (bytes < 0) {
            return null;
        } else {
            return String.format("%d b", bytes);
        }
    }

    public Observable<BitmapWithUri> getBitmap(Uri outputFileUri, Intent data){
        return Observable.create(subscriber -> {
            final boolean isCamera;
            isCamera = isCamera(data);
            Uri selectedImageUri;
            if (isCamera) {
                selectedImageUri = outputFileUri;
                //Bitmap factory
                BitmapFactory.Options options = new BitmapFactory.Options();
                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;
                final Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath(), options);
                subscriber.onNext(new BitmapWithUri(bitmap, selectedImageUri));
                subscriber.onCompleted();
                //mBinding.headerPicture.setImageBitmap(bitmap);
            } else {
                selectedImageUri = data == null ? null : data.getData();
                //Log.d("ImageURI", selectedImageUri.getLastPathSegment());
                // /Bitmap factory
                BitmapFactory.Options options = new BitmapFactory.Options();
                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;
                try {//Using Input Stream to get uri did the trick
                    InputStream input = mContext.getContentResolver().openInputStream(selectedImageUri);
                    Rect rect = new Rect(0,0,0,0);
                    final Bitmap bitmap = BitmapFactory.decodeStream(input,rect,options);
                    subscriber.onNext(new BitmapWithUri(bitmap, selectedImageUri));
                    subscriber.onCompleted();
                } catch (FileNotFoundException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private boolean isCamera(Intent data) {
        boolean isCamera;
        if (data == null) {
            isCamera = true;
        } else {
            final String action = data.getAction();
            if (action == null) {
                isCamera = false;
                if(data.getData()==null){
                    isCamera = true;
                }
            } else {
                isCamera = true;
            }
        }
        return isCamera;
    }


    public String getFileByUri(Uri imageUri) {
        String path;
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");

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


    public class BitmapWithUri{
        private Bitmap bitmap;
        private Uri uri;

        public BitmapWithUri(Bitmap bitmap, Uri uri) {
            this.bitmap = bitmap;
            this.uri = uri;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Uri getUri() {
            return uri;
        }
    }

    public String getImageUrl(String id) {
        if (id != null) {
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/teams/"
                    + MattermostPreference.getInstance().getTeamId()
                    + "/files/get" + id;
        } else {
            return "";
        }
    }
}
