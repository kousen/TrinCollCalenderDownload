# Code Review: Trinity College Calendar Download

## Overview

This code review analyzes the Trinity College Calendar Download project, which scrapes course schedules from Trinity College's website and generates ICS calendar files for import into calendar applications.

## Project Summary

**Technology Stack:**
- Language: Java
- Build Tool: Maven
- Key Libraries: Selenium WebDriver (4.27.0), JSoup (1.18.3/1.10.2), JUnit (3.8.1)
- Architecture: Three-class design with separation of concerns

**Core Functionality:**
1. Authenticates via Microsoft login through automated browser
2. Scrapes course data from Trinity's schedule website
3. Generates ICS files with recurring weekly events

## Strengths

✅ **Clear Separation of Concerns**
- `Course.java`: Clean data model
- `Scraper.java`: Focused on web scraping logic
- `IcsGenerator.java`: Dedicated to ICS file generation

✅ **Functional Core Features**
- Successfully handles Microsoft authentication flow
- Generates standard-compliant ICS files
- Handles recurring events with proper RRULE syntax

✅ **Security Awareness**
- No hardcoded credentials
- Uses browser-based authentication

## Critical Issues

### 1. Security Vulnerabilities

🔴 **Temporary Directory Cleanup**
```java
Path userDataDir = Files.createTempDirectory("chrome-profile");
```
- Creates temporary Chrome profiles but never cleans them up
- Could leave sensitive session data on disk

🔴 **Hardcoded File Path**
```java
String filePath = "C:\\Users\\nokon\\Downloads\\courses.ics";
```
- Exposes developer's username
- Windows-specific path won't work on other systems

🟡 **No Input Validation**
- Scraped data used directly without sanitization
- Could lead to injection vulnerabilities in generated ICS files

### 2. Code Quality Issues

🔴 **Poor Error Handling**
```java
} catch (Exception e) {
    e.printStackTrace();
}
```
- Generic exception catching throughout
- No proper logging framework
- Silent failures with incorrect assumptions

🔴 **Hardcoded Values**
- Semester dates (Jan 21 - May 9, 2025)
- Trinity URL
- File paths
- No configuration management

🟡 **Resource Management**
- WebDriver properly closed ✓
- But temporary directories not cleaned up

### 3. Design & Architecture

🔴 **Tight Coupling**
- Static methods make testing difficult
- No dependency injection
- No interface abstractions

🔴 **Platform Dependency**
- Windows-specific file paths
- No cross-platform considerations

🟡 **Missing Tests**
- JUnit dependency present but no tests written
- Critical parsing logic untested

### 4. Build Configuration Issues

🔴 **Duplicate Dependencies**
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.3</version>
</dependency>
<!-- Later in the file -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.10.2</version>
</dependency>
```

🔴 **Outdated Dependencies**
- JUnit 3.8.1 (current version is 5.x)
- No Java version specified in pom.xml

## Recommendations

### Immediate Fixes (High Priority)

1. **Clean up temporary directories**
```java
try {
    // ... existing code ...
} finally {
    driver.quit();
    // Add this:
    Files.walk(userDataDir)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
}
```

2. **Make file path configurable**
```java
String filePath = System.getProperty("user.home") + 
    File.separator + "Downloads" + 
    File.separator + "courses.ics";
```

3. **Fix dependency conflicts in pom.xml**
- Remove duplicate JSoup entry
- Update JUnit to version 5.x
- Add Maven compiler plugin with Java version

### Medium-Term Improvements

1. **Add Configuration Management**
Create `src/main/resources/application.properties`:
```properties
semester.start=2025-01-21
semester.end=2025-05-09
output.directory=${user.home}/Downloads
trinity.schedule.url=https://trincollself.service-now.com/...
```

2. **Implement Proper Logging**
- Add SLF4J with Logback
- Replace all `System.out.println` calls
- Add meaningful log levels

3. **Improve Error Handling**
- Use specific exception types
- Add retry logic for network operations
- Provide user-friendly error messages

4. **Add Input Validation**
```java
public Course(String name, String link, ...) {
    this.name = Objects.requireNonNull(name, "Course name cannot be null");
    this.link = validateUrl(link);
    // ... etc
}
```

### Long-Term Enhancements

1. **Add Comprehensive Testing**
- Unit tests for time parsing logic
- Integration tests with mock WebDriver
- Test edge cases and error scenarios

2. **Improve Architecture**
- Create interfaces for Scraper and Generator
- Implement dependency injection
- Add service layer for business logic

3. **Enhanced Features**
- Support multiple semesters
- Add CLI arguments for configuration
- Support for different calendar formats
- Direct calendar integration (not just file export)

## Conclusion

This project successfully accomplishes its core objective of scraping Trinity College schedules and generating calendar files. The code is functional and shows good understanding of web scraping and calendar formats.

However, before considering it production-ready, it needs attention to:
- Security (especially cleanup of temporary files)
- Error handling and logging
- Configuration management
- Cross-platform compatibility
- Testing

The recent removal of GUI functionality in favor of browser-based authentication was a positive architectural decision. With the recommended improvements, this could become a robust and maintainable tool for the Trinity College community.

## Quick Start Improvements

For immediate impact, focus on:
1. Fix the hardcoded file path
2. Clean up temporary Chrome profiles
3. Fix duplicate dependencies in pom.xml
4. Add basic error messages instead of stack traces

These changes would significantly improve security and usability with minimal effort.

## Additional Recommendations: Java Modernization & Testing

### 1. Upgrade to Java 21

The project would benefit significantly from modern Java features. Update the `pom.xml`:

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>21</release>
                <enablePreview>false</enablePreview>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. Convert Course to a Record

The 49-line `Course` class can become a single line:

```java
public record Course(
    String courseName,
    String courseLink,
    String instructor,
    String type,
    String days,
    String times,
    String location
) {
    // Add validation in compact constructor if needed
    public Course {
        Objects.requireNonNull(courseName, "Course name cannot be null");
        Objects.requireNonNull(courseLink, "Course link cannot be null");
    }
}
```

### 3. Use Text Blocks for ICS Generation

Replace string concatenation with formatted text blocks:

```java
private static final String ICS_HEADER = """
    BEGIN:VCALENDAR
    VERSION:2.0
    CALSCALE:GREGORIAN
    METHOD:PUBLISH
    PRODID:-//Trinity College//EN
    """;

