package com.svcp.ambulancebookingsystem.data.model;

public class EmergencyContact {
    private String contactId;
    private String name;
    private String phone;
    private String relation;

    public EmergencyContact() {
        // Required empty constructor for Firestore
    }

    public EmergencyContact(String contactId, String name, String phone, String relation) {
        this.contactId = contactId;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
    }

    // Getters and Setters
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}