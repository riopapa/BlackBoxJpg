package com.urrecliner.blackboxjpg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class Convert {

    static File [] eventFolders;
    static String DATE_PREFIX = "V";
    TextView textView;

    void run(File [] evFolders, TextView tv) {
        eventFolders = evFolders;
        textView = tv;
        try {
            new ConvertAsync().execute();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    private class ConvertAsync extends AsyncTask<String, Integer, String> {

        File[] eventPhotos;
        String[] eventNames;
        int[] startPositions, finishPositions;
        String PATH_PACKAGE = "BlackBox";
        String PATH_EVENT_JPG = "jpgTemp";
        String PATH_EVENT_PHOTO = "eventPhoto";
        File mPackagePath = new File(Environment.getExternalStorageDirectory(), PATH_PACKAGE);
        File mPackageEventPhotoPath = new File(mPackagePath, PATH_EVENT_PHOTO);
        File eventPath;
        int folderCount, photoCount;
        boolean rtnCode;
        StringBuilder folderSB;
        SpannableString processMsg = null;

        @Override
        protected String doInBackground(String... strings) {

            folderCount = eventFolders.length;
            startPositions = new int[folderCount];
            finishPositions = new int[folderCount];
            eventNames = new String[folderCount];
            folderSB = new StringBuilder();
            folderSB.append("Folder List\n");
            for (int idx = 0; idx < folderCount; idx++) {
                eventNames[idx] = eventFolders[idx].getName();
                startPositions[idx] = folderSB.length();
                folderSB.append(eventNames[idx])
                        .append("\n");
                finishPositions[idx] = folderSB.length();
            }
            folderSB.append("\n");
//            final String folderList = folderSB.toString();
            publishProgress(-1, -1);

            for (int idxPath = 0; idxPath < eventFolders.length; idxPath++) {
                if (!eventNames[idxPath].substring(0, 1).equals(DATE_PREFIX))
                    continue;
                eventPath = eventFolders[idxPath];
                eventPhotos = eventPath.listFiles();
                File eventPhotoPath = new File(mPackageEventPhotoPath, eventNames[idxPath]);
                if (!eventPhotoPath.exists())
                    rtnCode = eventPhotoPath.mkdirs();
                photoCount = eventPhotos.length;
                for (int idxJpg = 0; idxJpg < eventPhotos.length; idxJpg++) {
                    File eventPhoto = eventPhotos[idxJpg];
//                Log.w(eventPhoto.getName()," process");
                    int photoSize = (int) eventPhoto.length();
                    byte[] jpgBytes = new byte[photoSize];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(eventPhoto));
                        buf.read(jpgBytes, 0, jpgBytes.length);
                        buf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bm = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length);
                    Bitmap bm2 = bm.copy(Bitmap.Config.RGB_565, false);
                    bm2.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    byte[] photoBytes = stream.toByteArray();
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(new File(eventPhotoPath, eventPhoto.getName()));
                        fileOutputStream.write(photoBytes);
                    } catch (IOException e) {
                        Log.e("Err1", "IOException catch", e);
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                Log.e("err 2", "IOException finally", e);
                            }
                        }
                    }
                    if (idxJpg % 4 == 0)
                        publishProgress(idxPath, idxJpg);

                }
            }
            File newFile = new File(eventPath.toString().replace(DATE_PREFIX+"20-","C20-"));
            rtnCode = eventPath.renameTo(newFile);
            return null;
        }

        final Handler showHandler = new Handler() {
            public void handleMessage(Message msg) {
                showProgress();
            }
        };

        @Override
        protected void onProgressUpdate(Integer... values) {
            int folderIdx = values[0];
            int photoIdx = values[1];
            SpannableString ss = new SpannableString(folderSB);
            if (folderIdx == -1 ) {
                processMsg = ss;
                showHandler.sendEmptyMessage(0);
                return;
            }
            int bPos = startPositions[folderIdx];
            int fPos = finishPositions[folderIdx];
            int ePos = bPos + (fPos - bPos) * photoIdx / photoCount;
//                    Log.w("pos ",bPos+"+ "+ePos);
            if (ePos > bPos) {
                ss.setSpan(new ForegroundColorSpan(Color.BLUE), bPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new BackgroundColorSpan(Color.YELLOW), bPos, ePos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new StyleSpan(Typeface.BOLD), bPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        ss.setSpan(new RelativeSizeSpan(1.2f), bPos, fPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                processMsg = ss;
                showHandler.sendEmptyMessage(0);
            }
        }

        void showProgress() {
            textView.setText(processMsg);
            textView.invalidate();
            SystemClock.sleep(10);
        }

        @Override
        protected void onCancelled(String result) { }

        @Override
        protected void onPostExecute(String doI) {
        }

    }
}
