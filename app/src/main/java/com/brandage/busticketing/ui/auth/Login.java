package com.brandage.busticketing.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.brandage.busticketing.MainActivity;
import com.brandage.busticketing.R;
import com.brandage.busticketing.SharedPreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends Fragment {

    private static final String TAG = "Login";
    ProgressDialog progressDialog;
    String student_reg_format, admin_reg_format;
    SharedPreferenceManager sharedPreferenceManager;
    EditText username, password;
    Button login;
    FirebaseDatabase database;
    DatabaseReference users_ref;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        student_reg_format = "[A-Z]{3}/\\d{2}/[A-Z]{3}/\\d{4}";
        admin_reg_format = "[A-Z]{2}/\\d{3}";
        database = FirebaseDatabase.getInstance();
        users_ref = database.getReference("users");
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait ... ");

        login = root.findViewById(R.id.login_btn);
        username = root.findViewById(R.id.login_email);
        password = root.findViewById(R.id.login_password);

        login.setOnClickListener(v -> {
            if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                if (username.getText().toString().matches(student_reg_format)) {
                    progressDialog.show();
                    users_ref.child("register").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(username.getText().toString().replace("/", "-"))) {
                                if (Objects.equals(snapshot.child(username.getText().toString().replace("/", "-"))
                                        .child("username").getValue(), username.getText().toString())
                                        && Objects.equals(snapshot.child(username.getText().toString().replace("/", "-"))
                                        .child("password").getValue(), password.getText().toString())) {

                                    progressDialog.dismiss();
                                    sharedPreferenceManager.save_admin(false);
                                    sharedPreferenceManager.save_user_name(username.getText().toString());

                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                    requireActivity().finish();
                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else if (username.getText().toString().matches(admin_reg_format)) {
                    progressDialog.show();
                    users_ref.child("register").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(username.getText().toString().replace("/", "-"))) {
                                if (Objects.equals(snapshot.child(username.getText().toString().replace("/", "-"))
                                        .child("username").getValue(), username.getText().toString())
                                        && Objects.equals(snapshot.child(username.getText().toString().replace("/", "-"))
                                        .child("password").getValue(), password.getText().toString())) {

                                    progressDialog.dismiss();
                                    sharedPreferenceManager.save_admin(true);
                                    sharedPreferenceManager.save_user_name(username.getText().toString());

                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                    requireActivity().finish();
                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    Toast.makeText(getActivity(), "Not a match", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }
}