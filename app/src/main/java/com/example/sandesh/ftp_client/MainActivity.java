package com.example.sandesh.ftp_client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamException;


public class MainActivity extends AppCompatActivity {
    public float throughput;
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        Log.d("Checkexternel storage","read only?");
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            Log.d("Read only?","No both read and write");
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        Log.d("isextenelstorage avail","available?");
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            Log.d("Avaiable?","yes");
            return true;
        }
        return false;
    }
    public void ftp_operations() {
        Thread t = new Thread() {
            public void run() {
                FTPClient mFtpClient = new FTPClient();
                mFtpClient.setConnectTimeout(10 * 1000);
                //Log.d("connect", "Lets connect with the FTP server");
                boolean status = false;
                try {
                    mFtpClient = new FTPClient();
                    mFtpClient.setConnectTimeout(10 * 1000);
                    mFtpClient.connect(InetAddress.getByName("18.218.55.254"));
                    status = mFtpClient.login("sandesh", "sandesh");
                    Log.e("isFTPConnected", String.valueOf(status));
                    if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                        mFtpClient.setFileType(FTP.ASCII_FILE_TYPE);
                        mFtpClient.enterLocalPassiveMode();
                        FTPFile[] mFileArray = mFtpClient.listFiles();
                        Log.d("connect","Files are"+ Arrays.toString(mFileArray));
                        Log.e("Size", String.valueOf(mFileArray.length));
                        //for AWS server we need passive mode
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String filename = "reading";
                File file = new File(getFilesDir(), filename);
                Log.d("download", "File path:" + file.getAbsolutePath());
                OutputStream outputStream = null;
                try {
                    outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    try {
                        mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        mFtpClient.enterLocalPassiveMode();
                        mFtpClient.setAutodetectUTF8(true);
                        Boolean download_status = mFtpClient.retrieveFile("reading", outputStream);
                        Log.d("download:",""+mFtpClient.getReplyString());
                        Log.d("Download ststus",""+download_status);
                    }catch (FTPConnectionClosedException fe){
                        fe.getCause();
                    }catch (CopyStreamException ce){
                        ce.getCause();
                    }catch (IOException ie){
                        ie.getCause();
                    }
                    outputStream.flush();
                    outputStream.close();
                    Log.d("file size", "size of file\n" + file.length() + "\nFile path" + file.getAbsolutePath());
                    Log.d("FTP","Lets read the last line of the file");
                    if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                        Log.d("File", "Sorry cannot read the file");
                    }else{
                            //read the last line of the file
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
                            String line;
                            int count=1;
                            while ((line = reader.readLine()) != null) {
                                Log.d("Line red",""+line);
                            }
                            reader.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }/*
                try {
                    File f = new File(file.getAbsolutePath());
                    InputStream fileIS = new FileInputStream(f);
                    BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
                    String readString = new String();
                    while ((readString = buf.readLine()) != null) {
                        //Log.d("Content: ", readString);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        };
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if ((ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
                }

            }
        }

        if ((ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 777);
                }

            }
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ftp_operations();
    }
}

