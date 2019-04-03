package com.empatica.sample;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.google.firebase.database.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {
    private DatabaseReference mDatabase;

    private String filename = "SampleFile.txt";
    private String filepath = "MyFileStorage";
    File myExternalFile;
    String myData = "";

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;


    private static final String EMPATICA_API_KEY = "639e03895c4c4de78ac79c2379a497b0"; // TODO insert your API Key here


    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;

    private TextView accel_yLabel;

    private TextView accel_zLabel;

    private TextView bvpLabel;

    private TextView edaLabel;

    private TextView ibiLabel;

    private TextView temperatureLabel;

    private TextView batteryLabel;

    private TextView statusLabel;

    private TextView deviceNameLabel;

    private LinearLayout dataCnt;
    private String group="a";
    private user user;
    private boolean isResearchStarted=false;
    private TextView groupText;
    private TextView feedbackLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);

        dataCnt = (LinearLayout) findViewById(R.id.dataArea);

        accel_xLabel = (TextView) findViewById(R.id.accel_x);

        accel_yLabel = (TextView) findViewById(R.id.accel_y);

        accel_zLabel = (TextView) findViewById(R.id.accel_z);

        bvpLabel = (TextView) findViewById(R.id.bvp);

        edaLabel = (TextView) findViewById(R.id.eda);

        ibiLabel = (TextView) findViewById(R.id.ibi);

        temperatureLabel = (TextView) findViewById(R.id.temperature);

        //batteryLabel = (TextView) findViewById(R.id.battery);

        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        feedbackLabel = (TextView) findViewById(R.id.feedback);


        final Button disconnectButton = findViewById(R.id.disconnectButton);
        final Button resStartButton = findViewById(R.id.startRe);
        final Button resStopButton = findViewById(R.id.researchstop);
        final Button basstartButton = findViewById(R.id.basstart);
        final Button basendButton = findViewById(R.id.bassend);
        final Button missstartButton = findViewById(R.id.missstart);
        final Button missendButton = findViewById(R.id.missend);
        final Button mossstartButton = findViewById(R.id.mossstart);
        final Button mossendButton = findViewById(R.id.mossend);
        final Button sessstartButton = findViewById(R.id.sessstart);
        final Button sessendButton = findViewById(R.id.sessend);
        final Button resstartButton = findViewById(R.id.resstart);
        final Button resendButton = findViewById(R.id.resend);
        final Button triggerButton = findViewById(R.id.trgigger);
         groupText = findViewById(R.id.groupText);

        disconnectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setMessage("are you sure , you wanna stop the streaming ?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (deviceManager != null) {

                                    deviceManager.disconnect();
                                }                           }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.show();


            }
        });
        resStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDatabase.child("/cho/users/"+  groupText.getText().toString()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get Post object and use the values to update the UI
                                user muser = dataSnapshot.getValue(user.class);
                                if(muser==null){
                                    Date date = new Date();
                                    user=new user("", date);
                                    Map<String, Object> childUpdates = new HashMap<String, Object>();
                                    childUpdates.put("/cho/users/"  +  groupText.getText().toString(), user);
                                    mDatabase.updateChildren(childUpdates);

                                }
                                resStartButton.setVisibility(View.INVISIBLE);
                                groupText.setEnabled(false);
                                isResearchStarted=true;
                                feedbackLabel.setText("research started");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                String x="aa";
                            }
                        }
                );


            }
        });
        resStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setMessage("are you sure , you wanna end the recording session ?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/isDone").setValue(true);
                                isResearchStarted=false;
                                resStopButton.setVisibility(View.INVISIBLE);
                                triggerButton.setVisibility(View.INVISIBLE);
                                feedbackLabel.setText("research stopped");
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.show();



            }
        });


        basstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrigger("BA-SStart", "BA-SEnd");
            }
        });
        basendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/BA-SEnd").setValue(date.getTime());
                feedbackLabel.setText(" bas end");

            }
        });

        missstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrigger("MIS-SStart", "MIS-SEnd");
            }
        });

        missendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/MIS-SEnd").setValue(date.getTime());
                feedbackLabel.setText(" bas end");

            }
        });
        mossstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrigger("MOS-SStart", "MOS-SEnd");
            }
        });

        mossendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/MOS-SEnd").setValue(date.getTime());
                feedbackLabel.setText(" mos end");


            }
        });
        sessstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrigger("SES-SStart", "SES-SEnd");
            }
        });

        sessendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/SES-SEnd").setValue(date.getTime());
                feedbackLabel.setText(" ses end");

            }
        });
        resstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrigger("RE-SStart", "RE-SEnd");
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/RE-SEnd").setValue(date.getTime());
                feedbackLabel.setText(" res end");


            }
        });
        initEmpaticaDeviceManager();

    }

    private void startTrigger(final String triggerStart, final String triggerEnd){
        Date date = new Date();
        mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/" + triggerStart).setValue(date.getTime());
        feedbackLabel.setText(triggerStart);

        new CountDownTimer(60000 * 4 , 1000) {

            public void onTick(long millisUntilFinished) {
                String remining = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                feedbackLabel.setText(triggerStart + "remaining: " + remining);
            }

            public void onFinish() {
                feedbackLabel.setText(triggerStart +" done!");
                Date date = new Date();
                mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/" + triggerEnd).setValue(date.getTime());
            }
        }.start();
    }

    private void writeSensorsDataToFirebase(String key,sensorData sensData){
        if(isResearchStarted){
            String new_ke=mDatabase.child("/cho/users/"  + groupText.getText().toString()+'/'+key).push().getKey();
            mDatabase.child("/cho/users/"  + groupText.getText().toString()+"/"+key+"/"+new_ke).setValue(sensData);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection

            hide();

        } else if (status == EmpaStatus.CONNECTED) {

            show();
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {

            updateLabel(deviceNameLabel, "");

            hide();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        writeSensorsDataToFirebase("bvpData",new sensorData(bvp,timestamp));

        updateLabel(bvpLabel, "" + bvp);
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        //updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        writeSensorsDataToFirebase("gsrData",new sensorData(gsr,timestamp));

        updateLabel(edaLabel, "" + gsr);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        writeSensorsDataToFirebase("ibiData",new sensorData(ibi,timestamp));

        updateLabel(ibiLabel, "" + ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        writeSensorsDataToFirebase("tempData",new sensorData(temp,timestamp));

        updateLabel(temperatureLabel, "" + temp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
                }
                else {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
                }
            }
        });
    }

    void show() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.VISIBLE);
            }
        });
    }

    void hide() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.INVISIBLE);
            }
        });
    }
}
