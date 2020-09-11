package net.clustercode.holidayapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private TextView txtInfo;
    private EditText etUsername, etPassword;
    private Sheets sheetsService;
    private String spreadsheetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;
        int windowHeight = displayMetrics.heightPixels;
        getWindow().setLayout((int)(windowWidth*.8), (int)(windowHeight*.5));

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();
        sheetsService = new Sheets.Builder(transport, factory, null)
                .setApplicationName("Holiday App")
                .build();
        spreadsheetId = Config.spreadsheet_id;

        txtInfo = findViewById(R.id.txtInfo);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    public void signIn(View view) {
        txtInfo.setVisibility(View.GONE);
        new Thread() {
            @Override
            public void run() {
                try {
                    String range = "Sheet1!A1";
                    ValueRange result = sheetsService.spreadsheets().values()
                            .get(spreadsheetId, range)
                            .setKey(Config.google_api_key)
                            .execute();
                    int numberOfUser = Integer.parseInt(result.getValues().get(0).get(0).toString());

                    String range2 = "Sheet1!A3:B" + (3+numberOfUser);
                    ValueRange result2 = sheetsService.spreadsheets().values()
                            .get(spreadsheetId, range2)
                            .setKey(Config.google_api_key)
                            .execute();

                    for (int i = 0; i < numberOfUser; i++) {
                        if (etUsername.getText().toString().equals(result2.getValues().get(i).get(0).toString()) && etPassword.getText().toString().equals(result2.getValues().get(i).get(1).toString())) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result",(i+3));
                            returnIntent.putExtra("username",etUsername.getText().toString());
                            setResult(RESULT_OK,returnIntent);
                            finish();
                        }
                    }
                    txtInfo.post(new Runnable() {
                        @Override
                        public void run() {
                            txtInfo.setVisibility(View.VISIBLE);
                        }
                    });
                }
                catch (IOException e) {
                    Log.e("Test", Objects.requireNonNull(e.getLocalizedMessage()));
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {

    }
}
