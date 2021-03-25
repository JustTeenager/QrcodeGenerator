package com.project.qrcodegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final String SHARED_PREFS_KEY = "shared_prefs_key";
    private static final String SHARED_PREFS_NAME = "shared_prefs_name";
    private TelephonyManager manager;
    private ImageView qrCodeView;
    private TextView qrCodeText;
    private final String startUml = "https://xxxxxxx.ru?imei=";
    private final int QrCodeSize = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            qrCodeText = findViewById(R.id.uml_code_text);
            qrCodeView = findViewById(R.id.qr_code_view);

            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                createQrCode();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_CODE);
            }
        }
        catch (Exception e){setDebuggableText(e);}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                createQrCode();
            }
        }
    }

    private void createQrCode() {
        if (getSharedPreferences(SHARED_PREFS_NAME,MODE_PRIVATE).getString(SHARED_PREFS_KEY,null)==null) {
            manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                qrCodeView.setImageBitmap(urlToImageEncode(startUml + manager.getImei()));
            } else {
                qrCodeView.setImageBitmap(urlToImageEncode(startUml + manager.getDeviceId()));
            }
        } else{
            Log.d("tut_else_qr","вошли в else");
            byte[] bytes=Base64.decode(getSharedPreferences(SHARED_PREFS_NAME,MODE_PRIVATE).getString(SHARED_PREFS_KEY,""),Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            qrCodeView.setImageBitmap(bitmap);
        }
    }

    private Bitmap urlToImageEncode(String uml) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(uml,BarcodeFormat.DATA_MATRIX.QR_CODE, QrCodeSize, QrCodeSize,null);
            int bitMatrixWidth = bitMatrix.getWidth();
            int bitMatrixHeight = bitMatrix.getHeight();
            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            for (int y =0; y <bitMatrixHeight; y++){
                int offset = y*bitMatrixWidth;
                for (int x = 0; x < bitMatrixWidth; x++){
                    pixels[offset + x] = bitMatrix.get(x,y) ? getResources().getColor(R.color.qr_code_blue) : getResources().getColor(R.color.white);
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth,bitMatrixHeight, Bitmap.Config.ARGB_4444);
            bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth,bitMatrixHeight);
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    writeBitmapIntoPrefs(bitmap);
                }
            }.start();
            return bitmap;
        } catch (WriterException e) {
            setDebuggableText(e);
            return null;
        }

    }

    private void writeBitmapIntoPrefs(Bitmap bitmap) {
        Log.d("tut_to_prefs",bitmap.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encoded = Base64.encodeToString(b, Base64.DEFAULT);
        getSharedPreferences(SHARED_PREFS_NAME,MODE_PRIVATE).edit().putString(SHARED_PREFS_KEY,encoded).apply();
    }

    private void setDebuggableText(Exception e){
        Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        qrCodeText.setText(writer.toString());
    }

}