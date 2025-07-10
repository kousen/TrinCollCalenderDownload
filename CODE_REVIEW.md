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