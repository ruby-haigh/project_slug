package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class GroupTest {

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
    }

    @AfterEach
    public void teardown() {
        driver.close();
    }

    @Test
    public void createNewGroup() {
        driver.findElement(By.linkText("Create Circle")).click();
        driver.findElement(By.name("name")).sendKeys("example-group");

        WebElement newGroupButton = driver.findElement(By.className("scrapbook-button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", newGroupButton);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement createdGroup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("scrapbook-card-title"))
        );
        String groupName = createdGroup.getText();

        assertEquals("example-group", groupName);
    }
}
