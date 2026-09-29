package com.karakoc.scraper.linkedinjobpostingscraper;

import lombok.AllArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LinkedInJobPostingManager implements LinkedInJobPostingService {

    private final LinkedinJobPostingRepository linkedInJobPostingResultRepository;

    @Override
    public LinkedInJobPostingResult scrape(String jobUrl) throws IOException {
        long startTime = System.currentTimeMillis();

        // 1) ChromeDriver ayarları
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless"); // GUI olmadan çalıştır
        options.addArguments("--no-sandbox"); // Sandbox'ı devre dışı bırak
        options.addArguments("--disable-dev-shm-usage"); // Bellek ile ilgili sorunları önle
        options.addArguments("--remote-allow-origins=*"); // Remote bağlantı sorunlarını çöz


        WebDriver driver = new ChromeDriver(options);

        // WebDriverWait, en fazla 10 sn bekleyecek (gerekirse artır)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            System.out.println("[LinkedInJobPostingManager] İlan Taranıyor: " + jobUrl);
            driver.get(jobUrl);

            // 2) Sayfa yüklenmesini bekle (4 sn sabit + ek olarak Wait koşulları)
            Thread.sleep(4000);

            // ---------------------------------------------------------------
            // (A) "Giriş yap" modalini kapat (varsa)
            // ---------------------------------------------------------------
            // Senden gelen buton örneğinde class="modal__dismiss ... contextual-sign-in-modal__modal-dismiss ... aria-label='Kapat'"
            // Biz hem class hem attribute ile yakalamayı deneyelim:
            try {
                // 1) Kapatma butonunun görünmesini bekle (max 3 sn)
                WebElement closeModalButton = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.modal__dismiss, button.contextual-sign-in-modal__modal-dismiss")));

                closeModalButton.click();  // Normal click
//                System.out.println("[!] 'Giriş yap' modalı kapatma butonuna tıklandı.");

                // 2) Overlay'in yok olmasını bekle
                // modal__overlay--visible => invisible olana kadar bekle
                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div.modal__overlay.modal__overlay--visible")
                ));
//                System.out.println("[!] Overlay görünmez oldu, 'Show more' butonuna tıklayabiliriz.");

            } catch (TimeoutException | NoSuchElementException e) {
//                System.out.println("[!] 'Giriş yap' modalı veya kapatma butonu bulunamadı, devam ediyoruz: " + e.getMessage());
            }

            // ---------------------------------------------------------------
            // (B) "Show more" butonuna tıklayarak açıklamayı genişlet
            // ---------------------------------------------------------------
            try {
                // Show more butonu ekranda görünür mü?
                WebElement showMoreButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[class*='show-more']")));
                showMoreButton.click();
//                System.out.println("[+] 'Show more' butonu tıklandı, açıklama genişledi.");

            } catch (TimeoutException te) {
           //     System.out.println("[!] 'Show more' butonu clickable olmadı, kısa veya tam metin olabilir: " + te.getMessage());
            } catch (ElementClickInterceptedException ice) {
          //      System.out.println("[!] Yine overlay engelliyor gibi, JS ile tıklamayı deneyelim: " + ice.getMessage());
                // Aşağıda JS click örneği, overlay'i de kaldırabiliriz
            }

            // ---------------------------------------------------------------
            // (C) Rol başlığı
            // ---------------------------------------------------------------
            String roleTitle = "YOK";
            try {
                WebElement titleEl = driver.findElement(By.cssSelector("h1.top-card-layout__title"));
                roleTitle = titleEl.getText().trim();
            } catch (NoSuchElementException e) {
           //     System.out.println("[!] Rol başlığı bulunamadı: " + e.getMessage());
            }

            // ---------------------------------------------------------------
            // (D) Şirket profil linki
            // ---------------------------------------------------------------
            String companyProfileUrl = "YOK";
            try {
                WebElement companyLinkEl = driver.findElement(By.cssSelector("a.topcard__org-name-link"));
                String rawUrl = companyLinkEl.getAttribute("href"); // https://www.linkedin.com/company/eteam?trk=...

                if (rawUrl != null && !rawUrl.trim().isEmpty()) {
                    // 1) URL objesine parse ediyoruz
                    URL urlObj = new URL(rawUrl);

                    // 2) Sadece protokol + host + path
                    // query veya fragment kısmını eklemiyoruz
                    // URL'de: getProtocol() => https
                    //         getHost() => www.linkedin.com
                    //         getPath() => /company/eteam
                    companyProfileUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + urlObj.getPath();

                    // Örnek: https://www.linkedin.com/company/eteam
                }
            } catch (NoSuchElementException | MalformedURLException e) {
           //     System.out.println("[!] Şirket linki bulunamadı veya geçersiz: " + e.getMessage());
            }


            // ---------------------------------------------------------------
            // (E) İlan açıklaması (tam metin) al
            // ---------------------------------------------------------------
            String roleDescriptionHtml = "YOK";
            try {
                // 1) Önce "div.show-more-less-html__markup" dene
                WebElement descEl = driver.findElement(By.cssSelector("div.show-more-less-html__markup"));
                roleDescriptionHtml = descEl.getAttribute("innerHTML").trim();
            } catch (NoSuchElementException e) {
          //      System.out.println("[!] show-more-less-html__markup yok, 'description__text' deneyelim...");
                try {
                    WebElement descEl2 = driver.findElement(By.cssSelector("div.description__text"));
                    roleDescriptionHtml = descEl2.getAttribute("innerHTML").trim();
                } catch (NoSuchElementException e2) {
           //         System.out.println("[!] Açıklama alanı bulunamadı. 'YOK' kalacak.");
                }
            }

            // ---------------------------------------------------------------
            // (F) Sonuç nesnesi oluşturup DB'ye kaydet
            // ---------------------------------------------------------------
            LinkedInJobPostingResult result = new LinkedInJobPostingResult();
            result.setId(UUID.randomUUID().toString());
            result.setJobUrl(jobUrl);
            result.setRoleTitle(roleTitle);
            result.setCompanyProfileUrl(companyProfileUrl);
            result.setRoleDescription(roleDescriptionHtml);

            long endTime = System.currentTimeMillis();
            result.setTimeSpentMs(endTime - startTime);

            linkedInJobPostingResultRepository.save(result);

            System.out.println("[LinkedInJobPostingManager] Tarama tamam. " +
                    "Title: " + roleTitle + " | Desc length: " + roleDescriptionHtml.length());
            return result;

        } catch (Exception e) {
          //  System.err.println("[LinkedInJobPostingManager] Hata: " + e.getMessage());
            throw new RuntimeException(e);

        } finally {
            driver.quit();
        }
    }
}
