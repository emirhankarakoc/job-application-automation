package com.karakoc.scraper.websitescraper;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class WebsiteWebsiteScraperManager implements WebsiteScraperService {

    @Autowired
    private WebsiteScrapeResultRepository websiteScrapeResultRepository;

    // E-posta yakalamak için temel bir regex
    private static final Pattern EMAIL_REGEX = Pattern.compile("[\\w.\\-]+@[\\w.\\-]+\\.\\w+");

    /**
     * Controller'dan tetiklenecek metot.
     *
     * @param startUrl  Başlangıç URL
     * @param maxPages  En fazla kaç sayfa taranacak
     * @return          DB'ye kaydedilen WebsiteScrapeResult kaydı
     */
    public WebsiteScrapeResult scrape(String startUrl, int maxPages) {
        // Zaman ölçümünü başlat
        long startTime = System.currentTimeMillis();

        // BFS için kuyruk ve visited set/list
        Queue<String> queue = new LinkedList<>();
        queue.offer(startUrl);

        Set<String> visitedSet = new HashSet<>();
        List<String> visitedList = new ArrayList<>(); // Sıralı tutmak için

        // E-postaları saklayacağımız set
        Set<String> foundEmails = new HashSet<>();


        // 1) ChromeDriver ayarları
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless"); // GUI olmadan çalıştır
        options.addArguments("--no-sandbox"); // Sandbox'ı devre dışı bırak
        options.addArguments("--disable-dev-shm-usage"); // Bellek ile ilgili sorunları önle
        options.addArguments("--remote-allow-origins=*"); // Remote bağlantı sorunlarını çöz



        WebDriver driver = new ChromeDriver(options);

        int pageCount = 0;

        try {
            while (!queue.isEmpty() && pageCount < maxPages) {
                String currentUrl = queue.poll();
                if (currentUrl == null) continue;
                if (visitedSet.contains(currentUrl)) continue;

                visitedSet.add(currentUrl);
                visitedList.add(currentUrl);
                pageCount++;

                System.out.println("[" + pageCount + "] Taranıyor: " + currentUrl);

                // Sayfayı açalım
                try {
                    driver.get(completeUrl(currentUrl));
                    // "www.xxx.com" gibi eksik protokol gelirse tamamla

                    // Sayfanın HTML kaynağını al
                    String pageSource = driver.getPageSource();

                    // 1) E-posta bulma
                    Matcher matcher = EMAIL_REGEX.matcher(pageSource);
                    while (matcher.find()) {
                        String rawEmail = matcher.group();
                        String normalized = unmaskEmail(rawEmail);
                        if (isProbablyValidEmail(normalized)) {
                            String lowerNormalized = normalized.toLowerCase();
                            if (!(lowerNormalized.endsWith(".png") || lowerNormalized.endsWith(".jpg") || lowerNormalized.endsWith(".jpeg") || lowerNormalized.endsWith(".webp") || lowerNormalized.endsWith(".svg") ||
                                    lowerNormalized.endsWith("wixpress.com") ||
                                    lowerNormalized.endsWith("example.com") ||
                                    lowerNormalized.endsWith("doe.com") ||
                                    lowerNormalized.endsWith(".gif") || lowerNormalized.endsWith(".mp3") || lowerNormalized.endsWith(".mp4"))) {
                                foundEmails.add(normalized);
                            }
                        }
                    }


                    // 2) Link bulma
                    String currentDomain = getDomainWithoutWWW(currentUrl);
                    List<WebElement> anchors = driver.findElements(By.tagName("a"));
                    for (WebElement a : anchors) {
                        String href = a.getAttribute("href");
                        if (href == null || href.trim().isEmpty()) {
                            continue;
                        }
                        try {
                            URL absoluteUrl = new URL(new URL(completeUrl(currentUrl)), href);
                            String link = absoluteUrl.toString();

                            // Aynı domain mi?
                            String linkDomain = getDomainWithoutWWW(link);
                            if (currentDomain != null && currentDomain.equals(linkDomain)) {
                                if (!visitedSet.contains(link)) {
                                    queue.offer(link);
                                }
                            }
                        } catch (Exception e) {
                            // geçersiz link
                        }
                    }

                } catch (Exception e) {
                    System.err.println("[!] " + currentUrl + " sayfasında hata: " + e.getMessage());
                }
            }
        } finally {
            driver.quit();
        }

        // Zaman ölçümünü bitir
        long endTime = System.currentTimeMillis();
        long timeSpent = endTime - startTime;

        // Çıkan sonuçları DB'ye kaydet
        WebsiteScrapeResult result = new WebsiteScrapeResult();
        result.setId(UUID.randomUUID().toString());
        result.setStartUrl(startUrl);
        result.setPageCount(pageCount);
        result.setTimeSpentMs(timeSpent);
        result.setVisitedUrls(visitedList);
        result.setFoundEmails(foundEmails);

        // JPA ile kaydet
        websiteScrapeResultRepository.save(result);

        //System.out.println("Tarama bitti. Toplam sayfa: " + pageCount
            //    + ", Süre(ms): " + timeSpent);
        System.out.println("website taramasi basarili.");
        // Service metodundan en son bu entity'yi döndürelim
        return result;
    }

    /**
     * Sadece "www.xxx.com" şeklinde gelirse otomatik "https://" ekleyelim.
     */
    private String completeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    /**
     * @return  "www." kısmı atılmış domain.
     */
    private String getDomainWithoutWWW(String urlString) {
        try {
            if (!urlString.startsWith("http")) {
                urlString = "https://" + urlString;
            }
            URL url = new URL(urlString);
            String host = url.getHost().toLowerCase();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * [at], [dot] gibi basit maskeleri normale çeviren fonksiyon.
     */
    private String unmaskEmail(String str) {
        return str
                .replaceAll("(?i)\\[at\\]", "@")
                .replaceAll("(?i)\\(at\\)", "@")
                .replaceAll("(?i)\\s+at\\s+", "@")
                .replaceAll("(?i)\\[dot\\]", ".")
                .replaceAll("(?i)\\(dot\\)", ".")
                .replaceAll("(?i)\\s+dot\\s+", ".");
    }

    /**
     * core-js-bundle@3.2.1 gibi “sözde” e-postaları ayıklamak için
     * TLD (son kısım) sadece harflerden oluşmalı ve en az 2 karakter olmalı.
     */
    private boolean isProbablyValidEmail(String email) {
        if (!email.contains("@")) return false;
        String[] parts = email.split("@");
        if (parts.length != 2) return false;
        String domainPart = parts[1];  // "example.com"
        String[] domainSections = domainPart.split("\\.");
        if (domainSections.length < 2) {
            return false; // . yoksa veya tek parça
        }
        String tld = domainSections[domainSections.length - 1];
        // tld en az 2 karakter => com, net, org vs. Sadece harf => [a-zA-Z]+
        if (tld.length() < 2) return false;
        if (!tld.matches("[a-zA-Z]+")) return false;
        return true;
    }
}


