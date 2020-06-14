package org.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SearchTest {

    @Autowired
    private ApplicationContext context;
    private WebDriver driver;
    private GoogleMockServer mockServer;

    @Before
    public void setUp() {
        System.setProperty("webdriver.gecko.driver", Objects.requireNonNull(getClass().getClassLoader().getResource("geckodriver.exe")).getFile());
        driver = new FirefoxDriver();    //Firefox !!!
        driver.get("http://localhost:8080");
        mockServer = context.getBean(GoogleMockServer.class);
    }

    @After
    public void tearDown() {
        driver.quit();
        mockServer.stop();
    }

    @Test
    public void searchOneWord() {
        driver.findElement(By.id("keywords")).clear();
        driver.findElement(By.id("keywords")).sendKeys("java");
        driver.findElement(By.id("bth-search")).click();
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
        String tableStr = driver.findElement(By.tagName("table")).getText();
        Assert.assertEquals("Domain Count\n" +
                "en.wikipedia.org 1\n" +
                "java.com 1\n" +
                "docs.oracle.com 1\n" +
                "oracle.com 1\n" +
                "w3schools.com 1", tableStr);
    }

    @Test
    public void searchTwoWords() {
        driver.findElement(By.id("keywords")).clear();
        driver.findElement(By.id("keywords")).sendKeys("java", " ", "oracle");
        driver.findElement(By.id("bth-search")).click();
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
        String tableStr = driver.findElement(By.tagName("table")).getText();
        Assert.assertEquals("Domain Count\n" +
                "twitter.com 1\n" +
                "en.wikipedia.org 2\n" +
                "virtualbox.org 1\n" +
                "java.com 1\n" +
                "academy.oracle.com 1\n" +
                "netsuite.com 1\n" +
                "docs.oracle.com 2\n" +
                "oracle.com 2\n" +
                "linkedin.com 1\n" +
                "w3schools.com 1\n" +
                "education.oracle.com 1", tableStr);
    }
}
