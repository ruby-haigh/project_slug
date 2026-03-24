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

public class LogoutTest {

    WebDriver driver;
    Faker faker;

    @BeforeEach
    public void login() {
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        faker = new Faker();
        String email = faker.name().username() + "@email.com";
        driver.get("http://127.0.0.1:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
    }

    @Test
    public void testLogout() {
        driver.findElement(By.id("profile-pic-dropdown")).click();
        driver.findElement(By.linkText("Logout")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement body = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))
        );

        assertTrue(body.getText().contains("Log in to continue to your newsfeed"));
    }
}
