package com.brandage.busticketing.ui.home;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.brandage.busticketing.R;
import com.brandage.busticketing.SharedPreferenceManager;
import com.brandage.busticketing.model.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment {

    DatePickerDialog picker;
    Spinner from_spinner, to_spinner;
    String to, from;
    TextView txt_available_buses, home_user_id;
    EditText filter_date;
    String filter;
    Button buy_ticket_btn;
    ArrayAdapter arrayAdapter;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    DatabaseReference ticket_ref, bus_count;
    SharedPreferenceManager sharedPreferenceManager;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        filter = null;
        filter_date = root.findViewById(R.id.customer_filter_date_edt);
        from_spinner = root.findViewById(R.id.from_spinner);
        to_spinner = root.findViewById(R.id.to_spinner);
        txt_available_buses = root.findViewById(R.id.txt_available_buses);
        home_user_id = root.findViewById(R.id.home_user_id);
        buy_ticket_btn = root.findViewById(R.id.buy_ticket_btn);

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait ...");
        progressDialog.setTitle("Buying Ticket");

        database = FirebaseDatabase.getInstance();
        ticket_ref = database.getReference("tickets");
        bus_count = database.getReference("bus_count");
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireActivity());

        arrayAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.locations,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        from_spinner.setAdapter(arrayAdapter);
        to_spinner.setAdapter(arrayAdapter);
        to_spinner.setSelection(1);

        home_user_id.setText(sharedPreferenceManager.get_user_name());

        bus_count.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_available_buses.setText(String.valueOf(snapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        from_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from = parent.getItemAtPosition(position).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        to_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                to = parent.getItemAtPosition(position).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        filter_date.setInputType(InputType.TYPE_NULL);

        filter_date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            picker = new DatePickerDialog(requireActivity(), (view, y, m, dayOfMonth) ->
                    filter_date.setText((String.valueOf(dayOfMonth).length() == 1 ? "0" + (dayOfMonth) : (dayOfMonth))
                            + "/" + ((m + 1) <= 9 ? "0" + (m + 1) : (m + 1)) + "/" + y),
                    year, month, day);
            picker.show();
        });

        buy_ticket_btn.setOnClickListener(v -> {
            if (!filter_date.getText().toString().isEmpty() && !sharedPreferenceManager.get_user_name().isEmpty()){
                dialog_ticket_count();
            }else {
                Toast.makeText(getActivity(), "Please select date", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    private void dialog_ticket_count(){
        final Dialog d = new Dialog(requireActivity());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_ticket_count);
        d.setCancelable(true);
        Objects.requireNonNull(d.getWindow())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
        d.show();

        EditText edt_ticket_count = d.findViewById(R.id.edt_ticket_count);
        Button button_done = d.findViewById(R.id.add_ticket_count);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        button_done.setOnClickListener(view -> {
            if (!edt_ticket_count.getText().toString().isEmpty()) {

                Booking booking = new Booking();
                booking.setStart(from);
                booking.setDestination(to);
                booking.setId(sharedPreferenceManager.get_user_name());
                booking.setDate(filter_date.getText().toString());
                booking.setApproved(false);
                booking.setAmount("100.0");
                booking.setCount(Integer.parseInt(edt_ticket_count.getText().toString()));

                String uuid = ticket_ref.push().getKey();

                booking.setUuid(uuid);

                buy_ticket_btn.setVisibility(View.GONE);
                progressDialog.show();

                ticket_ref.child(booking.getUuid()).setValue(booking).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(getActivity(), "Bought", Toast.LENGTH_SHORT).show();
                        buy_ticket_btn.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        filter_date.setText(null);
                    }else {
                        buy_ticket_btn.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed! Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
                d.dismiss();
                d.getWindow().setAttributes(lp);
            } else {
                edt_ticket_count.setError("Cannot be empty");
            }
        });
    }
}