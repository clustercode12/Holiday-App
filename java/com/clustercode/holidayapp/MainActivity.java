package net.clustercode.holidayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView txtUser, txtEstimatedYearlyHolidayEntitlement, txtDaysAccruedToDateThisHolidayYear, txtDaysTakenAndBookedThisHolidayYear, txtDaysRemainingThisHolidayYear, txtHolidayYear;
    private Sheets sheetsService;
    private String spreadsheetId;
    private static final int REQUESTCODE = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUser = findViewById(R.id.txtUser);
        txtEstimatedYearlyHolidayEntitlement = findViewById(R.id.txtEstimatedYearlyHolidayEntitlement);
        txtDaysAccruedToDateThisHolidayYear = findViewById(R.id.txtDaysAccruedToDateThisHolidayYear);
        txtDaysTakenAndBookedThisHolidayYear = findViewById(R.id.txtDaysTakenAndBookedThisHolidayYear);
        txtDaysRemainingThisHolidayYear = findViewById(R.id.txtDaysRemainingThisHolidayYear);
        txtHolidayYear = findViewById(R.id.txtHolidayYear);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();
        sheetsService = new Sheets.Builder(transport, factory, null)
                .setApplicationName("Holiday App")
                .build();
        spreadsheetId = Config.spreadsheet_id;

        signIn();
    }

    private void signIn() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivityForResult(intent, REQUESTCODE);
    }

    private void getInfoFromUser(final int position) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String range = "Sheet1!C" + position + ":G" + position;
                    ValueRange result = sheetsService.spreadsheets().values()
                            .get(spreadsheetId, range)
                            .setKey(Config.google_api_key)
                            .execute();
                    final List<Object> strings = result.getValues().get(0);

                    txtDaysAccruedToDateThisHolidayYear.post(new Runnable() {
                        @Override
                        public void run() {
                            txtDaysAccruedToDateThisHolidayYear.setText(strings.get(1).toString());
                            txtEstimatedYearlyHolidayEntitlement.setText(strings.get(0).toString());
                            txtDaysRemainingThisHolidayYear.setText(strings.get(3).toString());
                            txtDaysTakenAndBookedThisHolidayYear.setText(strings.get(2).toString());
                            txtHolidayYear.setText(strings.get(4).toString());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("username");
                int result = data.getIntExtra("result", 0);
                if (result != 0) {
                    txtUser.setText(username);
                    getInfoFromUser(result);
                }
            }
        }
    }
}
