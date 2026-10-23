<div align="center">

# 🚀 JobOrbit — Backend API
### Spring Boot 3 · PostgreSQL · JWT · Greenhouse ATS · AI Recommendation Engine

[![API Health](https://img.shields.io/badge/API-Live%20on%20Render-0f62fe?style=for-the-badge&logo=render)](https://jobportal-26gu.onrender.com/actuator/health)
[![Frontend](https://img.shields.io/badge/Frontend-JobOrbit-6929c4?style=for-the-badge&logo=vercel)](https://job-orbit-eleven.vercel.app)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)](https://openjdk.org)

REST API backend for **JobOrbit** — an AI-powered job portal that aggregates real job listings from Greenhouse ATS companies, parses candidate resumes, and ranks jobs against candidate profiles using NLP cosine similarity.

*Your skills in orbit. Your career in motion.*

[📦 Frontend Repo](https://github.com/SaiMethish/career-gallery) &nbsp;·&nbsp; [🔗 Live App](https://job-orbit-eleven.vercel.app)

</div>

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                       CLIENT — JobOrbit (Angular 17)                │
│                       job-orbit-eleven.vercel.app                   │
└────────────────────────────┬────────────────────────────────────────┘
                             │  HTTPS + JWT Cookie (SameSite=None)
┌────────────────────────────▼────────────────────────────────────────┐
│                  Spring Boot 3 REST API  (Render)                   │
│                                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐   │
│  │  Auth / JWT │  │  Job Search  │  │  AI Recommendation      │   │
│  │   Module    │  │  + Filters   │  │  Engine (Cosine+Heurist) │   │
│  └─────────────┘  └──────────────┘  └─────────────────────────┘   │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐   │
│  │ Greenhouse  │  │   Resume     │  │  Role-Based Access      │   │
│  │  ATS Sync  │  │   Parser     │  │  (CANDIDATE/RECRUITER/  │   │
│  │  + Cron    │  │  + Supabase  │  │   ADMIN)                │   │
│  └─────────────┘  └──────────────┘  └─────────────────────────┘   │
└─────────────┬─────────────────────────┬───────────────────────────┘
              │                         │
   ┌──────────▼──────┐       ┌──────────▼──────────┐
   │  PostgreSQL DB  │       │  FastAPI AI Service  │
   │    (Render)     │       │  (NLP / Embeddings)  │
   └─────────────────┘       └──────────────────────┘
```

---

## ✨ Core Features

### 🔐 Authentication & Authorization
- JWT access tokens (short-lived, 15 min) + refresh tokens (httpOnly cookies, 7 days)
- Silent refresh flow with concurrent-request queuing — no parallel duplicate refresh calls
- Three roles: `CANDIDATE`, `RECRUITER`, `ADMIN`
- Session expiry → auto logout → preserve intended redirect destination URL
- Centralized CORS config with `SameSite=None; Secure=true` for Vercel–Render cross-origin

### 🏗️ Dual Job Source Model

**Source 1 — Direct Recruiter Job Postings**
- Registered recruiters create a company profile and post job listings directly on the platform
- Each listing includes: title, description, required skills, salary range, experience range, job type, location, and status
- Recruiters manage their own listings (edit, close, delete) and review all applicants per job
- Candidates apply directly on the platform; applications are tracked with status updates (Shortlisted / Rejected / Offered)
- Set with `source = INTERNAL` in the database

**Source 2 — Greenhouse ATS Integration (`GreenhouseSyncService`)**
- Fetches live job listings from 35+ registered Indian tech companies via the Greenhouse public API
- Filters to India-relevant locations using `IndiaLocationUtil`
- Upserts into PostgreSQL using the same `Job` entity schema as recruiter-posted jobs
  (new → insert, changed → update, closed/expired → delete)
- Extracts skills from job descriptions using keyword matching against a curated tech dictionary
- Sets `source = GREENHOUSE` and `applicationUrl` to the company's original apply link so candidates are redirected externally
- Daily automated sync via `@Scheduled(cron = "0 2 * * *")` — runs at 2 AM every day
- On-demand sync available via `POST /admin/sync/greenhouse`
- Cache eviction (`jobSearch`, `jobDetails`, `recommendedJobs`) runs after every sync

### 🤖 AI Recommendation Engine (`RecommendationServiceImpl`)
- Builds candidate text from: resume text, skills, experience, education, location
- Sends candidate text + up to 200 active job texts to FastAPI NLP microservice
- Receives cosine similarity scores → calibrated to intuitive 10–100% range
- Falls back to heuristic skill-overlap + title-word-boost scoring if AI service is unavailable
- Returns results sorted strictly descending by score, limited to top 100

### 📄 Resume Parsing
- PDF upload to Supabase Storage (persistent, CDN-backed)
- Text extraction and structured parsing for skills, experience, and education
- Resume text persisted on the candidate profile for AI matching on every request

### 🔍 Job Search & Filtering
- Full-text keyword search across title, company name, and skills
- Filters: location, job type, experience range, salary range, company category, source (INTERNAL/GREENHOUSE)
- Paginated results via Spring Data Pageable
- Redis caching for repeated searches

---

## 🗂️ Package Structure

```
com.example.jobPortal/
  client/          # Greenhouse API REST client (GreenhouseClient)
  config/          # Spring Security, CORS, Redis, Scheduler config
  configs/         # DB migration configs (schema patches for ATS)
  controller/      # 13 REST controllers
  dto/             # Request/Response DTOs, ATS-specific DTOs
  entity/          # JPA entities: User, Job, CandidateProfile, AtsCompany…
  exception/       # Custom exceptions + global @ControllerAdvice handler
  filter/          # JWT authentication filter
  mapper/          # MapStruct mappers (DTO ↔ Entity)
  repository/      # Spring Data JPA repositories
  scheduler/       # Daily Greenhouse job sync scheduler
  security/        # JWT utility, UserDetailsService implementation
  service/         # 18 business logic service interfaces + Impl/
  util/            # IndiaLocationUtil, slug helpers
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 17 |
| Database | PostgreSQL (Render managed) |
| Cache | Redis (Spring Cache) |
| Auth | JWT (jjwt library) + Spring Security |
| File Storage | Supabase Storage |
| ATS Integration | Greenhouse Public API |
| AI / NLP | FastAPI + Sentence Transformers |
| HTTP Client | Spring `RestClient` (Java 17) |
| ORM | Spring Data JPA + Hibernate |
| Build | Maven Wrapper (`./mvnw`) |
| Deployment | Render (free tier) |

---

## 🔌 API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/login` | Public | Login with email + password → returns JWT |
| POST | `/auth/register` | Public | Register new CANDIDATE or RECRUITER |
| POST | `/auth/refresh` | Cookie | Exchange refresh token → new access token |
| POST | `/auth/logout` | JWT | Invalidate session, clear cookies |

### Jobs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/jobs/search` | Public | Search with keyword, location, type, exp filters (paginated) |
| GET | `/jobs/{id}` | Public | Get single job details |
| GET | `/jobs/recommended` | CANDIDATE | AI-ranked recommendations (10–100%, descending) |
| POST | `/jobs` | RECRUITER | Post a new job listing |
| PUT | `/jobs/{id}` | RECRUITER | Update own job listing |
| DELETE | `/jobs/{id}` | RECRUITER/ADMIN | Delete job |

### Resume & Candidate Profile

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/resume/upload` | CANDIDATE | Upload PDF → parse and store |
| GET | `/candidate/profile` | CANDIDATE | Get own candidate profile |
| PUT | `/candidate/profile` | CANDIDATE | Update skills, experience, education |
| GET | `/candidate/applications` | CANDIDATE | Paginated application history |

### Recruiter

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/recruiter/profile` | RECRUITER | Get own recruiter/company profile |
| PUT | `/recruiter/profile` | RECRUITER | Update company profile |
| GET | `/jobs/{id}/applicants` | RECRUITER | List applicants for a job |
| PATCH | `/applications/{id}/status` | RECRUITER | Update applicant status |

### Admin

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/admin/sync/greenhouse` | ADMIN | Trigger manual Greenhouse ATS sync |
| GET | `/admin/users` | ADMIN | List all users (paginated) |
| GET | `/admin/jobs` | ADMIN | Moderate all job listings |

---

## 🚀 Local Development Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis (optional — degrades gracefully without it)
- FastAPI AI service (optional — heuristic fallback used if unavailable)

### Setup

```bash
# Clone the repository
git clone https://github.com/SaiMethish/jobPortal.git
cd jobPortal

# Build and run
./mvnw spring-boot:run
```

---

## 🌐 Deployment

| Service | URL |
|---------|-----|
| Backend REST API | https://jobportal-26gu.onrender.com |
| Health Check | https://jobportal-26gu.onrender.com/actuator/health |
| Frontend (JobOrbit) | https://job-orbit-eleven.vercel.app |

> ⚠️ **Render Free Tier:** The server sleeps after 15 minutes of inactivity. The first request after sleep takes ~60 seconds. The Angular frontend shows a loading state gracefully during wake-up.
