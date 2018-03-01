package com.example.mymemo;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by xianglei on 2018/2/12.
 */

public class Directory {

    public static final String VIDEO_DIRECTORY = "videos";

    public static final String AUDIO_DIRECTORY = "audios";

    public static final String PICTURE_DIRECTORY = "pictures";

    public static final String DIRECTORY = "directory";

    public static String getDirectory(Context context, int id, String directory) {
        String path = context.getExternalCacheDir().getPath();
        if(path != null) {
            File dir = new File(path + "/" + id);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            if(!directory.equals(DIRECTORY)) {
                File file = new File(dir + "/" + directory);
                if(!file.exists()) {
                    file.mkdirs();
                }
            }
            switch(directory) {
                case VIDEO_DIRECTORY:
                    return dir + "/" + directory + "/" + System.currentTimeMillis() + ".mp4";
                case AUDIO_DIRECTORY:
                    return dir + "/" + directory + "/" + System.currentTimeMillis() + ".mp3";
                case PICTURE_DIRECTORY:
                    return dir + "/" + directory + "/" + System.currentTimeMillis() + ".jpg";
                case DIRECTORY:
                    return dir.getAbsolutePath();
                default:
                    return null;
            }
        }
        return null;
    }

    public static String getAlbumPath(Context context, Intent data) {
        Uri uri = data.getData();
        String path = null;
        if(Build.VERSION.SDK_INT >= 19) {
            if(DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context);
                } else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse
                            ("content://downloads/public_downloads"), Long.valueOf(docId));
                    path = getImagePath(contentUri, null, context);
                }
            } else if("content".equalsIgnoreCase(uri.getScheme())) {
                path = getImagePath(uri, null, context);
            } else if("content".equalsIgnoreCase(uri.getScheme())) {
                path = uri.getPath();
            }
        } else {
            path = getImagePath(uri, null, context);
        }
        return path;
    }

    public static String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection,
                null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return path;
    }
}
