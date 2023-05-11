package at.wolframdental.Scanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.socketmobile.capture.AppKey;
import com.socketmobile.capture.CaptureError;
import com.socketmobile.capture.Property;
import com.socketmobile.capture.SocketCamStatus;
import com.socketmobile.capture.android.Capture;
import com.socketmobile.capture.android.events.ConnectionStateEvent;
import com.socketmobile.capture.client.CaptureClient;
import com.socketmobile.capture.client.ConnectionCallback;
import com.socketmobile.capture.client.ConnectionState;
import com.socketmobile.capture.client.DataEvent;
import com.socketmobile.capture.client.DeviceClient;
import com.socketmobile.capture.client.DeviceManagerStateEvent;
import com.socketmobile.capture.client.DeviceState;
import com.socketmobile.capture.client.DeviceStateEvent;
import com.socketmobile.capture.client.android.BuildConfig;
import com.socketmobile.capture.client.callbacks.PropertyCallback;
import com.socketmobile.capture.socketcam.client.CaptureExtension;
import com.socketmobile.capture.troy.ExtensionScope;
import com.socketmobile.capture.troy.SocketCamDevice;
import com.socketmobile.capture.types.DecodedData;
import com.socketmobile.capture.types.Device;
import com.socketmobile.capture.types.DeviceType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public final String tag = "H2W";
    private int serviceStatus = ConnectionState.DISCONNECTED;
    private HashMap<String, DeviceClient> deviceClientMap = new HashMap<String, DeviceClient>();
    private HashMap<String, DeviceState> deviceStateMap = new HashMap<String, DeviceState>();
    private CaptureClient mCaptureClient = null;
    private CaptureExtension mCaptureExtension;
    public Button Kamera;
    public Button Share;
    private ActivityResultLauncher<Intent> addActivityResultLauncher;
    private EditText artikelnummerInput;
    private Button suchenButton;
    private RecyclerView artikelListe;
    private List<Artikel> artikelListeData = new ArrayList<>();
    private ArtikelAdapter artikelAdapter;
    private static final String SAVED_ARTIKEL_LISTE_KEY = "saved_artikel_liste";
    private TextView T2011;
    private TextView T2012;
    private TextView T3011;
    private TextView T3012;
    private TextView T4111;
    private TextView T4112;
    private TextView T2031;
    private TextView T2032;
    private TextView T5001;
    private TextView T5002;
    private TextView T6111;
    private TextView T6112;

    ConnectionCallback connectionCallback = new ConnectionCallback() {
        @Override
        public void onConnectionStateChanged(ConnectionState state) {
            serviceStatus = state.intValue();
            switch(state.intValue()) {
                case ConnectionState.CONNECTING:
                    // do something or nothing
                    break;
                case ConnectionState.CONNECTED:
                    // client is now usable
                    break;
                case ConnectionState.DISCONNECTING:
                    // only called when shutting down gracefully
                    break;
                case ConnectionState.DISCONNECTED:
                    if(state.hasError()) {
                        // Handle error
                    } else {
                        // Shut down normally
                    }
                default:
                    // Unreachable
                    break;
            }
        }
    };

    String getLineForBarcode(Context c, String barcode) {
        String retValue = null;
        if (barcode.isEmpty())
            retValue = "txt_barcode";
        else retValue = barcode;
        int defaultQuantity = 1;
        if (true) {
            String value;
            if (true) {
                value = ", " + defaultQuantity;
            } else {
                value = " " + defaultQuantity;
            }

            retValue += value;
        }

        String newLineSymbol;
        if (true)
            newLineSymbol = "\n";
        else
            newLineSymbol = ";";
        if (!barcode.isEmpty()) {
            retValue = newLineSymbol + retValue;
        } else {
            retValue = retValue + newLineSymbol;
        }

        return retValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Capture.builder(getApplicationContext())
                .enableLogging(BuildConfig.DEBUG)
                .build();

        AppKey appKey = new AppKey("MC0CFQCCZVzNP52xT1qcAFuzC1v+d0wAvwIUeWjo449lobxw1jtB3sf23C68PXM=",
                "android:at.wolframdental.Scanner",
                "e1c1ca5c-fcda-ed11-a7c7-6045bd074938");
//        startSocketCamExtension();
        mCaptureClient = new CaptureClient(appKey);
        mCaptureClient.setListener(new CaptureClient.Listener() {
            @Override
            public void onDeviceManagerStateEvent(DeviceManagerStateEvent deviceManagerStateEvent) {

            }

            @Override
            public void onDeviceStateEvent(DeviceStateEvent event) {
                DeviceClient device = event.getDevice();
                DeviceState state = event.getState();
                int scannerStatus = state.intValue();
                String deviceGuid = device.getDeviceGuid();
                deviceStateMap.put(deviceGuid, state);
                deviceClientMap.put(deviceGuid, device);

                if (device.getDeviceType() != DeviceType.kModelSocketCamC820) {
                    stopSocketCamExtension();
                }
                Log.d(tag, "Scanner  : " + device.getDeviceName() + " - " + device.getDeviceGuid());

                switch (scannerStatus) {
                    case DeviceState.AVAILABLE: {
                        Log.d(tag, "Scanner State Available.");
                        break;
                    }
                    case DeviceState.OPEN: {
                        Log.d(tag, "Scanner State Open.");
                        break;
                    }
                    case DeviceState.READY: {
                        Log.d(tag, "Scanner State Ready.");
                        triggerDevices();
                        break;
                    }
                    case DeviceState.GONE: {
                        Log.d(tag, "Scanner State Gone.");
                        deviceStateMap.remove(deviceGuid);
                        deviceClientMap.remove(deviceGuid);
                        break;
                    }
                    default:
                        Log.d(tag, "Scanner State " + scannerStatus);
                        break;
                }
            }

            @Override
            public void onData(DataEvent event) {
                DecodedData data = event.getData();
                if (data.result == DecodedData.RESULT_SUCCESS) {
                    artikelnummerInput = findViewById(R.id.artikelnummer_input);
                    artikelnummerInput.setText(event.getData().getString());
                    suchenButton.performClick();
                } else {
                }
            }

            @Override
            public void onError(CaptureError captureError) {

            }
        });
        mCaptureClient.connect(connectionCallback);


        Kamera = findViewById(R.id.Kamera);
        Kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openCamera(); }
        });

        Share = findViewById(R.id.Share);
        Share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShare();
            }
        });

        artikelnummerInput = findViewById(R.id.artikelnummer_input);
        suchenButton = findViewById(R.id.suchen_button);
        artikelListe = findViewById(R.id.artikel_liste);

        artikelAdapter = new ArtikelAdapter(this, artikelListeData);
        artikelListe.setAdapter(artikelAdapter);
        artikelListe.setLayoutManager(new LinearLayoutManager(this));

        DatenbankHelper dbHelper = new DatenbankHelper(this);

        T2011 = findViewById(R.id.T2011);
        T2012 = findViewById(R.id.T2012);
        T3011 = findViewById(R.id.T3011);
        T3012 = findViewById(R.id.T3012);
        T4111 = findViewById(R.id.T4111);
        T4112 = findViewById(R.id.T4112);
        T2031 = findViewById(R.id.T2031);
        T2032 = findViewById(R.id.T2032);
        T5001 = findViewById(R.id.T5001);
        T5002 = findViewById(R.id.T5002);
        T6111 = findViewById(R.id.T6111);
        T6112 = findViewById(R.id.T6112);

        artikelAdapter.setOnItemCountChangeListener(new ArtikelAdapter.OnItemCountChangeListener() {
            @Override
            public void onItemCountChange(int itemCount) {
                updateCounts();
            }
        });

        Button plusButton = findViewById(R.id.plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, at.wolframdental.Scanner.Add.class);
                addActivityResultLauncher.launch(intent);
            }
        });

        addActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String addText = result.getData().getStringExtra("add_text");
                        if (addText != null) {
                            artikelnummerInput.setText(addText);
                            suchenButton.performClick();
                        }
                    }
                });

        suchenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String artikelnummer = artikelnummerInput.getText().toString();
                if(!artikelnummer.isEmpty()) {
                    switch (artikelnummer.length()) {
                        case 19:
                            artikelnummer = artikelnummer.substring(0, 14);
                            break;
                        case 20:
                            artikelnummer = artikelnummer.substring(0, 14);
                            break;
                        case 21:
                            break;
                        case 22:
                            artikelnummer = artikelnummer.substring(0, 18);
                            break;
                        case 23:
                            artikelnummer = artikelnummer.substring(0, 18);
                            break;
                        default:
                            break;}

                    try {
                        Artikel artikel = dbHelper.getArtikel(artikelnummer);
                        // artikelListeData.clear(); // vorherige Daten entfernen
                        artikelListeData.add(artikel);
                        artikelAdapter.notifyDataSetChanged();
                        artikelnummerInput.setText("");
                    } catch (Exception e) {
                        Artikel dummyArtikel = new Artikel(artikelnummer, "Kein Eintrag gefunden");
                        artikelListeData.add(dummyArtikel);
                        artikelAdapter.notifyDataSetChanged();
                        artikelnummerInput.setText("");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Bitte QR-Code eingeben!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        artikelnummerInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    suchenButton.performClick();
                    return true;
                }
                return false;
            }
        });

        // Zustand der ArtikelListeData wiederherstellen, falls vorhanden
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_ARTIKEL_LISTE_KEY)) {
            artikelListeData = savedInstanceState.getParcelableArrayList(SAVED_ARTIKEL_LISTE_KEY);
            artikelAdapter = new ArtikelAdapter(this, artikelListeData);
            artikelListe.setAdapter(artikelAdapter);
        }

        Button clearButton = findViewById(R.id.Clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artikelnummerInput.setText("");
                artikelListeData.clear();
                artikelAdapter.notifyDataSetChanged();
                T2011.setText("T2011: 0");
                T2012.setText("T2012: 0");
                T3011.setText("T3011: 0");
                T3012.setText("T3012: 0");
                T4111.setText("T4111: 0");
                T4112.setText("T4112: 0");
                T2031.setText("T2031: 0");
                T2032.setText("T2032: 0");
                T5001.setText("T5001: 0");
                T5002.setText("T5002: 0");
                T6111.setText("T6111: 0");
                T6112.setText("T6112: 0");
            }
        });

    }

    private void openShare() {
        Toast.makeText(this, "Share implementieren.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Zustand der ArtikelListeData speichern
        outState.putParcelableArrayList(SAVED_ARTIKEL_LISTE_KEY, new ArrayList<>(artikelListeData));

        // Zustand der Zählvariablen speichern
        outState.putString("T2011", T2011.getText().toString());
        outState.putString("T2012", T2012.getText().toString());
        outState.putString("T3011", T3011.getText().toString());
        outState.putString("T3012", T3012.getText().toString());
        outState.putString("T4111", T4111.getText().toString());
        outState.putString("T4112", T4112.getText().toString());
        outState.putString("T2031", T2031.getText().toString());
        outState.putString("T2032", T2032.getText().toString());
        outState.putString("T5001", T5001.getText().toString());
        outState.putString("T5002", T5002.getText().toString());
        outState.putString("T6111", T6111.getText().toString());
        outState.putString("T6112", T6112.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Zustand der ArtikelListeData wiederherstellen, falls vorhanden
        if (savedInstanceState.containsKey(SAVED_ARTIKEL_LISTE_KEY)) {
            artikelListeData = savedInstanceState.getParcelableArrayList(SAVED_ARTIKEL_LISTE_KEY);
            artikelAdapter = new ArtikelAdapter(this, artikelListeData);
            artikelListe.setAdapter(artikelAdapter);
        }

        // Zustand der Zählvariablen wiederherstellen, falls vorhanden
        if (savedInstanceState.containsKey("T2011")) {
            T2011.setText(savedInstanceState.getString("T2011"));
        }
        if (savedInstanceState.containsKey("T2012")) {
            T2012.setText(savedInstanceState.getString("T2012"));
        }
        if (savedInstanceState.containsKey("T3011")) {
            T3011.setText(savedInstanceState.getString("T3011"));
        }
        if (savedInstanceState.containsKey("T3012")) {
            T3012.setText(savedInstanceState.getString("T3012"));
        }
        if (savedInstanceState.containsKey("T4111")) {
            T4111.setText(savedInstanceState.getString("T4111"));
        }
        if (savedInstanceState.containsKey("T4112")) {
            T4112.setText(savedInstanceState.getString("T4112"));
        }
        if (savedInstanceState.containsKey("T2031")) {
            T2031.setText(savedInstanceState.getString("T2031"));
        }
        if (savedInstanceState.containsKey("T2032")) {
            T2032.setText(savedInstanceState.getString("T2032"));
        }
        if (savedInstanceState.containsKey("T5001")) {
            T5001.setText(savedInstanceState.getString("T5001"));
        }
        if (savedInstanceState.containsKey("T5002")) {
            T5002.setText(savedInstanceState.getString("T5002"));
        }
        if (savedInstanceState.containsKey("T6111")) {
            T6111.setText(savedInstanceState.getString("T6111"));
        }
        if (savedInstanceState.containsKey("T6112")) {
            T6112.setText(savedInstanceState.getString("T6112"));
        }

        // OnItemCountChangeListener für den Artikel-Adapter erneut setzen
        artikelAdapter.setOnItemCountChangeListener(new ArtikelAdapter.OnItemCountChangeListener() {
            @Override
            public void onItemCountChange(int itemCount) {
                updateCounts();
            }
        });

    }

    private void updateCounts() {
        int countT2011 = 0;
        int countT2012 = 0;
        int countT3011 = 0;
        int countT3012 = 0;
        int countT4111 = 0;
        int countT4112 = 0;
        int countT2031 = 0;
        int countT2032 = 0;
        int countT5001 = 0;
        int countT5002 = 0;
        int countT6111 = 0;
        int countT6112 = 0;

        for (Artikel artikel : artikelListeData) {
            if (artikel.getBeschreibung().contains("PLUS Front")) {
                countT2011++;
            }
            if (artikel.getBeschreibung().contains("PLUS Backen")) {
                countT2012++;
            }
            if (artikel.getBeschreibung().contains("LUX Front")) {
                countT3011++;
            }
            if (artikel.getBeschreibung().contains("LUX Backen")) {
                countT3012++;
            }
            if (artikel.getBeschreibung().contains("DENT Front")) {
                countT4111++;
            }
            if (artikel.getBeschreibung().contains("DENT Backen")) {
                countT4112++;
            }
            if (artikel.getBeschreibung().contains("COMP Front")) {
                countT2031++;
            }
            if (artikel.getBeschreibung().contains("COMP Backen")) {
                countT2032++;
            }
            if (artikel.getBeschreibung().contains("Bambino Front")) {
                countT5001++;
            }
            if (artikel.getBeschreibung().contains("Bambino Backen")) {
                countT5002++;
            }
            if (artikel.getBeschreibung().contains("Facetten Front")) {
                countT6111++;
            }
            if (artikel.getBeschreibung().contains("Facetten Backen")) {
                countT6112++;
            }
        }
        T2011.setText("T2011: " + countT2011);
        T2012.setText("T2012: " + countT2012);
        T3011.setText("T3011: " + countT3011);
        T3012.setText("T3012: " + countT3012);
        T4111.setText("T4111: " + countT4111);
        T4112.setText("T4112: " + countT4112);
        T2031.setText("T2031: " + countT2031);
        T2032.setText("T2032: " + countT2032);
        T5001.setText("T5001: " + countT5001);
        T5002.setText("T5002: " + countT5002);
        T6111.setText("T6111: " + countT6111);
        T6112.setText("T6112: " + countT6112);
    }

    private void openCamera() {
//        Toast.makeText(this, "Kamera öffnen implementieren.", Toast.LENGTH_SHORT).show();
        // DeviceClient.DeviceType.kModelSocketCamC820.start();
        onScanClicked();
    }

    private void onScanClicked() {
        if (canTriggerScanner()) {
            triggerDevices();
        } else {
            showCompanionDialog();
        }
    }

    private void showCompanionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Companion Dialog");
        builder.setMessage("No socket scanner is connected.");
        builder.setPositiveButton("Use Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startSocketCamExtension();
            }
        });


        builder.setNegativeButton("Launch Companion", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.companion_store_url)));
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private boolean canTriggerScanner() {
        return isServiceConnected() && isConnectedDevice();
    }

    private boolean isConnectedDevice() {
        return deviceStateMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().intValue() == DeviceState.READY)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .size() > 0;
    }

    private boolean isServiceConnected() {
        return serviceStatus == ConnectionState.READY;
    }
    private void triggerDevices() {
        List<DeviceClient> readyDevices = deviceStateMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().intValue() == DeviceState.READY)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .keySet()
                .stream()
                .map(entry -> deviceClientMap.get(entry))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<DeviceClient> bluetoothReaders = readyDevices.stream()
                .filter(device -> (device.getDeviceType() != DeviceType.kModelSocketCamC820))
                .collect(Collectors.toList());
        List<DeviceClient> socketCamDevices = readyDevices.stream()
                .filter(device -> (device.getDeviceType() == DeviceType.kModelSocketCamC820))
                .collect(Collectors.toList());
        if (bluetoothReaders.size() > 0) {
            for (int i = 0; i < bluetoothReaders.size(); i++) {
                DeviceClient device = bluetoothReaders.get(i);
                device.trigger(new PropertyCallback() {
                    @Override
                    public void onComplete(CaptureError captureError, Property property) {
                        Log.d("H2W", "trigger callback : " + captureError + " : " + property);
                    }
                });
            }
        } else {
            socketCamDevices.stream().findFirst().orElse(null)
                    .trigger(new PropertyCallback() {
                        @Override
                        public void onComplete(CaptureError captureError, Property property) {
                            Log.d("H2W", "trigger callback : " + captureError + " : " + property);
                        }
                    });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();stopSocketCamExtension();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounts();
    }

    @Override
    protected void onStop() {
        super.onStop();stopSocketCamExtension();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCaptureExtension != null)
            mCaptureExtension.stop();
    }

    public void startSocketCamExtension() {
        if(mCaptureClient != null) {
            mCaptureExtension = new CaptureExtension.Builder()
                    .setContext(this)
                    .setClientHandle(mCaptureClient.getHandle())
                    .setListener(mListener)
                    .setExtensionScope(ExtensionScope.LOCAL)
                    .build();
                mCaptureExtension.start();
        } else {
            Log.e("H2W", "startSocketCamExtension : mCaptureClient is null");
        }
    }

    public void stopSocketCamExtension() {
        if(mCaptureExtension != null) {
            mCaptureExtension.stop();
        } else {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScan(DataEvent event) {
        DecodedData data = event.getData();
        if (data.result == DecodedData.RESULT_SUCCESS) {
            artikelnummerInput = findViewById(R.id.artikelnummer_input);
            artikelnummerInput.setText(event.getData().getString());
            suchenButton.performClick();
        } else {
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    void onCaptureDeviceStateChange(DeviceStateEvent event) {
        DeviceClient device = event.getDevice();
        DeviceState state = event.getState();
        int scannerStatus = state.intValue();
        String deviceGuid = device.getDeviceGuid();
        deviceStateMap.put(deviceGuid, state);
        deviceClientMap.put(deviceGuid, device);

        if (device.getDeviceType() != DeviceType.kModelSocketCamC820) {
            stopSocketCamExtension();
        }
        Log.d(tag, "Scanner  : " + device.getDeviceName() + " - " + device.getDeviceGuid());

        switch (scannerStatus) {
            case DeviceState.AVAILABLE: {
                Log.d(tag, "Scanner State Available.");
                break;
            }
            case DeviceState.OPEN: {
                Log.d(tag, "Scanner State Open.");
                break;
            }
            case DeviceState.READY: {
                Log.d(tag, "Scanner State Ready.");
                triggerDevices();
                break;
            }
            case DeviceState.GONE: {
                Log.d(tag, "Scanner State Gone.");
                deviceStateMap.remove(deviceGuid);
                deviceClientMap.remove(deviceGuid);
                break;
            }
            default:
                Log.d(tag, "Scanner State " + scannerStatus);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    void onCaptureServiceConnectionStateChange(ConnectionStateEvent event) {
        ConnectionState state = event.getState();

        if (state.hasError()) {
            CaptureError error = state.getError();
            Log.d(tag, "Error on service connection. Error: " + error.getCode() + " " + error.getMessage());
            switch (error.getCode()) {
                case CaptureError.COMPANION_NOT_INSTALLED: {
                    AlertDialog alert = new AlertDialog.Builder(this)
                            .setMessage(R.string.prompt_install_companion)
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Install", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.companion_store_url)));
                                    startActivity(i);
                                }
                            }).create();
                    alert.show();
                    break;
                }
                case CaptureError.SERVICE_NOT_RUNNING: {
                    if (state.isDisconnected()) {
                        if (Capture.notRestartedRecently()) {
                            Capture.restart(this);
                        }
                    }
                    break;
                }
                case CaptureError.BLUETOOTH_NOT_ENABLED: {
                    Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // T ODO: Consider calling
                        //    A ctivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        break;
                    }
                    startActivity(i);
                    break;
                }
                default:
                {
                    break;
                }
            }
        } else {
            mCaptureClient = event.getClient();

            serviceStatus = state.intValue();
            Log.d(tag, "Service Status is changed to " + serviceStatus + " (" + state +")");
            switch (serviceStatus) {
                case ConnectionState.CONNECTING:
                {
                    break;
                }
                case ConnectionState.CONNECTED:
                {
                    break;
                }
                case ConnectionState.READY:
                {
                    break;
                }
                case ConnectionState.DISCONNECTING:
                {
                    break;
                }
                case ConnectionState.DISCONNECTED:
                {
                    break;
                }
            }
        }
    }

    CaptureExtension.Listener mListener = new CaptureExtension.Listener() {
        @Override
        public void onExtensionStateChanged(ConnectionState connectionState) {

            switch (connectionState.intValue()) {
                case ConnectionState.CONNECTED:
                    mCaptureClient.setSocketCamStatus(SocketCamStatus.ENABLE, new PropertyCallback() {
                        @Override
                        public void onComplete(CaptureError captureError, Property property) {
                            if (captureError != null) {
                                Log.d(tag, "Failed setSocketCamStatus " + captureError.getMessage());
                            }
                        }
                    });
                    break;
                case ConnectionState.READY:
                    break;
                case ConnectionState.DISCONNECTED:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(CaptureError error) {
        }
    };
}