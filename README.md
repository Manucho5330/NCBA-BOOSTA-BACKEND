# NCBA BOOSTA Backend

A Java 25 / Spring Boot 3.5 / Spring Cloud 2025.0 / Maven multi-module backend
adjacent to `NCBA-BOOSTA-PROJECT`. Customer profiles, loans, and customer
transactions are owned by `customer-service`; anchor records and portal data
are owned by `anchor-service`. Customer notifications, loans and drawdown requests are
stored in the same customer-service database. The internal LMS has been removed;
Customer and Anchor remain available, with an external LMS integration seam
that returns `503 Service Unavailable` until its API contract is configured.

## Modules and local ports

| Module | Purpose | Port |
| --- | --- | ---: |
| `discovery-server` | Eureka service registry | 8761 |
| `config-server` | Spring Cloud Config Server | 8888 |
| `portfolio-service` | External customer and anchor scoring adapter only | 8091 |
| `customer-service` | Customer and loan records, plus DWH profile adapter | 8083 |
| `anchor-service` | Anchor records, onboarding stages and approval trail | 8084 |
| `auth-service` | Credential storage, login, JWT issue and validation | 8086 |
| `banking-integration-service` | Core banking transaction initiation and confirmation adapter | 8087 |
| `audit-service` | Audit event write/query API | 8088 |
| `reporting-service` | Portfolio report API with an external LMS provider seam | 8089 |
| `api-gateway` | Single entry point and service routing | 8080 |
| `common` | Shared API errors and exception handling | library |

## API DTOs and Swagger

Typed API request and response contracts live in the domain services'
`dto/request` and `dto/response` packages. Controllers map those contracts to
and from internal entities where applicable; technical idempotency keys are
not returned in API responses.

In Swagger UI, path, query, and header inputs appear under **Parameters**.
JSON sent with `@RequestBody` appears separately under **Request body**. GET
endpoints generally have no request body. If Swagger reports that it failed to
load an API definition, the selected service's OpenAPI document could not be
retrieved; that is separate from whether a DTO package exists.

Customer, Anchor, Auth, and Audit use the MySQL
`ecosystem_boost` schema. Hibernate creates or updates their entity tables on
service startup in the local-development configuration. Portfolio is stateless
and has no customer, loan, or anchor database.

The internal LMS portal, service, staff-profile service, and their routes have
been removed. `reporting-service` exposes `ExternalLmsPortfolioProvider` as the
adapter boundary; its default implementation responds with `503 Service
Unavailable` until the external LMS API and authentication contract are agreed
and implemented. No external LMS URL, payload, or credentials are assumed.

The Gateway routes `/api/v1/**` endpoints to their owning services and exposes
each OpenAPI document at `/openapi/{service}` in the combined Swagger UI. Each
OpenAPI document uses a relative root server URL, so **Try it out** calls use
the Gateway origin instead of calling a service's port directly. After
changing this setting, restart the services and Gateway, then reload Swagger.
T24 GL entries and reconciliation exceptions remain unavailable: this
workflow does not create simulated records or endpoints for those unsupported
data sources.
Swagger requests use the Gateway origin; authorize in the UI with a valid
Bearer token for a `BANK_MAKER` or `BANK_CHECKER` account before trying
protected APIs.

## Build

From this directory, with JDK 25 installed:

```sh
./mvnw clean package
```

## Run locally

Build once with `./mvnw clean package` (the Spring Boot `repackage` goal creates
executable application JARs), then start each application in its own terminal
from this directory. Keep every terminal open. Start Discovery and Config
first, followed by the domain services, and start the Gateway last:

```sh
java -jar discovery-server/target/discovery-server-0.1.0-SNAPSHOT.jar
java -jar config-server/target/config-server-0.1.0-SNAPSHOT.jar
java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar
java -jar portfolio-service/target/portfolio-service-0.1.0-SNAPSHOT.jar
java -jar customer-service/target/customer-service-0.1.0-SNAPSHOT.jar
java -jar anchor-service/target/anchor-service-0.1.0-SNAPSHOT.jar
java -jar banking-integration-service/target/banking-integration-service-0.1.0-SNAPSHOT.jar
java -jar audit-service/target/audit-service-0.1.0-SNAPSHOT.jar
java -jar reporting-service/target/reporting-service-0.1.0-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar
```

The apps can also run without Config Server/Eureka for local development;
the API gateway uses Eureka `lb://` service discovery, so start Eureka and
Config Server before the services and gateway.
For Eureka clients on a local machine, the application configs prefer
registering the instance IP address rather than its hostname so the Gateway
can connect to the registered service endpoints.

For anonymous local API testing only, start the secured API services and the
Gateway with the `local-no-auth` Spring profile. For example:

```sh
./mvnw -pl anchor-service spring-boot:run -Dspring-boot.run.profiles=local-no-auth
./mvnw -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local-no-auth
```

