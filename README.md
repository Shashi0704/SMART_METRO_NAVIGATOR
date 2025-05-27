# Smart Metro Project

This is a Smart Metro System project that helps manage and track metro operations.

## Project Structure
```
smart-metro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── smartmetro/
│   │   │           ├── Metro.java
│   │   │           └── MetroService.java
│   │   ├── resources/
│   │   │   ├── arrival_times.txt
│   │   │   ├── colorcodes.txt
│   │   │   ├── matrix.txt
│   │   │   ├── node_values_new.txt
│   │   │   ├── station.txt
│   │   │   └── status_updates.txt
│   │   └── webapp/
│   │       ├── resources/
│   │       │   ├── images/
│   │       │   ├── css/
│   │       │   └── js/
│   │       ├── WEB-INF/
│   │       │   └── views/
│   │       ├── index.html
│   │       ├── path.html
│   │       ├── styles1.css
│   │       └── script.js
│   └── test/
│       ├── java/
│       └── resources/
├── pom.xml
└── README.md
```

## Setup Instructions

1. Open IntelliJ IDEA
2. Click on "File" -> "Open"
3. Navigate to the project directory and select it
4. IntelliJ IDEA will automatically recognize it as a Maven project
5. Wait for Maven to download all dependencies
6. Build the project using Maven: Right-click on the project -> "Maven" -> "Build"

## Running the Application

1. Configure a Tomcat server in IntelliJ IDEA
2. Deploy the application to the server
3. Access the application through your web browser at `http://localhost:8080/smart-metro`

## Development

- Java source files are located in `src/main/java/com/smartmetro/`
- Web resources (HTML, CSS, JS) are in `src/main/webapp/`
- Configuration files and resources are in `src/main/resources/` 
