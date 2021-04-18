package com.brandage.busticketing.ui.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brandage.busticketing.R;
import com.brandage.busticketing.SharedPreferenceManager;
import com.brandage.busticketing.model.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference ticket_ref, admin_ref, bus_count;
    SharedPreferenceManager sharedPreferenceManager;
    GridView gridView;
    List<Booking> bookings;
    ProgressDialog progressDialog;
    Button bus_count_btn;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onStart() {
        super.onStart();
        if (sharedPreferenceManager.get_admin()) {
            bus_count_btn.setVisibility(View.VISIBLE);
            get_ticket();
        }else {
            bus_count_btn.setVisibility(View.GONE);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin, container, false);

        gridView = root.findViewById(R.id.admin_tickets_grid);
        bus_count_btn = root.findViewById(R.id.bus_count_btn);
        swipeRefreshLayout = root.findViewById(R.id.swipe);
        bookings = new ArrayList<>();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Logging In ... ");

        database = FirebaseDatabase.getInstance();
        ticket_ref = database.getReference("tickets");
        admin_ref = database.getReference("admin");
        bus_count = database.getReference("bus_count");
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireActivity());

        bus_count_btn.setOnClickListener(v -> dialog_bus_count());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (sharedPreferenceManager.get_admin()){
                get_ticket();
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        return root;
    }

    public class CustomAdapter extends BaseAdapter {

        Context context;
        List<Booking> bookings;
        LayoutInflater inflater;

        public CustomAdapter(Context context, List<Booking> bookings) {
            this.context = context;
            this.bookings = bookings;
        }

        @Override
        public int getCount() {
            return bookings.size();
        }

        @Override
        public Object getItem(int i) {
            return bookings.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.model_ticket, null);
            TextView id = view.findViewById(R.id.model_ticket_id);
            TextView from = view.findViewById(R.id.model_from);
            TextView to = view.findViewById(R.id.model_to);
            TextView date = view.findViewById(R.id.model_date);
            TextView count = view.findViewById(R.id.model_count);

            if (bookings.get(i).isApproved()){
                view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
            } else {
                view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
            }

            id.setText(bookings.get(i).getId());
            from.setText(bookings.get(i).getStart());
            to.setText(bookings.get(i).getDestination());
            date.setText(bookings.get(i).getDate());
            count.setText(String.valueOf(bookings.get(i).getCount()));

            view.setOnClickListener(v -> dialog_ticket_options(bookings.get(i)));
            return view;
        }
    }

    @SuppressLint("SetTextI18n")
    public void dialog_ticket_options(Booking booking){
        final Dialog d = new Dialog(requireActivity());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_ticket_options);
        d.setCancelable(true);
        Objects.requireNonNull(d.getWindow())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
        d.show();

        Button delete_button =d.findViewById(R.id.ticket_option_delete);
        Button approve_button =d.findViewById(R.id.ticket_option_approve);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        if (booking.isApproved()){
            approve_button.setText("Decline");
        }else {
            approve_button.setText("Approve");
        }

        approve_button.setOnClickListener(v -> {
            if (booking.isApproved()){
                booking.setApproved(false);
            }else if (!booking.isApproved()){
                booking.setApproved(true);
            }
            ticket_ref.child(booking.getUuid()).setValue(booking).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    d.dismiss();
                    d.getWindow().setAttributes(lp);
                }else {
                    d.dismiss();
                    d.getWindow().setAttributes(lp);
                    Toast.makeText(getActivity(), "No Success", Toast.LENGTH_SHORT).show();
                }
            });
        });

        delete_button.setOnClickListener(v -> ticket_ref.child(booking.getUuid())
                .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        d.dismiss();
                        d.getWindow().setAttributes(lp);
                        Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                    }else {
                        d.dismiss();
                        d.getWindow().setAttributes(lp);
                        Toast.makeText(getActivity(), "No success", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void dialog_bus_count(){
        final Dialog d = new Dialog(requireActivity());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_bus_count);
        d.setCancelable(true);
        Objects.requireNonNull(d.getWindow())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
        d.show();

        EditText edt_bus_count = d.findViewById(R.id.edt_bus_count);
        Button button_done = d.findViewById(R.id.add_bus_count);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        button_done.setOnClickListener(view -> {
            if (!edt_bus_count.getText().toString().isEmpty()) {

                bus_count.setValue(edt_bus_count.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(), "Failed! Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
                d.dismiss();
                d.getWindow().setAttributes(lp);
            } else {
                edt_bus_count.setError("Cannot be empty");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void dialog(){
        final Dialog d = new Dialog(requireActivity());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_admin_login);
        d.setCancelable(true);
        Objects.requireNonNull(d.getWindow())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
        d.show();

        EditText username = d.findViewById(R.id.admin_login_username);
        EditText password = d.findViewById(R.id.admin_login_password);
        Button button_done = d.findViewById(R.id.admin_login_btn);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        button_done.setOnClickListener(v -> {
            if (!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()) {
                progressDialog.show();
                admin_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            if (Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString()
                                    .equals(username.getText().toString()) &&
                                    Objects.requireNonNull(dataSnapshot.child("password").getValue()).toString()
                                            .equals(password.getText().toString())){

                                bus_count_btn.setVisibility(View.VISIBLE);
                                get_ticket();

                                d.dismiss();
                                d.getWindow().setAttributes(lp);
                                progressDialog.dismiss();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(requireActivity(), "No Success", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    public void get_ticket(){
        ticket_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookings.clear();
                swipeRefreshLayout.setRefreshing(false);
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Booking booking = new Booking();
                    if (dataSnapshot.hasChild("uuid")) {
                        booking.setUuid(Objects.requireNonNull(dataSnapshot.child("uuid").getValue()).toString());
                    }
                    booking.setCount(Objects.requireNonNull(Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("count").getValue()).toString())));
                    booking.setAmount(Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString());
                    booking.setDate(Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString());
                    booking.setApproved(Boolean.parseBoolean(Objects.requireNonNull(dataSnapshot.child("approved").getValue()).toString()));
                    booking.setDestination(Objects.requireNonNull(dataSnapshot.child("destination").getValue()).toString());
                    booking.setStart(Objects.requireNonNull(dataSnapshot.child("start").getValue()).toString());
                    booking.setId(Objects.requireNonNull(dataSnapshot.child("id").getValue()).toString());

                    bookings.add(booking);
                }
                progressDialog.dismiss();
                CustomAdapter customAdapter = new CustomAdapter(getActivity(), bookings);
                gridView.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}