Apply that profile to each secured API service you start: `auth-service`,
`portfolio-service`, `customer-service`, `anchor-service`,
`banking-integration-service`, and `audit-service`. Discovery
and Config Server do not need it. The profile bypasses endpoint authorization
and does not require `BOOSTA_AUTH_JWT_SECRET`; it is unsafe outside a local,
isolated development environment and must not be used for deployment. Some
controller operations consume JWT claims for ownership or actor information;
those operations still need a valid token and may not work correctly in this
profile. Use normal authentication for those workflows.

## MySQL local setup

Create the schema once in MySQL if it does not already exist:

```sql
CREATE DATABASE ecosystem_boost;
```

The Customer, Anchor, Auth, and Audit services default to
`jdbc:mysql://localhost:3306/ecosystem_boost` and create/update their tables
with Hibernate `ddl-auto=update` when they start. Override the defaults with
`BOOSTA_DB_URL`, `BOOSTA_DB_USERNAME`, and `BOOSTA_DB_PASSWORD` in each service
process (including Config Server when it is running). For the local root
account shown in the setup transcript, the default username is `root` and the
default password is empty. Prefer a dedicated least-privilege MySQL account
instead of root; do not put production credentials in source control. The
default JDBC URL is for local development; configure TLS and a managed secrets
provider in non-local environments.
`BOOSTA_JPA_DDL_AUTO` defaults to `update` for local development; set it to
`validate` in environments where schema changes are managed through reviewed
migrations. The database server must be running and the schema must exist
before these four services start.

Use JDK 25. The root POM uses Spring Boot 3.5.16 and Spring Cloud 2025.0.3,
which are compatible with the configured Java 25 target. Springdoc UI modules
use version 2.8.17. The repository includes a Maven bootstrap wrapper, so a
separate global Maven installation is not required. On Git Bash run:

```sh
./mvnw -version
./mvnw clean package
```

The first wrapper run downloads Apache Maven 3.9.9 to the user's Maven cache;
it needs network access to `archive.apache.org` and Windows `curl.exe`/`tar.exe`.
From Command Prompt, run `mvnw.cmd clean package`. The wrapper invokes the
project's root `pom.xml` and Maven uses the Java 25 compiler release configured
there. The old `./mvwn` command was misspelled; `spring-boot: run` also contains
an invalid space. To run one service after packaging, use the exact Spring
Boot goal, for example `./mvnw -pl auth-service spring-boot:run`.

In normal secured mode, configure the same `BOOSTA_AUTH_JWT_SECRET` in
`auth-service`, `customer-service`, `anchor-service`, `portfolio-service`, and
`api-gateway` (at least 32 characters).
The `local-no-auth` profile described above does not require this JWT secret.
Set `BOOSTA_AUTH_BOOTSTRAP_SECRET` in `auth-service` for one-time administrator
creation, and `BOOSTA_AUTH_SERVICE_SECRET` in `auth-service` for protected
service-to-service account provisioning. Keep these values outside source
control. The Customer, Anchor, Auth, and Audit
services persist to the local MySQL database described below.
For automated drawdown confirmation, provision a dedicated enabled
`BANK_CHECKER` reconciliation account and configure its login as
`BOOSTA_RECONCILIATION_IDENTIFIER` and `BOOSTA_RECONCILIATION_SECRET` for
`customer-service`. The service polls core banking every 30 seconds by default
(`BOOSTA_DRAWDOWN_CONFIRMATION_POLL_INTERVAL` configures the interval).

## Gateway routes and API

All routes are prefixed `/api/v1`:

* `GET /api/v1/customers`, `GET /api/v1/customers/{id}`
* `POST /api/v1/customers` to create an onboarding customer
* `PUT /api/v1/customers/{id}` to update the shared customer profile
* `GET /api/v1/anchors`, `GET /api/v1/anchors/{id}`
* `GET /api/v1/anchor-onboardings`, `POST /api/v1/anchor-onboardings`, and
  `POST /api/v1/anchor-onboardings/{id}/steps/{step}/complete` for the
  sequential anchor creation workflow. It records pitch acceptance, due
  diligence evidence, credit paper, Ecosystem review, ordered CR approvals,
  signed MOU, CAR approvals, compiled pack, anchor account/pricing details,
  IBPS reference and retailer consents.
* `GET /api/v1/loans`, `GET /api/v1/loans/{id}`
* `POST /api/v1/drawdowns` with `{"customerId":"cust-001","anchorId":"anchor-eabl","amount":10000,"tenorDays":30,"purpose":"Stock"}`; it stays `PENDING` until core banking confirmation
* `GET /api/v1/drawdowns`, `GET /api/v1/drawdowns/{id}` to list the signed-in
  customer's requests or poll one request
