package org.example.spring.test.htmlUnit.pages;

import org.openqa.selenium.WebDriver;

/**
 * Represents the common elements in a page within the message application.
 *
 * @author Rob Winch
 *
 */
public class AbstractPage {
	protected WebDriver driver;


	public AbstractPage(WebDriver driver) {
		setDriver(driver);
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}


    public static void get(WebDriver driver, String relativeUrl) {
        String url = System.getProperty("geb.build.baseUrl","http://localhost") + relativeUrl;
        driver.get(url);
    }
}