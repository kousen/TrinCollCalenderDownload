# Trinity College Calendar Download - Project Context

## Overview
This is a Java-based web scraper that extracts course schedules from Trinity College's website and generates ICS calendar files. The project uses Selenium WebDriver for browser automation and handles Microsoft authentication.

## Key Technical Details

### Architecture
- **Language**: Java (currently targets Java 8, but should be upgraded to Java 21)
- **Build Tool**: Maven
- **Main Components**:
  - `Course.java`: Data model (49-line POJO that could be a record)
  - `Scraper.java`: Selenium-based web scraping
  - `IcsGenerator.java`: ICS file generation and main entry point

### Dependencies
- Selenium WebDriver 4.27.0
- JSoup 1.18.3 (duplicate entry with 1.10.2 - needs fixing)
- JUnit 3.8.1 (severely outdated - should be 5.x)

## Critical Issues to Address

### Security
1. **Temporary Chrome profiles are never cleaned up** - leaves session data on disk
2. **Hardcoded file path** exposes developer username: `C:\Users\nokon\Downloads\courses.ics`
3. **No input validation** on scraped data

### Code Quality
1. **Generic exception handling** - catches all exceptions, prints stack traces
2. **Hardcoded semester dates** (Jan 21 - May 9, 2025)
3. **Platform-specific** Windows file paths
4. **No tests** despite JUnit dependency

## Quick Fixes Needed
```java
// 1. Clean up temp directories
Files.walk(userDataDir)
    .sorted(Comparator.reverseOrder())
    .map(Path::toFile)
    .forEach(File::delete);

// 2. Cross-platform file path
String filePath = System.getProperty("user.home") + 
    File.separator + "Downloads" + 
    File.separator + "courses.ics";

// 3. Fix pom.xml - remove duplicate JSoup, update JUnit to 5.x
```

## Java Modernization Opportunities

### Convert Course to Record (Java 14+)
```java
public record Course(
    String courseName,
    String courseLink,
    String instructor,
    String type,
    String days,
    String times,
    String location
) {}
```

### Use Text Blocks (Java 15+)
```java
private static final String EVENT_TEMPLATE = """
    BEGIN:VEVENT
    SUMMARY:%s
    LOCATION:%s
    UID:%s@trincoll.edu
    DTSTART;TZID=America/New_York:%s
    DTEND;TZID=America/New_York:%s
    %s
    END:VEVENT
    """;
```

### Functional Programming
- Replace loops with streams
- Use Optional for nullable values
- Method references for cleaner code

## Testing Commands
When implementing improvements, use these commands:
```bash
# Compile
mvn clean compile

# Run tests (after adding them)
mvn test

# Package
mvn package

# Run the application
java -cp target/classes edu.trincoll.IcsGenerator
```

## Project Workflow
1. User runs `IcsGenerator.main()`
2. Chrome opens to Trinity's schedule page
3. User manually logs in (Microsoft auth)
4. Scraper extracts course data
5. ICS file generated in Downloads folder

## Important URLs
- Trinity Schedule: `https://internet3.trincoll.edu/spTools/ClassSchedule.aspx`
- Project uses browser automation, so URL may change based on redirects

## Development Tips
- The project removed GUI functionality in favor of browser-based auth (good decision)
- Selenium waits up to 120 seconds for login
- ICS files use America/New_York timezone
- Course data parsing handles "TBA" schedules by skipping them

## Code Review Available
See [CODE_REVIEW.md](CODE_REVIEW.md) for comprehensive analysis including:
- Security vulnerabilities
- Java 21 modernization
- Testing strategies
- Architecture improvements

## Future Documentation & Project Management Plans

### 1. Add LICENSE File
- Currently states "proprietary" in README
- Should have proper LICENSE file
- Discuss licensing intentions with student first

### 2. Create CONTRIBUTING.md
If accepting contributions, include:
- Code style guidelines
- How to report issues
- Pull request process
- Development setup instructions

### 3. GitHub Issue Templates
Create `.github/ISSUE_TEMPLATE/` directory with:
- `bug_report.md` - For reporting bugs
- `feature_request.md` - For suggesting features
- `security_vulnerability.md` - For security issues

### 4. Add CHANGELOG.md
Track version history:
- Document GUI removal
- Track future updates
- Follow Keep a Changelog format

### 5. Enhanced Documentation
- Add example ICS file output in README
- Include screenshots of:
  - Login flow
  - Generated calendar import
- Add troubleshooting section for common issues:
  - ChromeDriver version mismatches
  - Authentication timeouts
  - File permission errors

### 6. GitHub Repository Metadata
- Add repository topics: `trinity-college`, `calendar`, `web-scraper`, `selenium`, `java`
- Update repository description to be more descriptive
- Add website field pointing to Trinity College

### 7. Additional Documentation Ideas
- Create `docs/` folder with:
  - Detailed setup guide
  - Architecture diagrams
  - API documentation (if refactored)
- Add badges to README (build status, license, etc.)
- Create GitHub Wiki for extended documentation