package com.brandage.busticketing.ui.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.brandage.busticketing.MainActivity;
import com.brandage.busticketing.R;
import com.brandage.busticketing.SharedPreferenceManager;
import com.brandage.busticketing.model.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends Fragment {

    private static final String TAG = "Register";
    ProgressDialog progressDialog;
    SharedPreferenceManager sharedPreferenceManager;
    EditText username, password;
    Button register_btn;
    FirebaseDatabase database;
    DatabaseReference users_ref;

    public Register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        database = FirebaseDatabase.getInstance();
        users_ref = database.getReference("users");
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait ... ");

        register_btn = root.findViewById(R.id.register_btn);
        username = root.findViewById(R.id.register_email);
        password = root.findViewById(R.id.register_password);

        register_btn.setOnClickListener(v -> {
            if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
                progressDialog.show();
                users_ref.child("register").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.hasChild(username.getText().toString().replace(".", "-"))){
                            progressDialog.dismiss();
                            Student student = new Student(username.getText().toString(), password.getText().toString());
                            users_ref.child("register").child(username.getText().toString().replace(".", "-"))
                                    .setValue(student).addOnCompleteListener(task -> new AlertDialog.Builder(getActivity())
                                            .setTitle("Register")
                                            .setIcon(R.drawable.ic_home_black_24dp)
                                            .setMessage("User Created")
                                            .setNegativeButton("OK", ((dialog, which) -> {
                                                sharedPreferenceManager.save_user_name(username.getText().toString());
                                                sharedPreferenceManager.save_password(password.getText().toString());
                                                startActivity(new Intent(getActivity(), MainActivity.class));
                                                requireActivity().finish();
                                            }))
                                    .show());
                        }else {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Register")
                                    .setIcon(R.drawable.ic_home_black_24dp)
                                    .setMessage("User already exists")
                                    .setNegativeButton("OK", ((dialog, which) -> {
                                        ViewPager viewPager = requireActivity().findViewById(R.id.viewPager);
                                        viewPager.setCurrentItem(2);
                                    })).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return root;
    }
}