private static final String EVENT_TEMPLATE = """
    BEGIN:VEVENT
    SUMMARY:%s
    DESCRIPTION:%s
    LOCATION:%s
    UID:%s@trincoll.edu
    DTSTART;TZID=America/New_York:%s
    DTEND;TZID=America/New_York:%s
    %s
    SEQUENCE:0
    STATUS:CONFIRMED
    TRANSP:OPAQUE
    BEGIN:VALARM
    TRIGGER:-PT15M
    DESCRIPTION:Reminder
    ACTION:DISPLAY
    END:VALARM
    END:VEVENT
    """;

// Usage:
String event = EVENT_TEMPLATE.formatted(
    course.courseName(),
    course.courseLink(),
    course.location(),
    uid,
    startDateTime,
    endDateTime,
    rrule
);
```

### 4. Functional Programming Improvements

#### In IcsGenerator:

```java
// Replace the day mapping loop with streams
private static String expandDays(String days, Map<String, String> dayMap) {
    return days.chars()
        .mapToObj(c -> String.valueOf((char) c))
        .map(dayMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(","));
}

// Replace the course processing loop
private static String generateEvents(List<Course> courses) {
    return courses.stream()
        .filter(course -> isValidSchedule(course))
        .map(course -> generateEvent(course))
        .collect(Collectors.joining("\n"));
}

private static boolean isValidSchedule(Course course) {
    return course.days() != null && 
           !course.days().trim().isEmpty() && 
           !"TBA".equalsIgnoreCase(course.days()) &&
           course.times() != null && 
           !course.times().trim().isEmpty() && 
           !"TBA".equalsIgnoreCase(course.times());
}
```

#### In Scraper:

```java
// Use Optional for instructor email
private static String extractInstructor(WebElement row) {
    return row.findElements(By.cssSelector("a[href^=mailto]"))
        .stream()
        .findFirst()
        .map(WebElement::getText)
        .orElse("N/A");
}

// Combine console output into a single formatted string
private static void logCourse(Course course) {
    String courseInfo = """
        Course Name: %s
        Course Link: %s
        Instructor: %s
        Type: %s
        Days: %s
        Times: %s
        Location: %s
        ---------------------------
        """.formatted(
            course.courseName(),
            course.courseLink(),
            course.instructor(),
            course.type(),
            course.days(),
            course.times(),
            course.location()
        );
    System.out.println(courseInfo);
}
```

### 5. Additional Modern Java Features

#### Switch Expressions for Time Parsing:

```java
private static String parseTimeComponent(String component) {
    return switch (component.toLowerCase()) {
        case "tba", "tbd" -> "";
        case String s when s.contains(":") -> convertTo24Hour(s);
        default -> "0900"; // Default time
    };
}
```

#### Use java.time API Instead of SimpleDateFormat:

```java
private static String convertTo24HourFormat(String time12Hour) {
    try {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("h:mma", Locale.US);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HHmm");
        LocalTime time = LocalTime.parse(time12Hour.toUpperCase(), inputFormatter);
        return time.format(outputFormatter);
    } catch (DateTimeParseException e) {
        logger.error("Failed to parse time: {}", time12Hour, e);
        return "0900";
    }
}
```

#### Pattern Matching (Java 21):

```java
private static String extractValue(Object obj) {
    return switch (obj) {
        case String s when !s.isBlank() -> s.trim();
        case String s -> "N/A";
        case null -> "N/A";
        default -> obj.toString();
    };
}
```

#### Try-with-resources Enhancement:

```java
private static void cleanupTempDirectory(Path tempDir) {
    try (var paths = Files.walk(tempDir)) {
        paths.sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    } catch (IOException e) {
        logger.error("Failed to cleanup temp directory", e);
    }
}
```

### 6. Comprehensive Testing Strategy

#### Unit Tests:

```java
// CourseTest.java
@Test
void testCourseRecordValidation() {
    assertThrows(NullPointerException.class, 
        () -> new Course(null, "link", "prof", "LEC", "MWF", "9:00am-10:15am", "Room 101"));
}

