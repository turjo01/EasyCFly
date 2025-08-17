package dev.turjo.easycfly.models;

import java.util.UUID;

public class FlightData {
    
    private final UUID playerUUID;
    private final long startTime;
    private long totalFlightTime;
    private double totalDistance;
    private int timesFlown;
    
    public FlightData(UUID playerUUID, long startTime) {
        this.playerUUID = playerUUID;
        this.startTime = startTime;
        this.totalFlightTime = 0;
        this.totalDistance = 0;
        this.timesFlown = 0;
    }
    
    public FlightData(UUID playerUUID, long startTime, long totalFlightTime, double totalDistance, int timesFlown) {
        this.playerUUID = playerUUID;
        this.startTime = startTime;
        this.totalFlightTime = totalFlightTime;
        this.totalDistance = totalDistance;
        this.timesFlown = timesFlown;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getTotalFlightTime() {
        return totalFlightTime;
    }
    
    public void setTotalFlightTime(long totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
    
    public int getTimesFlown() {
        return timesFlown;
    }
    
    public void setTimesFlown(int timesFlown) {
        this.timesFlown = timesFlown;
    }
    
    public void addFlightTime(long time) {
        this.totalFlightTime += time;
    }
    
    public void addDistance(double distance) {
        this.totalDistance += distance;
    }
    
    public void incrementTimesFlown() {
        this.timesFlown++;
    }
}