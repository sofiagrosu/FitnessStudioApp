package com.fitness.fitness_app.model;

public class CheckInResult {
    private boolean allowed;
    private String message;
    private String screenColor;
    private CheckIn checkIn;

    public CheckInResult() {}

    public CheckInResult(boolean allowed, String message, String screenColor, CheckIn checkIn) {
        this.allowed = allowed;
        this.message = message;
        this.screenColor = screenColor;
        this.checkIn = checkIn;
    }

    public static CheckInResult green(String message, CheckIn checkIn) {
        return new CheckInResult(true, message, "GREEN", checkIn);
    }

    public static CheckInResult red(String message) {
        return new CheckInResult(false, message, "RED", null);
    }

    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getScreenColor() { return screenColor; }
    public void setScreenColor(String screenColor) { this.screenColor = screenColor; }
    public CheckIn getCheckIn() { return checkIn; }
    public void setCheckIn(CheckIn checkIn) { this.checkIn = checkIn; }
}
