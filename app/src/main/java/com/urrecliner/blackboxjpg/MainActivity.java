package com.urrecliner.blackboxjpg;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String PATH_PACKAGE = "BlackBox";
    String PATH_EVENT_JPG = "eventJpg";
    String PATH_EVENT_PHOTO = "eventPhoto";
    String FORMAT_LOG_TIME = "yy-MM-dd HH.mm.ss.SSS";
    String FORMAT_DATE = "yy-MM-dd";
    SimpleDateFormat sdfDate = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
    SimpleDateFormat sdfLogTime = new SimpleDateFormat(FORMAT_LOG_TIME, Locale.getDefault());
    String DATE_PREFIX = "V";
    File mPackagePath = new File(Environment.getExternalStorageDirectory(), PATH_PACKAGE);
    File mPackageEventJpgPath = new File(mPackagePath, PATH_EVENT_JPG);
    File mPackageEventPhotoPath = new File(mPackagePath, PATH_EVENT_PHOTO);
    File [] eventFolders, eventPhotos;
    int folderCount, photoCount;
    boolean convert;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!mPackageEventPhotoPath.exists())
            convert = mPackageEventPhotoPath.mkdirs();
        textView = findViewById(R.id.result);
        eventFolders = getDirectoryList(mPackageEventJpgPath);
        folderCount = eventFolders.length;
        Log.w("event","Jpg Folder "+ folderCount);
        if (folderCount > 0) {
            Arrays.sort(eventFolders);
            StringBuilder  folderSB = new StringBuilder();
            for (File eventPath: eventFolders) {
                if (eventPath.getName().substring(0, 1).equals(DATE_PREFIX)) {  // to ignore . folder/files
                    folderSB.append(eventPath.getName())
                            .append("\n");
                }
            }
            textView.setText(folderSB);
            for (File eventPath: eventFolders) {
                if (eventPath.getName().substring(0, 1).equals(DATE_PREFIX)) {  // to ignore . folder/files
                    eventPhotos = getDirectoryList(eventPath);
                    File eventPhotoPath = new File(mPackageEventPhotoPath, eventPath.getName());
                    if (!eventPhotoPath.exists())
                        convert = eventPhotoPath.mkdirs();
                    Arrays.sort(eventPhotos);
                    photoCount = eventPhotos.length;
                    Log.w(eventPath.getName()," photos="+photoCount);
                    for (File eventPhoto: eventPhotos) {
                        Log.w(eventPhoto.getName()," process");
                        int size = (int) eventPhoto.length();
                        byte[] jpgBytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(eventPhoto));
                            buf.read(jpgBytes, 0, jpgBytes.length);
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        Bitmap bm =BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length);
                        Bitmap bm2 = bm.copy(Bitmap.Config.RGB_565, false);
                        bm2.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        byte[] photoBytes = stream.toByteArray();
                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = new FileOutputStream(new File(eventPhotoPath, eventPhoto.getName()));
                            fileOutputStream.write(photoBytes);
                        } catch (IOException e) {
                            Log.e("Err1","IOException catch", e);
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e) {
                                    Log.e("err 2", "IOException finally", e);
                                }
                            }
                        }
//                        StringBuilder processSB = new StringBuilder();
//                        processSB.append(folderSB);
//                        processSB.append(eventPhoto.getName());
//                        final String processMsg = processSB.toString();
//                        Log.w("log", eventPhoto.getName());
//                        this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textView.setText(processMsg);
//                                textView.invalidate();
//                            }
//                        });
//                        SystemClock.sleep(100);
                    }
                }
            }
        }
        Toast.makeText(getApplicationContext(), "Processing Completed", Toast.LENGTH_LONG).show();
    }

    File[] getDirectoryList(File fullPath) {
        return fullPath.listFiles();
    }
}