@Test
void testCourseEquality() {
    var course1 = new Course("CS101", "link", "prof", "LEC", "MWF", "9:00am-10:15am", "Room 101");
    var course2 = new Course("CS101", "link", "prof", "LEC", "MWF", "9:00am-10:15am", "Room 101");
    assertEquals(course1, course2);
}

// IcsGeneratorTest.java
@ParameterizedTest
@CsvSource({
    "9:00am, 0900",
    "2:30pm, 1430",
    "12:00pm, 1200",
    "12:30am, 0030"
})
void testTimeConversion(String input, String expected) {
    assertEquals(expected, IcsGenerator.convertTo24HourFormat(input));
}

@Test
void testDayExpansion() {
    Map<String, String> dayMap = Map.of("M", "MO", "W", "WE", "F", "FR");
    assertEquals("MO,WE,FR", IcsGenerator.expandDays("MWF", dayMap));
}

@Test
void testIcsEventGeneration() {
    var course = new Course("CS101", "http://link", "Dr. Smith", "LEC", "MWF", "9:00am-10:15am", "Room 101");
    String ics = IcsGenerator.generateEvent(course);
    
    assertAll(
        () -> assertTrue(ics.contains("SUMMARY:CS101")),
        () -> assertTrue(ics.contains("LOCATION:Room 101")),
        () -> assertTrue(ics.contains("RRULE:FREQ=WEEKLY")),
        () -> assertTrue(ics.contains("BYDAY=MO,WE,FR"))
    );
}

// ScraperTest.java
@Test
void testDaysAndTimesExtraction() {
    String[] result = Scraper.extractDaysAndTimes("MWF: 9:00am - 10:15am");
    assertArrayEquals(new String[]{"MWF", "9:00am-10:15am"}, result);
}

@Test
void testTbaHandling() {
    String[] result = Scraper.extractDaysAndTimes("TBA");
    assertArrayEquals(new String[]{"", ""}, result);
}
```

#### Integration Tests:

```java
@TestConfiguration
public class MockWebDriverConfig {
    @Bean
    @Primary
    public WebDriver mockWebDriver() {
        WebDriver mockDriver = Mockito.mock(WebDriver.class);
        // Configure mock behavior
        return mockDriver;
    }
}

@SpringBootTest
class ScraperIntegrationTest {
    @Test
    void testFullScrapingWorkflow() {
        // Test with mock HTML response
    }
}
```

#### Property-Based Testing with jqwik:

```java
@Property
void allGeneratedIcsFilesAreValid(@ForAll @StringLength(min = 1, max = 50) String courseName,
                                 @ForAll("validDays") String days,
                                 @ForAll("validTimes") String times) {
    var course = new Course(courseName, "http://link", "Prof", "LEC", days, times, "Room");
    String ics = IcsGenerator.generateEvent(course);
    
    // Validate ICS format
    assertTrue(ics.startsWith("BEGIN:VEVENT"));
    assertTrue(ics.endsWith("END:VEVENT"));
    assertTrue(ics.contains("UID:"));
}

@Provide
Arbitrary<String> validDays() {
    return Arbitraries.of("M", "T", "W", "R", "F", "MW", "TR", "MWF", "MTWR");
}
```

### 7. Additional Improvements

#### Dependency Injection with Spring Boot:

```java
@SpringBootApplication
public class TrinityCalendarApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrinityCalendarApplication.class, args);
    }
}

@Service
public class CalendarService {
    private final Scraper scraper;
    private final IcsGenerator generator;
    
    @Autowired
    public CalendarService(Scraper scraper, IcsGenerator generator) {
        this.scraper = scraper;
        this.generator = generator;
    }
}
```

#### Configuration with @ConfigurationProperties:

```java
@ConfigurationProperties(prefix = "trinity.calendar")
@Validated
public class CalendarProperties {
    @NotNull
    private LocalDate semesterStart;
    
    @NotNull
    private LocalDate semesterEnd;
    
    @NotBlank
    private String scheduleUrl;
    
    private Path outputDirectory = Paths.get(System.getProperty("user.home"), "Downloads");
    
    // getters and setters
}
```

#### Sealed Classes for Course Types:

```java
public sealed interface CourseComponent 
    permits Lecture, Lab, Discussion, Seminar {
    
    String type();
    String schedule();
}

public record Lecture(String schedule) implements CourseComponent {
    public String type() { return "LEC"; }
}

public record Lab(String schedule) implements CourseComponent {
    public String type() { return "LAB"; }
}
```

These modernizations would transform the codebase into a more maintainable, testable, and idiomatic Java 21 application while maintaining backward compatibility where needed.