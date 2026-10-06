# booking-saas

Multi-tenant appointment/booking SaaS. Spring Boot (Java 21) REST API with JWT + role-based
access control, Vue 3 + Vite + TypeScript SPA, deployed to Google Cloud Run + Cloud SQL
PostgreSQL via Terraform and keyless (Workload Identity Federation) GitHub Actions.

## Stack

- **Backend** — Java 21, Spring Boot 3.5, Spring Security 6 (JWT via jjwt), Spring Data JPA,
  Flyway, PostgreSQL 16, JUnit 5 + Mockito + Testcontainers, Maven.
- **Frontend** — Vue 3.5, Vite 8, TypeScript 6, vue-router 5, Pinia 4, axios, Vitest.
- **Infra** — Docker (multi-stage), Cloud Run, Cloud SQL, Artifact Registry, Terraform,
  GitHub Actions + Workload Identity Federation.

## Architecture

```
booking-saas/
  backend/   Spring Boot API (packaged by feature: auth, catalog, user, appointment)
  frontend/  Vue SPA (api client, auth store, router + guards, views)
  infra/     Terraform: Cloud SQL, Cloud Run, Artifact Registry, IAM + WIF
  Dockerfile multi-stage (node build -> maven package with SPA -> JRE)
```

Multi-tenancy: every tenant-scoped row carries a `tenant_id`; a ThreadLocal `TenantContext`
(populated by the JWT filter) scopes every repository query. The crown-jewel business rule
lives in `AppointmentService.create` — a booking is rejected if the staff member already has a
non-cancelled appointment overlapping the requested window.

Roles: `OWNER` (one per tenant, created at signup), `ADMIN`, `STAFF`. Method security
(`@PreAuthorize`) gates writes: services and team creation need OWNER/ADMIN, role changes and
deletion need OWNER.

## Local development

Prerequisites: JDK 21, Node 24, Docker.

```bash
# 1. Start PostgreSQL
docker compose up -d postgres

# 2. Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# 3. Frontend (port 5173, proxies /api to :8080)
cd frontend && npm install && npm run dev
```

Open http://localhost:5173, sign up (creates your tenant + owner account), then add a service,
add a staff member, and book.

## Tests

```bash
cd backend  && ./mvnw verify           # unit + Testcontainers integration
cd frontend && npm run test:unit       # Vitest
cd frontend && npm run lint && npm run build
```

The backend integration suite boots the full context against a real PostgreSQL and asserts the
double-booking guard, cross-tenant isolation (404 on foreign tenant references), RBAC (STAFF
cannot create services), and 401 on unauthenticated API calls.

## CI/CD

- `.github/workflows/ci.yml` — on push/PR: `mvnw verify` (Java 21) + npm lint/test/build (Node 24).
- `.github/workflows/deploy.yml` — on push to master: builds the image, pushes to Artifact
  Registry, and deploys to Cloud Run, authenticated keylessly via Workload Identity Federation.

## Deployment

One-time bootstrap:

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # fill project_id, passwords, github_repo
terraform init
terraform plan
terraform apply                                # creates Cloud SQL, Cloud Run, AR, WIF + IAM
```

`terraform apply` outputs the values needed to wire GitHub Actions. Set them as repository
**variables** (Settings → Secrets and variables → Actions):

| Variable      | Terraform output                |
|---------------|---------------------------------|
| `WIF_PROVIDER`| `workload_identity_provider`    |
| `GCP_SA`      | `service_account_email`         |
| `AR_HOST`     | `southamerica-east1-docker.pkg.dev` (region-dependent) |
| `AR_IMAGE`    | `artifact_registry_image`       |
| `SERVICE_NAME`| `booking-api`                   |
| `REGION`      | `southamerica-east1`            |

Then every push to `master` (or a manual `workflow_dispatch`) builds and deploys. The service
URL is the `service_url` output. Tear down with `terraform destroy`.

Note: Cloud SQL is a billed resource (~$10–20/mo for the smallest instance); destroy it when
not demoing.

## API

| Method | Path                     | Access            | Description |
|--------|--------------------------|-------------------|-------------|
| POST   | `/api/auth/signup`       | public            | Create tenant + owner, returns JWT |
| POST   | `/api/auth/login`        | public            | Login, returns JWT |
| GET    | `/api/services`          | authenticated     | List services |
| POST/PUT/DELETE | `/api/services` | OWNER/ADMIN | Manage services |
| GET    | `/api/users`             | authenticated     | List team |
| POST   | `/api/users`             | OWNER/ADMIN       | Add member |
| PATCH  | `/api/users/{id}/role`   | OWNER             | Change role |
| DELETE | `/api/users/{id}`        | OWNER             | Remove member |
| GET/POST | `/api/appointments`    | authenticated     | List / create (double-booking guarded) |
| PATCH  | `/api/appointments/{id}/status` | authenticated | Update status |
| DELETE | `/api/appointments/{id}` | OWNER/ADMIN       | Delete |
| GET    | `/api/health`            | public            | Liveness |
