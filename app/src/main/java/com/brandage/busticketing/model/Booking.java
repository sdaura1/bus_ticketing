package com.brandage.busticketing.model;

public class Booking {

    String uuid;
    String id;
    String start;
    String destination;
    String date;
    String amount;
    int count;
    boolean approved;

    public Booking() {

    }

    public Booking(String uuid, String id, String start, String destination, String date,
                   String amount, boolean approved, int count) {
        this.uuid = uuid;
        this.id = id;
        this.start = start;
        this.count = count;
        this.approved = approved;
        this.destination = destination;
        this.amount = amount;
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(String amount){
        this.amount = amount;
    }

    public String getAmount(){
        return amount;
    }
}
