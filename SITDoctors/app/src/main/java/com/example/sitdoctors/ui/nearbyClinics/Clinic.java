package com.example.sitdoctors.ui.nearbyClinics;
import org.osmdroid.util.GeoPoint;

public class Clinic {
    public String title;
    public double distance;
    public GeoPoint location;

    public Clinic(String title, double distance, GeoPoint location) {
        this.title = title;
        this.distance = distance;
        this.location = location;
    }
}
