package com.project.qrcodegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 100;
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
        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            qrCodeView.setImageBitmap(urlToImageEncode(startUml+ manager.getImei()));
           // qrCodeText.setText(startUml + manager.getImei());
        }
        else {
            qrCodeView.setImageBitmap(urlToImageEncode(startUml+manager.getDeviceId()));
           // qrCodeText.setText(startUml+manager.getDeviceId());
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
            return bitmap;
        } catch (WriterException e) {
            setDebuggableText(e);
            e.printStackTrace();
            return null;
        }

    }

    private void setDebuggableText(Exception e){
        Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        qrCodeText.setText(writer.toString());
    }

}