# Job Application Automation

This Java app takes a LinkedIn job post URL and helps send a job application by email.

## How it works

1. The app reads the job post and the company page with Selenium.
2. It opens up to 20 pages on the company website to find email addresses.
3. An AI API writes an email for that job post using the user's profile details.
4. The app sends that email to the found addresses, with the user's CV file attached. It also sends a copy to the user.
5. MySQL stores the request, job and company data, generated email, and send record.

`POST /queue?linkedinjobpostingUrl=...` adds a request. The same LinkedIn URL can be added only once. A scheduled task processes one pending request at a time and sets its status to `DONE` or `FAILED`. The queue runs in this Spring Boot app and is stored in MySQL.

## Tech

Java 17, Spring Boot, MySQL, JPA, Spring Security with JWT, Selenium, an AI API, and SMTP. The user's CV file is stored in Cloudflare R2 using the AWS S3 SDK. Email is sent with Spring's `JavaMailSender` and configured SMTP credentials.

## Code

- Queue and worker: `src/main/java/com/karakoc/scraper/prodbykarakoc/`
- Job and company readers: `src/main/java/com/karakoc/scraper/linkedinjobpostingscraper/` and `linkedincompanyscraper/`
- Website email search and AI message: `websitescraper/` and `chatgptapi/`
- CV upload and email sending: `user/`, `cloudflare/`, and `mailservice/`

The short paths above are also under `src/main/java/com/karakoc/scraper/`.

## Run locally

You need Java 17, Maven, MySQL, a browser for Selenium, and your own AI, SMTP, and R2 settings. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `OPENAI_API_KEY`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. See `src/main/resources/application.properties` for the other settings.

```bash
mvn spring-boot:run
```
