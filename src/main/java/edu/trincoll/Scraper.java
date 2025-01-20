package edu.trincoll;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Scraper {

    public static List<Course> scrapeCourseData(String url) {
        List<Course> courses = new ArrayList<>();

        // Prompt user for credentials using a simple GUI
        String email = JOptionPane.showInputDialog(null, "Enter your Trinity email (Your Office 365 login is your TC username followed by @trincoll.edu. Example: jdoe5@trincoll.edu)", "Login", JOptionPane.PLAIN_MESSAGE);
        if (email == null || email.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Email is required to proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return courses;
        }

        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(null, passwordField, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION || passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(null, "Password is required to proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return courses;
        }
        String password = new String(passwordField.getPassword());

        // Set up Chrome options (headless optional)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Uncomment to run headless
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
            emailField.sendKeys(email);
            driver.findElement(By.id("idSIButton9")).click(); // Click "Next"

            WebElement passwordFieldElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0118")));
            passwordFieldElement.sendKeys(password);
            driver.findElement(By.id("idSIButton9")).click(); // Click "Sign in"

            // Wait for potential MFA prompt
            try {
                WebElement mfaPrompt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idDiv_SAOTCAS_Title")));
                System.out.println("MFA code prompt detected. Please enter the code on your phone.");

                WebElement signInRequestNumberElement = driver.findElement(By.id("idRichContext_DisplaySign"));
                String signInRequestNumber = signInRequestNumberElement.getText();

                JOptionPane.showMessageDialog(null,
                        "MFA Code: " + signInRequestNumber + "\nOpen your Authenticator app, and enter the number shown to sign in.",
                        "MFA Code Required", JOptionPane.INFORMATION_MESSAGE);

                // Wait for a few seconds to allow manual MFA code entry
                Thread.sleep(20000); // Wait for 20 seconds (adjust as needed)

                // Handle "Stay signed in?" prompt if it appears
                WebElement staySignedIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idBtn_Back")));
                staySignedIn.click(); // Choose "No"
            } catch (Exception e) {
                System.out.println("No MFA prompt detected or it was bypassed automatically.");
            }


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
