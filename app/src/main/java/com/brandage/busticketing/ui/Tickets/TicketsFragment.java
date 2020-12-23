package com.brandage.busticketing.ui.Tickets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

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

public class TicketsFragment extends Fragment {

    private static final String TAG = "TicketsFragment";
    FirebaseDatabase database;
    DatabaseReference ticket_ref;
    SharedPreferenceManager sharedPreferenceManager;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Booking> bookings;
    GridView gridView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mytickets, container, false);

        gridView = root.findViewById(R.id.tickets_grid);
        swipeRefreshLayout = root.findViewById(R.id.swipe);
        bookings = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        ticket_ref = database.getReference("tickets");
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireActivity());

        swipeRefreshLayout.setRefreshing(true);

        get_tickets();

        swipeRefreshLayout.setOnRefreshListener(this::get_tickets);

        return root;
    }

    public void get_tickets(){
        ticket_ref.orderByChild("id").equalTo(sharedPreferenceManager.get_user_name())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookings.clear();
                swipeRefreshLayout.setRefreshing(false);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Booking booking = new Booking();
                    if (dataSnapshot.hasChild("uuid")){
                        booking.setUuid(Objects.requireNonNull(dataSnapshot.child("uuid").getValue()).toString());
                    }
                    booking.setCount(Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("count").getValue()).toString()));
                    booking.setAmount(Objects.requireNonNull(dataSnapshot.child("amount").getValue()).toString());
                    booking.setDate(Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString());
                    booking.setApproved(Boolean.parseBoolean(Objects.requireNonNull(dataSnapshot.child("approved").getValue()).toString()));
                    booking.setDestination(Objects.requireNonNull(dataSnapshot.child("destination").getValue()).toString());
                    booking.setStart(Objects.requireNonNull(dataSnapshot.child("start").getValue()).toString());
                    booking.setId(Objects.requireNonNull(dataSnapshot.child("id").getValue()).toString());

                    bookings.add(booking);
                }
                CustomAdapter customAdapter = new CustomAdapter(getActivity(), bookings);
                gridView.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class CustomAdapter extends BaseAdapter {

        Context context;
        List<Booking> bookings;
        LayoutInflater inflater;

        public CustomAdapter(Context applicationContext, List<Booking> bookings) {
            this.context = applicationContext;
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

        @SuppressLint("ViewHolder")
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
            return view;
        }
    }
}