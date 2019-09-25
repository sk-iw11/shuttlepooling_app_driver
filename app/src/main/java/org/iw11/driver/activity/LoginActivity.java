package org.iw11.driver.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.iw11.driver.R;
import org.iw11.driver.network.RestServiceFactory;
import org.iw11.driver.network.TokenManager;
import org.iw11.driver.network.model.BusCredentials;
import org.iw11.driver.network.model.TokenModel;

import javax.xml.datatype.Duration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText inputName;
    private EditText inputPassword;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(getApplicationContext());
        if (tokenManager.getToken() != null) {
            Intent intent = new Intent(getApplicationContext(), TrackingActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_login);
        initViews();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = inputName.getText().toString();
                String password = inputPassword.getText().toString();

                if (name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter credentials!", Toast.LENGTH_LONG).show();
                    return;
                }

                loginButton.setEnabled(false);

                Call<TokenModel> call = RestServiceFactory.getApiService().postLogin(new BusCredentials(name, password));
                call.enqueue(new Callback<TokenModel>() {
                    @Override
                    public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                        if (response.code() != 200) {
                            Toast.makeText(getApplicationContext(), "Auth failed!", Toast.LENGTH_LONG).show();
                            loginButton.setEnabled(true);
                            return;
                        }

                        tokenManager.setToken(response.body().getToken());

                        Intent intent = new Intent(getApplicationContext(), TrackingActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<TokenModel> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error connecting to server!", Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void initViews() {
        inputName = findViewById(R.id.input_login);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.button_login);
    }
}
