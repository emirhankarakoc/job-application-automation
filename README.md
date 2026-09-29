# Job Application Automation

This Java app helps with the repeated work in job applications. It takes a LinkedIn job-post URL, reads the job and company pages, looks for a contact email on the company's website, writes a draft with an AI API, sends the email through SMTP, and saves what it sent.

## How it works

- `POST /queue` saves a pending request in MySQL. The same URL cannot be added twice.
- A scheduled worker takes one pending request at a time.
- Selenium reads the job and company pages. The website crawler checks up to 20 pages for email addresses.
- The app creates an email subject and body, sends the message, and stores the job, company, draft, and sent-mail records.
- The request becomes `DONE` or `FAILED`.

The queue is stored in MySQL. A scheduled task in the same Spring Boot app reads it.

## Tech and code

Java 17, Spring Boot, MySQL, JPA, Spring Security/JWT, Selenium, an AI API, and SMTP. The AWS S3 SDK is used with Cloudflare R2 where storage is configured. 

- Queue and status: `src/main/java/com/karakoc/scraper/prodbykarakoc/`
- Job and company readers: `linkedinjobpostingscraper/` and `linkedincompanyscraper/`
- Website search and email draft: `websitescraper/` and `chatgptapi/`
- Email records: `mailservice/` and `sentmails/`

All paths above are under `src/main/java/com/karakoc/scraper/`.

## Run locally

You need Java 17, Maven, MySQL, a browser for Selenium, and your own AI and SMTP accounts. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `OPENAI_API_KEY`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. See `src/main/resources/application.properties` for all settings.

```bash
mvn spring-boot:run
```

Account API tests are in `src/test`. The scraper reads live websites, so its page selectors may need updates when those sites change.
