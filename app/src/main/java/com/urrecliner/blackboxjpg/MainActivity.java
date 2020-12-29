package com.urrecliner.blackboxjpg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    String PATH_PACKAGE = "BlackBox";
    String PATH_EVENT_JPG = "jpgTemp";
    String PATH_EVENT_PHOTO = "eventPhoto";
    File mPackagePath = new File(Environment.getExternalStorageDirectory(), PATH_PACKAGE);
    File mPackageEventJpgPath = new File(mPackagePath, PATH_EVENT_JPG);
    File mPackageEventPhotoPath = new File(mPackagePath, PATH_EVENT_PHOTO);
    File [] eventFolders;
    int folderCount;
    boolean rtnCode;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!mPackageEventPhotoPath.exists())
            rtnCode = mPackageEventPhotoPath.mkdirs();
        eventFolders = getDirectoryList(mPackageEventJpgPath);
        textView = findViewById(R.id.result);
        folderCount = eventFolders.length;
        if (folderCount == 0) {
            textView.setText("No folders to convert");
            new Timer().schedule(new TimerTask() {
                public void run() {
                    finish();
                    finishAffinity();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 5000);
        }  else {
            Arrays.sort(eventFolders);
            ConstraintLayout cl = findViewById(R.id.background);
            cl.setBackgroundColor(Color.GRAY);
             Convert cv = new Convert();
             cv.run(eventFolders, textView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_run:
                if (folderCount > 0) {
                    ConstraintLayout cl = findViewById(R.id.background);
                    cl.setBackgroundColor(Color.GRAY);
                    Convert cv = new Convert();
                    cv.run(eventFolders, textView);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    void zip_photo() throws IOException {
//        FileOutputStream fos = null;
//        for (int idxPath = 0; idxPath < eventFolders.length; idxPath++) {
//            if (!eventNames[idxPath].substring(0,1).equals(DATE_PREFIX))
//                continue;
//            File eventPath = eventFolders[idxPath];
////            eventPhotos = getDirectoryList(eventPath);
////            String sourceFile = eventPath.toString();
//            File zipOutFile = new File(mPackageEventPhotoPath, eventPath.getName()+".zip");
//            try {
//                fos = new FileOutputStream(zipOutFile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            ZipOutputStream zipOut = new ZipOutputStream(fos);
////            File fileToZip = new File(sourceFile);
//
//            zipFile(eventPath, eventPath.getName(), zipOut);
//            zipOut.close();
//            fos.close();
//            File newFile = new File(eventPath.toString().replace(DATE_PREFIX + "20-", "C20-"));
//            rtnCode = eventPath.renameTo(newFile);
//        }
//        Log.e("Zip Photo","completed");
//    }

    // zipFile is recursive
//    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
//        if (fileToZip.isHidden()) {
//            return;
//        }
//        if (fileToZip.isDirectory()) {
//            Log.e("run Dir",fileToZip.toString());
//            if (fileName.endsWith("/")) {
//                zipOut.putNextEntry(new ZipEntry(fileName));
//                zipOut.closeEntry();
//            } else {
//                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
//                zipOut.closeEntry();
//            }
//            File[] children = fileToZip.listFiles();
//            for (File childFile : children) {
//                Log.w("jpg",childFile.getName());
//                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
//            }
//            return;
//        }
//        FileInputStream fis = new FileInputStream(fileToZip);
//        ZipEntry zipEntry = new ZipEntry(fileName);
//        zipOut.putNextEntry(zipEntry);
//        byte[] bytes = new byte[1024];
//        int length;
//        while ((length = fis.read(bytes)) >= 0) {
//            zipOut.write(bytes, 0, length);
//        }
//        fis.close();
//    }

    private SpannableString listUpFiles(int currIdx) {
        int sPos = 0, fPos = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Folder List\n");
        sPos = sb.length();
        for (int idx = 0; idx < eventFolders.length; idx++) {
            if (currIdx == idx)
                sPos = sb.length();
            String folderName = eventFolders[idx].getName();
//            sb.append((currIdx == idx)? "<< ":"");
            sb.append(folderName).append("\n");
            fPos = sb.length();
            sb.append("\n");
        }
        SpannableString ss = new SpannableString(sb);
        ss.setSpan(new ForegroundColorSpan(Color.BLUE), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(1.2f), sPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    File[] getDirectoryList(File fullPath) {
        return fullPath.listFiles();
    }
}