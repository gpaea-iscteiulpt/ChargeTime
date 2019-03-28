package com.tese.chargingtime;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView txtRegister = (TextView) findViewById(R.id.txtRegister);
        String str = "Register new account <b> here </b>";
        txtRegister.setText(Html.fromHtml(str));

        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editEmail.getText().toString();
                final String password = editPassword.getText().toString();

                if(email.isEmpty() || password.isEmpty()){
                    showMessage("Login failed.");
                }else{
                    signIn(email, password);
                }
            }
        });

    }

    public void signIn(final String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mDatabase.child("users").child(email.replace(".", "")).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User mUser = dataSnapshot.getValue(User.class);
                            Constants.setUsername(mUser.getUsername());
                            Constants.setUserPoints(mUser.getPoints());
                            updateUi();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showMessage("Error accessing database.");
                        }
                    });
                }else{
                    showMessage("Login failed! " + task.getException().getMessage());
                }
            }
        });
    }

    private void updateUi(){
        startActivity(new Intent(this, Menu.class));
        finish();
    }

    public void registerUser(View view){
        startActivity(new Intent(this, Register.class));
        finish();
    }

    private void showMessage(String str){
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

}
