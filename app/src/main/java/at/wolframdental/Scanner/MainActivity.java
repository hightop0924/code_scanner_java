package at.wolframdental.Scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.socketmobile.capture.AppKey;
import com.socketmobile.capture.CaptureError;
import com.socketmobile.capture.android.Capture;
import com.socketmobile.capture.client.CaptureClient;
import com.socketmobile.capture.client.ConnectionState;
import com.socketmobile.capture.client.DataEvent;
import com.socketmobile.capture.client.DeviceClient;
import com.socketmobile.capture.client.DeviceState;
import com.socketmobile.capture.client.android.BuildConfig;
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
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private CaptureClient mCaptureClient;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Capture.builder(getApplicationContext())
                .enableLogging(BuildConfig.DEBUG)
                .build();

        startSocketCamExtension();

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
        Toast.makeText(this, "Kamera öffnen implementieren.", Toast.LENGTH_SHORT).show();
        // DeviceClient.DeviceType.kModelSocketCamC820.start();
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

    CaptureExtension.Listener mListener = new CaptureExtension.Listener() {
        @Override
        public void onExtensionStateChanged(ConnectionState connectionState) {
            switch (connectionState.intValue()) {
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