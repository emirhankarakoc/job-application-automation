# Job Application Automation

This Java app helps with the repeated work in job applications. It takes a LinkedIn job-post URL, reads the job and company pages, looks for a contact email on the company's website, writes a draft with an AI API, sends the email through SMTP, and saves what it sent.

## How it works

- `POST /queue` saves a pending request in MySQL. The same URL cannot be added twice.
- A scheduled worker takes one pending request at a time.
- Selenium reads the job and company pages. The website crawler checks up to 20 pages for email addresses.
- The app creates an email subject and body, sends the message, and stores the job, company, draft, and sent-mail records.
- The request becomes `DONE` or `FAILED`.

This is a queue in the database and one Spring Boot process. It does not use Kafka or a separate worker service.

## Tech and code

Java 17, Spring Boot, MySQL, JPA, Spring Security/JWT, Selenium, an AI API, and SMTP. The AWS S3 SDK is used with Cloudflare R2 where storage is configured. That is not an AWS deployment.

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

The Docker Compose file is an old draft and does not start the app. Account API tests are in `src/test`; the full browser-to-email flow does not have an automated end-to-end test. Website changes can also break the scraper.
