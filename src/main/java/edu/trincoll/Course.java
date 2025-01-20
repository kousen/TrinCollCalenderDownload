package edu.trincoll;

public class Course {
    private String courseName;
    private String courseLink;
    private String instructor;
    private String type;
    private String days;  // Separate days
    private String times; // Separate times
    private String location;

    public Course(String courseName, String courseLink, String instructor, String type, String days, String times, String location) {
        this.courseName = courseName;
        this.courseLink = courseLink;
        this.instructor = instructor;
        this.type = type;
        this.days = days;
        this.times = times;
        this.location = location;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getType() {
        return type;
    }

    public String getDays() {
        return days;
    }

    public String getTimes() {
        return times;
    }

    public String getLocation() {
        return location;
    }
}
