package edu.trincoll;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IcsGenerator {

    public static void generateIcs(List<Course> courses) {
        StringBuilder icsContent = new StringBuilder();

        // iCalendar header
        icsContent.append("BEGIN:VCALENDAR\n")
                .append("VERSION:2.0\n")
                .append("CALSCALE:GREGORIAN\n")
                .append("METHOD:PUBLISH\n")
                .append("PRODID:-//Trinity College//EN\n");

        // Define start and end date for the custom recurrence
        String startDate = "20250121";  // January 21, 2025
        String endDate = "20250509";    // May 9, 2025

        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("M", "MO");
        dayMap.put("T", "TU");
        dayMap.put("W", "WE");
        dayMap.put("R", "TH");
        dayMap.put("F", "FR");

        for (Course course : courses) {
            String days = course.getDays();
            String times = course.getTimes();

            // Skip event if days or times are "TBA"
            if (days == null || days.trim().isEmpty() || "TBA".equalsIgnoreCase(days) ||
                    times == null || times.trim().isEmpty() || "TBA".equalsIgnoreCase(times)) {
                continue;
            }

            // Extract start and end times
            String[] timeRange = times.split("\\s*-\\s*");
            String startTime = convertTo24HourFormat(timeRange[0].trim());
            String endTime = timeRange.length > 1 ? convertTo24HourFormat(timeRange[1].trim()) : "";

            String uid = UUID.randomUUID().toString();

            StringBuilder rrule = new StringBuilder("RRULE:FREQ=WEEKLY;UNTIL=" + endDate + "T235959Z;BYDAY=");

            // Split combined days like "MWF" or "TR" into individual day codes
            List<String> expandedDays = new ArrayList<>();
            for (char day : days.toCharArray()) {
                if (dayMap.containsKey(String.valueOf(day))) {
                    expandedDays.add(dayMap.get(String.valueOf(day)));
                }
            }

            // Add day codes to the recurrence rule
            for (String dayCode : expandedDays) {
                rrule.append(dayCode).append(",");
            }

            if (rrule.charAt(rrule.length() - 1) == ',') {
                rrule.deleteCharAt(rrule.length() - 1);
            }

            String startDateTime = startDate + "T" + startTime + "00";
            String endDateTime = startDate + "T" + endTime + "00";

            icsContent.append("BEGIN:VEVENT\n")
                    .append("SUMMARY:").append(course.getCourseName()).append("\n")
                    .append("DESCRIPTION:").append(course.getCourseLink()).append("\n")
                    .append("LOCATION:").append(course.getLocation()).append("\n")
                    .append("UID:").append(uid).append("@trincoll.edu\n")
                    .append("DTSTART;TZID=America/New_York:").append(startDateTime).append("\n")
                    .append("DTEND;TZID=America/New_York:").append(endDateTime).append("\n")
                    .append(rrule).append("\n")
                    .append("SEQUENCE:0\n")
                    .append("STATUS:CONFIRMED\n")
                    .append("TRANSP:OPAQUE\n")
                    .append("BEGIN:VALARM\n")
                    .append("TRIGGER:-PT15M\n")
                    .append("DESCRIPTION:Reminder\n")
                    .append("ACTION:DISPLAY\n")
                    .append("END:VALARM\n")
                    .append("END:VEVENT\n");
            System.out.println("Generated Event:");
            System.out.println(icsContent.substring(icsContent.lastIndexOf("BEGIN:VEVENT"), icsContent.length()));
        }

        icsContent.append("END:VCALENDAR");

        String filePath = "C:\\Users\\nokon\\Downloads\\courses.ics";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(icsContent.toString());
            System.out.println("ICS file generated successfully at: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to ICS file: " + e.getMessage());
        }
    }

    private static String convertTo24HourFormat(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mma");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HHmm");
        try {
            return outputFormat.format(sdf.parse(time));
        } catch (Exception e) {
            System.err.println("Error parsing time: " + time);
            return "0900"; // Default to 9:00 AM in case of error
        }
    }

    public static void main(String[] args) {
        List<Course> courses = Scraper.scrapeCourseData("https://internet3.trincoll.edu/spTools/ClassSchedule.aspx");
        generateIcs(courses);
    }
}
