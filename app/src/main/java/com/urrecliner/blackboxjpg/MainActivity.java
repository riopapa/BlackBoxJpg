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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String PATH_PACKAGE = "BlackBox";
    String PATH_EVENT_JPG = "eventJpg";
    String PATH_EVENT_PHOTO = "eventPhoto";
    String DATE_PREFIX = "V";
    File mPackagePath = new File(Environment.getExternalStorageDirectory(), PATH_PACKAGE);
    File mPackageEventJpgPath = new File(mPackagePath, PATH_EVENT_JPG);
    File mPackageEventPhotoPath = new File(mPackagePath, PATH_EVENT_PHOTO);
    File [] eventFolders, eventPhotos;
    int folderCount, photoCount;
    boolean rtnCode;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!mPackageEventPhotoPath.exists())
            rtnCode = mPackageEventPhotoPath.mkdirs();
        textView = findViewById(R.id.result);
        textView.setText("Converting..");
        new Timer().schedule(new TimerTask() {
            public void run() {
//            textView.post(new Runnable() {
//                @Override
//                public void run() {
                    convert_photo();
//                }
//            });
            }
        }, 1000);
    }

    void convert_photo() {
        eventFolders = getDirectoryList(mPackageEventJpgPath);
        folderCount = eventFolders.length;
        Log.w("event","Jpg Folder "+ folderCount);
        if (folderCount > 0) {
            Arrays.sort(eventFolders);
            StringBuilder  folderSB = new StringBuilder();
            folderSB.append("Folder List\n");
            for (File eventPath: eventFolders) {
                if (eventPath.getName().substring(0, 1).equals(DATE_PREFIX)) {  // to ignore . folder/files
                    folderSB.append(eventPath.getName())
                            .append("\n");
                }
            }
            final String folderList = folderSB.toString()+"\n-->\n";
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(folderList);
                    textView.invalidate();
                }
            });

            for (File eventPath: eventFolders) {
                if (eventPath.getName().substring(0, 1).equals(DATE_PREFIX)) {  // to ignore . folder/files
                    eventPhotos = getDirectoryList(eventPath);
                    File eventPhotoPath = new File(mPackageEventPhotoPath, eventPath.getName());
                    if (!eventPhotoPath.exists())
                        rtnCode = eventPhotoPath.mkdirs();
                    Arrays.sort(eventPhotos);
                    photoCount = eventPhotos.length;
                    int idx = 0;
                    for (File eventPhoto: eventPhotos) {
//                        Log.w(eventPhoto.getName()," process");
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
                        final String processMsg = folderList+eventPath.getName()+
                                "\n" + eventPhoto.getName()+" ("+ ++idx + "/"+photoCount+")";
                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(processMsg);
                                textView.invalidate();
                            }
                        });
                        SystemClock.sleep(100);
                    }
                    File newFile = new File(eventPath.toString().replace(DATE_PREFIX+"20-","C20-"));
                    rtnCode = eventPath.renameTo(newFile);
                }
            }
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("DONE !");
                Toast.makeText(getApplicationContext(), "Processing Completed", Toast.LENGTH_LONG).show();
            }
        });
        new Timer().schedule(new TimerTask() {
            public void run() {
                finish();
                finishAffinity();
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 3000);

    }
    File[] getDirectoryList(File fullPath) {
        return fullPath.listFiles();
    }
}