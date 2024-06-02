package com.example.librecord;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.view.View;
import android.widget.*;
import android.content.*;
import android.os.Bundle;
import android.util.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    Button backButton, signup;
    Intent loginPage;
    Connect connection;
    Connection conn;
    String tableName;
    EditText username, email, password, libID;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        connection = new Connect();

        BackToLogin();
    }

    public void BackToLogin(){
        backButton = (Button) findViewById(R.id.back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginPage);
                finish();
            }
        });

        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        libID = (EditText) findViewById(R.id.libid);

        signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp(username, email, password, libID);
            }
        });
    }

    public void SignUp(EditText username, EditText email, EditText password, EditText libID){
        tableName = "AccountData";
        status = (TextView) findViewById(R.id.status);
        Log.d("Testing Value", username.getText().toString());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            try {
                conn = connection.CONN();
                if (conn == null){
                    Log.d("Connection Error", "The connection is null");
                }
                String query = "INSERT INTO "+tableName+"(username, email, password, LibID) VALUES (\""+username.getText().toString()+
                        "\",\""+email.getText().toString()+"\",\""+password.getText().toString()+"\","+libID.getText().toString()+")";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.execute();
                runOnUiThread(()->{
                    try {
                        Thread.sleep(1000);
                        status.setText("SIGN UP SUCCESSFULLY!");
                    } catch (Exception e){
                        Log.d("Thread error", "Thread Error");
                    }
                });
            } catch (SQLException e){
                runOnUiThread(()->{
                    try {
                        Thread.sleep(1000);
                        status.setText("SIGN UP UNSUCCESSFUL");
                        status.setTextColor(Color.GREEN);
                    } catch (Exception e1){
                        Log.d("Thread error", "Thread Error");
                    }
                });
                Log.d("SQL Error", "There is something wrong");
            }
        });
    }
}