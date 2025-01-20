# Trinity College Course ICS Generator

This project scrapes course schedules from the Trinity College website and generates an `.ics` calendar file for easier integration with calendar applications like Google Calendar or Outlook.

## Features
- **Course Data Scraping**: Uses Selenium to scrape course details, including name, link, schedule, location, and instructor.
- **ICS File Generation**: Converts course data into the iCalendar format with support for weekly recurrence and alarms.
- **Authentication**: Prompts users for Trinity credentials and handles MFA for secure access.
- **Custom Recurrence Rules**: Supports weekly schedules based on course days and times.

---

## Project Structure

### `IcsGenerator.java`
Handles the generation of the `.ics` file. It:
- Builds iCalendar content based on course details.
- Handles weekly recurrence using start and end dates.
- Converts 12-hour time format to 24-hour format.
- Writes the calendar data to a file.

### `Scraper.java`
Scrapes course data from the Trinity College schedule website. It:
- Authenticates using Trinity email and password.
- Handles MFA (Multi-Factor Authentication) prompts.
- Extracts course details, including days, times, and locations.

### `Course.java`
A helper class that represents a course with attributes such as:
- Course name
- Course link
- Instructor
- Type
- Days
- Times
- Location

---

## Requirements

### Software
- **Java 8 or higher**
- **Selenium WebDriver**
- **ChromeDriver**
- **Maven** (for dependency management)

### Libraries
- `selenium-java` (for web scraping)
- `javax.swing` (for user input dialogs)

---

## Setup Instructions

1. **Install Dependencies**
    - Download and install Java 8 or higher.
    - Install Maven for dependency management.
    - Add `selenium-java` and `chromedriver` dependencies to your `pom.xml`.

2. **Set Up ChromeDriver**
    - Download the correct version of ChromeDriver for your browser.
    - Place it in your system's PATH or specify the path in the code.

3. **Run the Program**
    - Compile the project: `mvn compile`.
    - Execute the main method in `IcsGenerator` to generate the `.ics` file.

4. **Access the Generated File**
    - The `.ics` file is saved in the `Downloads` folder by default:
      ```
      C:\Users\[YourUsername]\Downloads\courses.ics
      ```

---

## Usage

1. **Run the Program**  
   Execute the `main` method in `IcsGenerator`.

2. **Login**
    - Enter your Trinity email and password when prompted.
    - Complete the MFA authentication if required.

3. **View the `.ics` File**  
   Import the generated file into your preferred calendar application.

---

## Notes

- Ensure you have a stable internet connection during scraping.
- The program assumes courses follow standard formats. Manual review of generated events is recommended.
- Update the ChromeDriver path if your browser version changes.

---

## Future Improvements

- **Error Handling**: Improve handling for non-standard course schedules.
- **UI Enhancements**: Add a graphical user interface for easier interaction.
- **Dynamic Dates**: Allow users to input custom semester start and end dates.
- **Testing**: Add unit tests for ICS generation and scraping logic.

---
# License
This software is proprietary and not open for public use or distribution. All rights reserved by Nnaemeka Okonkwo.

