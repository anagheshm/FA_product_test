package anaghesh.beacons_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

// IMP: Google vision API used. Formats to be controlled

public class ScanQR extends AppCompatActivity {

    SurfaceView cameraPreview;
    EditText beacon,vin;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    Button assign;
    public final static String BEACON_NUM="Beacon_num";
    public final static String VIN_NUM="vin_num";


    final int RequestCameraPermissionID = 1001;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        cameraPreview =  findViewById(R.id.camera_pre);

        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        toolbarSetup();
        cameraPreview =  findViewById(R.id.camera_pre);
        Log.e("error",""+cameraPreview);
        beacon = findViewById(R.id.beacon_edittext);
        beacon.setText("");//test

        vin = findViewById(R.id.vin_edittext);
        vin.setText("");//test
        assign = findViewById(R.id.assign);
        assign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("Database", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(BEACON_NUM,beacon.getText().toString());
                editor.putString(VIN_NUM,vin.getText().toString());
                editor.apply();
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ScanQR.this);
                builder.setTitle("Checkin Successful");
                //  builder.setIcon(R.mipmap.ic_launcher);
                builder.setMessage("Beacon No. "+sharedPreferences.getString(BEACON_NUM,"")+" is assigned to VIN "+sharedPreferences.getString(VIN_NUM,""))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               finish();
                            }
                        });
                android.app.AlertDialog alert = builder.create();
                alert.show();

            }
        });
        barcodeDetector = new BarcodeDetector.Builder(this)
              //  .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
        //Add Event
        alert();
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(ScanQR.this,
                            new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0)
                {
                    beacon.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop( );
                            Log.e("QR ",""+qrcodes.valueAt(0).displayValue);

                            Log.e("Beacon ",""+beacon.getText().toString());

                            Log.e("VIN ",""+vin.getText().toString());

                            if(beacon.getText().toString().isEmpty()) {
                                beacon.setText(qrcodes.valueAt(0).displayValue);
                                vinAlert();
                            }
                            else {
                                vin.setText(qrcodes.valueAt(0).displayValue);
                            }
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);


                        }
                    });

                }

            }

        });
    }
    void toolbarSetup(){
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    void alert(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ScanQR.this);
        builder.setTitle("Scan Beacon Barcode");
        //  builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Please scan the beacon barcode")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();

    }
    void vinAlert(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ScanQR.this);
        builder.setTitle("Scan Car Barcode");
        //  builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Please scan the car barcode")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        restartCamera();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }


    @SuppressLint("MissingPermission")
    public void restartCamera()
    {
        Log.e("method","Restart Camera");
        try {
            cameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}