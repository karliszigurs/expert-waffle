# About

A simple ledger application with external currency rate conversion API integration.

Goal of the application was to follow the specification provided, therefore:

* Navigating existing client accounts is implemented
* Reviewing transaction history of accounts is implemented
* Transfers between accounts in same currency are implemented
* Transfers between accounts in different currencies are implemented
* DB versioning (using flyway) is present
* 100% business logic tests coverage
* Transactions can both succeed or fail and will be recorded with corresponding description

However, the following is left for next iteration:

* Fractional currency support - only basic support is added and needs further work to handle
  conversions between different-fractionals (e.g. USD<>JPY) currencies correctly.
* Managing clients and accounts is not implemented - database is pre-seeded with example accounts 
* Integration of "proper" external database. Currently we use H2 in-process one.
* Containerization (e.g. docker compose use)
* Authentication, security, cors handling, etc
* Logging, metrics & observability

# Getting Started

* Java21 required (and unix based system is assumed)
* Configure [Currency Beacon](https://currencybeacon.com/) API key in `application.properties`
* Execute `gradew bootRun` from the root directory of the project
* Navigate to `examples/` and execute `./examples.sh`
* Inspect the output for actions performed :)
* Pre-seed data is located under `main/resources/db.migration.h2` 
* If DB cleanup is required delete \*.db files created in the root directory.
  DB will be automatically recreated on the next application start.