* `GET /api/v1/transactions` for confirmed Customer-service disbursements
* `GET /api/v1/notifications` for the signed-in customer or anchor's notices;
  bank staff can create a notice with `POST /api/v1/notifications`, and portal
  users can update its read/archive state with `PATCH /api/v1/notifications/{id}`
* `POST /api/v1/repayments` with `{"loanId":"loan-1","amount":1000,"method":"NCBA_ACCOUNT"}` and an `Idempotency-Key` header; repayment remains pending until core banking confirms it
* `PATCH /api/v1/loans/{id}/repayments` with `{"amount": 1000}` to record a
  pending repayment (`Idempotency-Key` header required)
* `GET /api/v1/portfolio/summary` for the Customer-service summary
* `POST /api/v1/auth/login` with `{"identifier":"user@example.com","secret":"..."}` (customers use their phone and four-digit PIN); handled by `auth-service`
* `POST /api/v1/auth/bootstrap` with `X-BOOSTA-BOOTSTRAP-SECRET` and `{"identifier":"admin@example.com","secret":"..."}` to create the first administrator; only succeeds before any admin exists
* `POST /api/v1/scoring/customers` and `POST /api/v1/scoring/anchors` to submit
  scoring data, and `GET /api/v1/scoring/{id}` to retrieve a result using the
  scoring ID returned by the provider. These are listed in Swagger under
  **Scoring**; they require a customer or anchor JWT, and the provider enforces
  access to result IDs. Configure `BOOSTA_SCORING_BASE_URL` and
  `BOOSTA_SCORING_API_KEY` for the provider integration.
* `GET /api/v1/customer-data-warehouse/customers/{id}` to fetch a customer record from the configured DWH
* `POST /api/v1/banking/transactions` and `GET /api/v1/banking/transactions/{reference}/confirmation` for the core banking adapter
* `POST /api/v1/audit/events` and `GET /api/v1/audit/events` for audit events
* `GET /api/v1/reports/portfolio` for an external LMS portfolio report; returns
  `503 Service Unavailable` until an external provider is configured

The authentication service stores credentials as BCrypt hashes and issues
one-hour HS256 JWTs. Anchor onboarding is managed independently by
`anchor-service`; customer account onboarding is separate from the removed
staff-profile workflow. Staff passwords must contain at least 12 characters;
customer PINs are four digits. The API gateway validates JWTs and applies role
restrictions for the remaining Customer and Anchor workflows.

## External integration configuration

The vendor request/response fields below are adapter contracts for this
prototype; confirm them with each integration owner before production.

* `BOOSTA_SCORING_BASE_URL`, `BOOSTA_SCORING_API_KEY`: scoring vendor; adapter
  paths are `/scores/customer` and `/scores/anchor`. Both decisions are served
  from the portfolio service.
* `BOOSTA_DWH_BASE_URL`, `BOOSTA_DWH_API_KEY`: DWH customer lookup path is
  `/customers/{customerId}`.
* `BOOSTA_CORE_BANKING_BASE_URL`, `BOOSTA_CORE_BANKING_API_KEY`: transaction
  initiation uses `POST /transactions`; confirmation uses
  `GET /transactions/{reference}/confirmation`. An idempotency key is required.

These adapters return `503 Service Unavailable` when an integration is not
configured rather than manufacturing a success response. Audit and reporting
are operational scaffolds, not production-grade immutable audit storage or a
warehouse-backed reporting pipeline.

Anchor onboarding is a persisted, sequential workflow in `anchor-service`.
Makers can list and advance cases they created; Checkers/Admins can review all
cases. Direct anchor creation is disabled so the staged workflow cannot be
bypassed.
Initial required evidence is three years of audited financials, debtor/creditor
ageing, director IDs, KRA PINs and CRB certificates, and company CR8, CR12, and
Memorandum/Articles. The CR trail captures the listed originator,
recommendation, and implementation approvals in order; the CAR trail captures
BM/RM, Ecosystem, Consumer Analysts, CRM, and Retail Management sign-offs.
Rejected approval entries remain recorded and return the case for rework.
Legal-requested MOU revisions are logged and keep the case at the MOU gate
until the revised document is signed.
Document filenames and references are metadata only: this implementation does
not upload or store document contents, and document references must point to an
approved secure repository. Docu-Approval, Legal, IBPS, T24 account validation,
and scoring are explicit external integration points, not live integrations.
An anchor record created after the IBPS reference is recorded has drawdowns
disabled; retailer consent makes the case ready for scoring but does not itself
trigger scoring or activate drawdowns.

The supplied loan-flow diagram also describes T24 loan-account creation and
posting, due-date automatic debit from a customer transactional account when
funds are sufficient, and customer-initiated prepayment through NCBA/M-Pesa or
branch cash deposit. The current backend only models pending disbursement and
repayment confirmation and does not yet implement scheduled account sweeps,
branch cash-deposit ingestion, T24 loan-account posting, or the M-Pesa adapter.

