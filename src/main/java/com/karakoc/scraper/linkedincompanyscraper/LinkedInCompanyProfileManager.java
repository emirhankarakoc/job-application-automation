package com.karakoc.scraper.linkedincompanyscraper;

import lombok.AllArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LinkedInCompanyProfileManager implements LinkedInCompanyProfileService {

    private final LinkedInCompanyProfileResultRepository companyProfileResultRepository;

    @Override
    public LinkedInCompanyProfileResult scrapeCompanyProfile(String companyProfileUrl) {
        long startTime = System.currentTimeMillis();

        // 1) ChromeDriver ayarları
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless"); // GUI olmadan çalıştır
        options.addArguments("--no-sandbox"); // Sandbox'ı devre dışı bırak
        options.addArguments("--disable-dev-shm-usage"); // Bellek ile ilgili sorunları önle
        options.addArguments("--remote-allow-origins=*"); // Remote bağlantı sorunlarını çöz

        WebDriver driver = new ChromeDriver(options);

        // (2) Modal kapatma vb. için WebDriverWait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
//            System.out.println("[LinkedInCompanyProfileManager] Profil Taranıyor: " + companyProfileUrl);
            driver.get(companyProfileUrl);

            // Kısa sabit bekleme
            Thread.sleep(2000);

            // ---------------------------------------------------------------
            // (A) Giriş Yap Modalini Kapatma (Varsa)
            // ---------------------------------------------------------------
            try {
                // Çeşitli class'lar: "button.modal__dismiss" veya "button.contextual-sign-in-modal__modal-dismiss"
                WebElement closeModalButton = driver.findElement(
                        By.cssSelector("button.modal__dismiss, button.contextual-sign-in-modal__modal-dismiss")
                );

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeModalButton);
//                System.out.println("[!] Giriş yap modalı kapatma butonuna tıklandı.");

                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div.modal__overlay.modal__overlay--visible")
                ));
//                System.out.println("[!] Overlay yok oldu, sayfa etkileşime hazır.");

                Thread.sleep(1000);

            } catch (NoSuchElementException | TimeoutException e) {
//                System.out.println("[!] Giriş yap modalı bulunamadı veya kapatma gerekmiyor: " + e.getMessage());
            }

            // ---------------------------------------------------------------
            // (B) Website bilgisini bulma
            // ---------------------------------------------------------------
            // Paylaştığın HTML'de "div[data-test-id='about-us__website'] dd a" içinde site linki var
            // Metin olarak "http://www.eteaminc.com" görünüyor
            String websiteText = "YOK";
            try {
                // data-test-id="about-us__website" => hem dt hem dd a var
                WebElement websiteAnchor = driver.findElement(
                        By.cssSelector("div[data-test-id='about-us__website'] dd a")
                );
                // Anchor içindeki görünen metin => "http://www.eteaminc.com"
                websiteText = websiteAnchor.getText().trim();

                // İstersen href'i decode ederek de alabilirsin:
                // String redirectHref = websiteAnchor.getAttribute("href");
                // => "https://www.linkedin.com/redir/redirect?url=http%3A%2F%2Fwww%2Eeteaminc%2Ecom..."
                // decode edilerek: "http://www.eteaminc.com"

                if (websiteText.isEmpty()) {
                    websiteText = "YOK";
                }
            } catch (NoSuchElementException noWebsiteEx) {
//                System.out.println("[!] Website alanı bulunamadı: " + noWebsiteEx.getMessage());
            }

            // ---------------------------------------------------------------
            // (C) Sonuç Nesnesi
            // ---------------------------------------------------------------
            LinkedInCompanyProfileResult result = new LinkedInCompanyProfileResult();
            result.setId(UUID.randomUUID().toString());
            result.setCompanyProfileUrl(companyProfileUrl);
            result.setWebsite(websiteText);

            long endTime = System.currentTimeMillis();
            result.setTimeSpentMs(endTime - startTime);

            // DB kaydı
            companyProfileResultRepository.save(result);

            //System.out.println("[LinkedInCompanyProfileManager] Profil tarama tamamlandı. Website: "
              //      + websiteText + " | Süre(ms): " + (endTime - startTime));

            return result;

        } catch (Exception e) {
            System.err.println("[LinkedInCompanyProfileManager] Hata: " + e.getMessage());
            throw new RuntimeException(e);

        } finally {
            driver.quit();
        }
    }
}
