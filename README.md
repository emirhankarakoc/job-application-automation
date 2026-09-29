# Job Application Automation

A Java backend experiment that takes a LinkedIn job-posting URL and prepares tailored email outreach. It connects several steps that would otherwise be manual: extracting the posting, finding the employer's website and contact addresses, drafting an email, sending it, and recording the outcome.

## Processing flow

```text
POST /queue -> MySQL pending order -> scheduled worker
    -> Selenium job/company extraction -> employer-site crawl (up to 20 pages)
    -> AI-generated subject/body -> SMTP send -> sent-mail record
```

`QueueManager` owns the workflow. It rejects duplicate posting URLs, records the order, and links the job, company, website, generated draft, and sent-mail records. `ScheduledService` polls pending orders in one application process and marks completed or failed work. The queue is database-backed; it is **not** Kafka or a separate message broker.

## Stack and code map

- Java 17, Spring Boot 3.3, Spring Security/JWT, JPA, MySQL
- Selenium for page extraction; a bounded crawl for employer contact addresses
- External AI API for the tailored draft; Spring Mail/SMTP for delivery
- AWS SDK for Cloudflare R2-compatible storage where configured (not an AWS deployment)

| Component | Path |
| --- | --- |
| Order API and status | `src/main/java/com/karakoc/scraper/prodbykarakoc/` |
| Job and company extraction | `src/main/java/com/karakoc/scraper/linkedinjobpostingscraper/`, `linkedincompanyscraper/` |
| Website crawl and email draft | `src/main/java/com/karakoc/scraper/websitescraper/`, `chatgptapi/` |
| Delivery and sent records | `src/main/java/com/karakoc/scraper/mailservice/`, `sentmails/` |

## Local setup

Requires Java 17, Maven, MySQL, a browser compatible with the Selenium setup, and credentials for the external services used in a full run.

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `OPENAI_API_KEY` as appropriate. `src/main/resources/application.properties` lists the full configuration, including optional R2 values. Then:

```bash
mvn spring-boot:run
```

The `docker-compose.yml` in this snapshot is only a commented draft; it does not start the stack. The repository has account API tests under `src/test`, but the complete scraping-to-delivery workflow has not been verified by an automated integration test in this public snapshot. External site markup and email delivery configuration affect a full run.
