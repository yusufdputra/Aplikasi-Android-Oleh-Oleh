package com.ysf.oleholeh.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.ysf.oleholeh.R;

public class LupaPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back;
    private TextInputLayout error_email;
    private EditText email;
    private Button btn_reset;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupa_password);

        init();
        setOnClickListener();
    }

    private void setOnClickListener() {
        iv_back.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

    }

    private void init() {

        iv_back = findViewById(R.id.iv_back);
        error_email = findViewById(R.id.error_email);
        email = findViewById(R.id.et_email);
        btn_reset = findViewById(R.id.btn_reset);
        progressBar = findViewById(R.id.progressBar);

        auth =  FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.iv_back:
                this.onBackPressed();
                this.finish();
                break;

            case R.id.btn_reset:
                if (!validasiEmail()){
                    return;
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    String get_email = email.getText().toString();

                    auth.sendPasswordResetEmail(get_email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Snackbar.make(v, "Berhasil mengirim reset password. Silahkan cek email anda", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                    }else{
                                        Snackbar.make(v, "Gagal mengirim reset password", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                }
                            });
                }
                break;
        }
    }



    private boolean validasiEmail() {
        String email = error_email.getEditText().getText().toString().trim();
        if (email.isEmpty()) {
            error_email.setError("Field tidak boleh kosong");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error_email.setError("Masukkan alamat email yang benar");
            return false;
        } else {
            error_email.setError(null);
            return true;
        }
    }

}
