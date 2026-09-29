FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y chromium chromium-driver && \
    rm -rf /var/lib/apt/lists/*

# Selenium'un Chromium ve ChromeDriver'ı bulabilmesi için gerekli ortam değişkenleri.
ENV CHROME_BIN=/usr/bin/chromium
ENV webdriver.chrome.driver=/usr/lib/chromium-browser/chromedriver

WORKDIR /app

# GitHub'da pushladığınız target klasöründeki jar dosyasını kopyalıyoruz.
COPY target/scraper-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
