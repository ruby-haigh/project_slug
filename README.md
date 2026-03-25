# 🐌 Snail Mail

⚙️ See our live app [here](https://project-slug.onrender.com)

The application uses:
  - `maven` to build the project
  - `thymeleaf` for templating
  - `flyway` to manage `postgres` db migrations
  - `selenium` for feature testing
  - `faker` to generate fake names for testing
  - `junit4` for unit testing
  - `auth0` and `spring-security` for authentication and user management
  - `lombok` to generate getters and setters
  - `cloudinary` to enable image upload
  - `spotify` to enable song upload
  - `spring email` to enable automatic email notification

## About Our App

Snail Mail was made for the modern, busy person who struggles to stay connected with those who matter. Whether it's friends, family, colleagues, or otherwise - we all want to stay in touch but this can feel difficult with our busy lifestyles or overwhelming in today's increasingly digital climate.

Enter Snail Mail: a slower, more intentional form of social media which allows you to connect with those who mean the most when you have the time. 

Once you sign up, you can create circles and invite others to join. Monthly or fortnightly, each member receives a list of prompts designed to encourage meaningful connection and has a week to respond to them. Once each response window closes, everyone's responses will be collated into a feed for you to view. From there, you can react or reach out to people via email or WhatsApp directly through the app if you wish.
  
## QuickStart Instructions

- Click "Use this template" to create a copy of this repo on your GitHub account.
- Open the codebase in an IDE like InteliJ or VSCode
  - If using IntelliJ, accept the prompt to install the Lombok plugin (if you don't get prompted, press command and comma
  to open the Settings and go to Plugins and search for Lombok made by Jetbrains and install).
- Create two new Postgres databases called `slug_development` and `slug_test`
- Install Maven `brew install maven`
- [Set up Auth0](https://journey.makers.tech/pages/auth0) (you only need the "Create an Auth0 app" section)
- Build the app and start the server, using the Maven command `mvn spring-boot:run`
> The database migrations will run automatically at this point
- Visit `http://127.0.0.1:8080/` to sign up

## Running the tests

- Install chromedriver using `brew install chromedriver`
- Start the server in a terminal session `mvn spring-boot:run -Dspring-boot.run.profiles=test`
- Open a new terminal session and navigate to the project_slug directory
- Run your tests in the second terminal session with `mvn test`

> All the tests should pass. If one or more fail, read the next section.

## Common Setup Issues

### The application is not running

For the feature tests to execute properly, you'll need to have the server running in one terminal session and then use a second terminal session to run the tests.

### Chromedriver is in the wrong place

Selenium uses Chromedriver to interact with the Chrome browser. If you're on a Mac, Chromedriver needs to be in `/usr/local/bin`. You can find out where it is like this `which chromedriver`. If it's in the wrong place, move it using `mv`.

### Chromedriver can't be opened

Your Mac might refuse to open Chromedriver because it's from an unidentified developer. If you see a popup at that point, dismiss it by selecting `Cancel`, then go to `System Preferences`, `Security and Privacy`, `General`. You should see a message telling you that Chromedriver was blocked and, if so, there will be an `Open Anyway` button. Click that and then re-try your tests.