The Customer service persists a pending drawdown, reserves the anchor limit,
and submits an idempotent transfer request to Banking Integration. It creates
the loan and transaction ledger entry only after a core-banking confirmation
returns a recognized success status. Rejected transfers release the reserved
limit; network errors leave the request pending for retry. The data warehouse
is used only for customer information, not transfer confirmation. Repayments
also remain pending until confirmed; the loan balance and transaction ledger
are updated only after confirmation. The customer and anchor portal routes do
not use Portfolio for domain data.

Direct service URLs are `http://localhost:8091` (Portfolio scoring only),
`http://localhost:8083` (Customer), and
`http://localhost:8084` (Anchor); through the gateway use
`http://localhost:8080`.
The API Gateway provides a combined Swagger UI at
`http://localhost:8080/swagger-ui/index.html`. It lists the OpenAPI documents
for Authentication, Customer, Anchor, Banking Integration, Audit, Reporting,
and Scoring. The gateway proxies each
document through `/openapi/{service}` so browser requests stay on the gateway
origin. The corresponding service docs are available directly at
`http://localhost:{port}/v3/api-docs` for troubleshooting.

To pick up the combined UI and endpoint documentation, rebuild and restart the
Gateway and any services whose POM or security configuration changed. The
Swagger UI and OpenAPI documents are intentionally public for local API
exploration; actual API endpoints retain their role-based authorization.
Each API document declares the shared `bearerAuth` JWT scheme, so Swagger UI
shows an **Authorize** button. Use the Authentication API's login operation
with a valid account, copy its `token` value, then select **Authorize** and
paste the token (without the `Bearer ` prefix) before executing protected
operations. The login and bootstrap operations themselves do not require a
JWT; bootstrap also requires the configured `X-BOOSTA-BOOTSTRAP-SECRET`
header and works only before the first administrator exists. A token must have
the role permitted for the operation. Discovery, Config Server, and the
`common` library are infrastructure/components rather than business APIs and
are not listed as Swagger services.

## Frontend integration

The React app still uses mock state for workflow features that do not have
backend endpoints. Its customer-facing API adapter reads customer profiles,
anchors, loans, and transactions from the gateway, and persists profile
updates, single drawdowns, and repayments. The internal LMS portal and API client are removed; the external LMS will be
connected through a separately implemented provider once its contract is
available. T24 GL entries and reconciliation exceptions remain unavailable
because no real backend source is configured.
Anchor onboarding stages are persisted by Anchor Service, but external
Docu-Approval sign-off and secure document storage are not integrated.
Multi-signatory loan approvals and other remaining anchor operations are
still prototype-only and should be moved behind APIs before production.

## Requirements baseline from the solution review

The following items are target requirements and must not be treated as
implemented merely because the service boundaries can accommodate them:

| Requirement | Current state / next design decision |
| --- | --- |
| T24 customer, business, KYC, and account prefill for individual and non-individual customers | DWH customer lookup adapter exists; T24 fields, verification, consent, and account-opening handoff are not integrated. |
| NCBA NOW journey when an eligible customer has no NCBA account | Not implemented; define eligibility response, redirect/resume contract, and re-verification before continuing. |
| Business-premises geolocation and geotagging | Not implemented; define capture consent, precision, retention, and authorized access. |
| Multiple anchors and independent drawdowns across tenures | Anchor/customer records and single drawdown flow exist; per-customer/per-anchor facility allocation and independent maturity/repayment schedules are not complete. |
| Customer aggregate exposure and duplicate-financing controls | Not implemented; define authoritative limit source, concurrent reservation rules, and aggregate exposure checks across anchors and active drawdowns. |
| External LMS reporting and facility administration | The internal implementation has been removed. Define the external API, authentication, data ownership, and authorization contracts before adding an adapter. |
| Joint, partnership, and corporate account mandates | Not implemented; define signatory retrieval/verification, approval thresholds, expiry, and transaction execution rules with T24. |
| Anchor portfolio dashboard and obligations | Anchor portal data exists, but customer facilities, upcoming dues, and overdue obligations are not yet exposed as an anchor-scoped servicing view. |

Before implementing these items, approve the customer/anchor/facility data
model, T24 and NCBA NOW API contracts, role and authorization matrix,
exception/reconciliation flows, and immutable audit requirements.

Customer and Anchor APIs validate JWTs and role claims. Customer and Anchor own their domain
records, and Portfolio exposes only the externally integrated customer and
anchor scoring endpoints. Before production, move the local shared database to
managed MySQL with least-privilege service access, keep internal service ports
private, add account recovery and login rate limiting, implement immutable
audit retention, and confirm vendor-specific signing, retry, timeout, and
reconciliation behavior.
