package com.example.finalapp.Models;

public class Transaction {

    public enum Type {
        INCOME,
        EXPENSE
    }
    private String transactionName;
    private float amount;
    private String date;
    private String category;
    private String note;
    private int color;
    private Type type;

    private String id;


    public Transaction(String transactionName, float amount, String date, String category, String note, int color, Type type) {
        this.transactionName = transactionName;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.note = note;
        this.color = color;
        this.type = type;
        this.id = "0";
    }

    public Transaction() {
    }


    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionName='" + transactionName + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", color=" + color +
                ", type=" + type +
                ", id='" + id + '\'' +
                '}';
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
