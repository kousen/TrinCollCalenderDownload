package edu.trincoll;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Scraper {

    public static List<Course> scrapeCourseData(String url) throws IOException {
        List<Course> courses = new ArrayList<>();

        // Set up Chrome options
        // Do not run headless
        ChromeOptions options = new ChromeOptions();
        Path userDataDir = Files.createTempDirectory("chrome-profile");
        options.addArguments("--user-data-dir=" + userDataDir.toString());
        WebDriver driver = new ChromeDriver(options);


        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));

            // Wait for user to input credentials and handle the login process
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("i0116"))); // Wait for email input
            System.out.println("Please log in using your Trinity credentials.");

            // Wait for the course table to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tr.TITLE_row, tr.TITLE_row_alt")));

            List<WebElement> courseRows = driver.findElements(By.cssSelector("tr.TITLE_row, tr.TITLE_row_alt"));
            for (WebElement row : courseRows) {
                WebElement courseLinkElement = row.findElement(By.cssSelector("a[href]"));
                String courseName = courseLinkElement.getText();
                String courseLink = courseLinkElement.getAttribute("href");

                WebElement nextRow = row.findElement(By.xpath("following-sibling::tr"));
                if (nextRow != null) {
                    List<WebElement> nextRowColumns = nextRow.findElements(By.tagName("td"));
                    if (nextRowColumns.size() > 1) {
                        String type = nextRowColumns.get(1).getText();
                        String daysAndTimes = nextRow.findElement(By.cssSelector("td.TITLE_times")).getText();
                        String location = nextRowColumns.size() > 3 ? nextRowColumns.get(3).getText() : "N/A";

                        String instructor = "N/A";
                        try {
                            WebElement instructorEmailElement = nextRow.findElement(By.cssSelector("a[href^=mailto]"));
                            instructor = instructorEmailElement.getText();
                        } catch (Exception e) {
                            System.out.println("Instructor email not found.");
                        }

                        String[] dayAndTime = extractDaysAndTimes(daysAndTimes);
                        String days = dayAndTime[0];
                        String times = dayAndTime[1];

                        Course course = new Course(courseName, courseLink, instructor, type, days, times, location);
                        courses.add(course);

                        System.out.println("Course Name: " + courseName);
                        System.out.println("Course Link: " + courseLink);
                        System.out.println("Instructor: " + instructor);
                        System.out.println("Type: " + type);
                        System.out.println("Days: " + days);
                        System.out.println("Times: " + times);
                        System.out.println("Location: " + location);
                        System.out.println("---------------------------");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return courses;
    }

    private static String[] extractDaysAndTimes(String daysAndTimes) {
        daysAndTimes = daysAndTimes.trim();

        // Handle "TBA" case
        if (daysAndTimes.contains("TBA")) {
            return new String[]{"", ""};
        }

        String[] result = new String[2];

        String[] parts = daysAndTimes.split(":", 2);

        if (parts.length == 2) {
            result[0] = parts[0].trim();
            String time = parts[1].trim();

            String[] timeParts = time.split(" - ", 2);

            if (timeParts.length == 2) {
                result[1] = timeParts[0].trim() + "-" + timeParts[1].trim();
            } else {
                result[1] = timeParts[0].trim();
            }
        } else {
            result[0] = daysAndTimes;
            result[1] = "";
        }

        return result;
    }

}
