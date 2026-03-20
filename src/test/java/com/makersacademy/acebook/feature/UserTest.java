package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    WebDriver driver;
    Faker faker;

    @BeforeEach
    public void login() {
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        faker = new Faker();
        String email = faker.name().username() + "@email.com";
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();
//        Navigate to account page
        driver.findElement(By.id("profile-pic-dropdown")).click();
        driver.findElement(By.linkText("My Account")).click();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
    }

    @Test
    public void updateAccountDetails() {
        driver.findElement(By.linkText("Edit Profile")).click();
        driver.findElement(By.name("name")).sendKeys("example-name");
        driver.findElement(By.name("bio")).sendKeys("example-bio");
        driver.findElement(By.className("btn-primary")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement body = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))
        );

        assertTrue(body.getText().contains("example-name"));
        assertTrue(body.getText().contains("example-bio"));
    }

}
