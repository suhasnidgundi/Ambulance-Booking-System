package com.svcp.ambulancebookingsystem.data.model;

public class DashboardSummary {
    private int activeBookings;
    private int totalBookings;
    private int totalAmbulances;
    private int totalDrivers;
    private int totalUsers;

    public DashboardSummary() {
        // Default constructor
    }

    public DashboardSummary(int activeBookings, int totalBookings, int totalAmbulances, int totalDrivers, int totalUsers) {
        this.activeBookings = activeBookings;
        this.totalBookings = totalBookings;
        this.totalAmbulances = totalAmbulances;
        this.totalDrivers = totalDrivers;
        this.totalUsers = totalUsers;
    }

    public int getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(int activeBookings) {
        this.activeBookings = activeBookings;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getTotalAmbulances() {
        return totalAmbulances;
    }

    public void setTotalAmbulances(int totalAmbulances) {
        this.totalAmbulances = totalAmbulances;
    }

    public int getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(int totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }
}