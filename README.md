# MedAssist — Clinical Decision Support System (CDSS)

> A system assisting healthcare professionals in medical diagnosis and treatment recommendations, analyzing patient symptoms, medical history, and test results — with decision support and patient data privacy.
> **Stack:** Servlets · JSP · JDBC · Spring (Core + MVC + Security)

---

## 1. Problem Statement

Doctors juggling high patient loads often don't have time to cross-reference a patient's full history, current symptoms, and lab results before making a call. Misdiagnosis or delayed diagnosis due to information overload is a real, documented problem in clinical settings.

**MedAssist doesn't replace the doctor.** It's a decision-support layer: it aggregates patient data (history + symptoms + test results), runs it through a rule-based inference engine, and surfaces **ranked, explainable diagnosis suggestions with confidence scores** — the doctor reviews, accepts, overrides, or requests more tests. Every decision is logged for accountability. Patient data stays encrypted and access-controlled throughout.

### Non-goals (be upfront about this in your report/viva)
- This is **not** a replacement for a licensed physician — it's a decision-support aid.
- No ML model training is claimed — the reasoning engine is deterministic/rule-based (a real hospital pilot would need clinical validation before any ML claims anyway; keep the scope honest).

---

## 2. Actors

| Actor | Role |
|---|---|
| **Patient** | Registers, submits symptoms, views own history & recommendations (read-only) |
| **Doctor** | Reviews patient data + engine suggestions, confirms/overrides diagnosis, prescribes treatment |
| **Lab Technician** | Enters/uploads test results against a patient record |
| **Admin** | Manages users, roles, and the clinical rule knowledge-base |

---

## 3. High-Level Architecture

```mermaid
graph TB
    subgraph Client["Presentation Layer"]
        JSP_P[Patient Portal - JSP]
        JSP_D[Doctor Dashboard - JSP]
        JSP_A[Admin Console - JSP]
    end

    subgraph Web["Web / Controller Layer"]
        FC[Front Controller Servlet]
        MVC[Spring MVC Controllers]
        SEC[Spring Security Filter Chain]
    end

    subgraph Service["Service Layer - Spring Beans"]
        PS[PatientService]
        CS[ConsultationService]
        DSE[Decision Support Engine]
        TS[TreatmentService]
        AS[AuditService]
    end

    subgraph Data["Data Access Layer - JDBC"]
        PDAO[PatientDAO]
        SDAO[SymptomDAO]
        TDAO[TestResultDAO]
        DDAO[DiagnosisDAO]
        RDAO[RuleDAO]
    end

    subgraph DB["Database"]
        MYSQL[(MySQL / PostgreSQL)]
    end

    JSP_P --> FC
    JSP_D --> MVC
    JSP_A --> MVC
    FC --> SEC
    MVC --> SEC
    SEC --> PS
    SEC --> CS
    SEC --> TS

    CS --> DSE
    DSE --> RDAO
    PS --> PDAO
    CS --> SDAO
    CS --> TDAO
    TS --> DDAO
    CS --> AS

    PDAO --> MYSQL
    SDAO --> MYSQL
    TDAO --> MYSQL
    DDAO --> MYSQL
    RDAO --> MYSQL
    AS --> MYSQL

    classDef presentation fill:#DCEEFB,stroke:#1E6FA8,stroke-width:2px,color:#0B2E4A
    classDef web fill:#E4F5E1,stroke:#2E8B57,stroke-width:2px,color:#0F3D24
    classDef service fill:#FDECC8,stroke:#D98E04,stroke-width:2px,color:#4A3400
    classDef data fill:#F3E1F5,stroke:#8E44AD,stroke-width:2px,color:#3B1547
    classDef db fill:#FDE2E1,stroke:#C0392B,stroke-width:2px,color:#4A1410

    class JSP_P,JSP_D,JSP_A presentation
    class FC,MVC,SEC web
    class PS,CS,DSE,TS,AS service
    class PDAO,SDAO,TDAO,DDAO,RDAO data
    class MYSQL db
```

**Why this layering matters for the viva:** Servlets act as the front-controller for legacy/simple flows (login, file upload), Spring MVC handles the richer request routing (`@Controller`, `@Service`, `@Repository`), and JDBC (either raw `java.sql` or Spring's `JdbcTemplate`) is the actual data-access mechanism underneath JPA-less DAOs — this is exactly what your project brief asks for (Servlets + JSP + JDBC + Spring together, not one replacing the other).

---

## 4. Core Modules

1. **Auth & Access Control** — Spring Security; role-based (`PATIENT`, `DOCTOR`, `LAB_TECH`, `ADMIN`); session-based auth via `HttpSession`, backed by encrypted credentials.
2. **Patient Management** — demographics, medical history (allergies, chronic conditions, past diagnoses).
3. **Symptom Intake** — structured symptom entry (not free text) mapped to a standard symptom taxonomy — this structure is what makes rule matching possible.
4. **Test Result Management** — lab technician enters results; flagged against normal ranges automatically.
5. **Decision Support Engine (DSE)** — the core: rule-based inference over symptoms + history + test results → ranked diagnosis suggestions with confidence + supporting evidence per suggestion.
6. **Treatment Recommendation** — maps confirmed diagnosis → standard treatment protocol templates (doctor edits before finalizing).
7. **Audit & Privacy** — every read/write to patient data logged (who, what, when); PII fields encrypted at rest (AES) via a `@Convert`-style JDBC wrapper or manual encryption in the DAO layer.

---

## 5. Decision Support Engine — Design

Keep this deterministic and explainable (a rules/weighted-scoring engine, not a black box) — this is what makes it defensible in a viva when someone asks "is this real AI?"

```mermaid
flowchart TD
    A[Collect Input: Symptoms + History + Test Results] --> B[Normalize Input to Standard Codes]
    B --> C{Match against Rule Knowledge-Base}
    C --> D[Rule 1: Symptom-Condition Weighted Match]
    C --> E[Rule 2: Test Result Threshold Match]
    C --> F[Rule 3: History Risk Factor Match]
    D --> G[Aggregate Scores per Candidate Condition]
    E --> G
    F --> G
    G --> H[Rank Conditions by Confidence Score]
    H --> I{Confidence above safety threshold?}
    I -->|Yes| J[Return Top-N Suggestions + Evidence Trail]
    I -->|No| K[Return 'Insufficient Data - Recommend Further Tests']
    J --> L[Doctor Reviews in Dashboard]
    K --> L
    L --> M[Doctor Accepts / Overrides / Requests More Tests]
    M --> N[Final Diagnosis Logged with Doctor Signature]

    classDef input fill:#DCEEFB,stroke:#1E6FA8,stroke-width:2px,color:#0B2E4A
    classDef rule fill:#E4F5E1,stroke:#2E8B57,stroke-width:2px,color:#0F3D24
    classDef decision fill:#FDECC8,stroke:#D98E04,stroke-width:2px,color:#4A3400
    classDef outcome fill:#F3E1F5,stroke:#8E44AD,stroke-width:2px,color:#3B1547
    classDef human fill:#FDE2E1,stroke:#C0392B,stroke-width:2px,color:#4A1410

    class A,B input
    class C,D,E,F,G rule
    class H,I decision
    class J,K outcome
    class L,M,N human
```

**Rule structure (stored in DB, editable by Admin — this is your "knowledge base management" feature):**

| Rule Component | Example |
|---|---|
| Condition | Type 2 Diabetes |
| Symptom weights | Frequent urination (0.3), Excessive thirst (0.3), Fatigue (0.15) |
| Test thresholds | Fasting glucose > 126 mg/dL (0.4 weight, hard flag) |
| Risk factors | Family history diabetes (+0.1), BMI > 30 (+0.1) |
| Safety threshold | Suggestion only surfaces if aggregate score ≥ 0.6 |

This gives you a genuinely explainable system: every suggestion the doctor sees comes with *why* — which symptoms, which test result, which history factor contributed, and by how much.

---

## 6. End-to-End Data Flow (Sequence Diagram)

```mermaid
sequenceDiagram
    actor Patient
    participant JSP as JSP Portal
    participant Servlet as Front Servlet
    participant Ctrl as Spring Controller
    participant CS as ConsultationService
    participant DSE as Decision Support Engine
    participant DAO as JDBC DAO Layer
    participant DB as Database
    actor Doctor

    Patient->>JSP: Submit symptoms form
    JSP->>Servlet: POST /consultation/submit
    Servlet->>Ctrl: forward request
    Ctrl->>CS: createConsultation(patientId, symptoms)
    CS->>DAO: fetch patient history + latest test results
    DAO->>DB: SELECT history, test_results
    DB-->>DAO: rows
    DAO-->>CS: PatientContext object
    CS->>DSE: evaluate(symptoms, history, testResults)
    DSE->>DAO: fetch active rules
    DAO->>DB: SELECT rules WHERE active = true
    DB-->>DAO: rule set
    DAO-->>DSE: rules
    DSE-->>CS: RankedDiagnosisList (with evidence + confidence)
    CS->>DAO: persist consultation + suggestions
    DAO->>DB: INSERT consultation, diagnosis_suggestions
    CS-->>Ctrl: consultationId
    Ctrl-->>JSP: redirect to confirmation page
    JSP-->>Patient: "Submitted - awaiting doctor review"

    Doctor->>JSP: Open Doctor Dashboard
    JSP->>Ctrl: GET /dashboard/pending
    Ctrl->>CS: getPendingConsultations(doctorId)
    CS->>DAO: fetch pending + suggestions + evidence
    DAO->>DB: SELECT joined consultation data
    DB-->>DAO: rows
    DAO-->>CS: consultation list
    CS-->>Ctrl: DTOs
    Ctrl-->>JSP: render dashboard
    Doctor->>JSP: Confirm/Override diagnosis + add treatment
    JSP->>Ctrl: POST /diagnosis/finalize
    Ctrl->>CS: finalizeDiagnosis(consultationId, doctorDecision)
    CS->>DAO: update diagnosis, insert audit log
    DAO->>DB: UPDATE diagnosis, INSERT audit_log
    CS-->>Ctrl: success
    Ctrl-->>JSP: "Diagnosis finalized"
```

---

## 7. Database Schema (ER Diagram)

```mermaid
erDiagram
    USER ||--o{ PATIENT_PROFILE : has
    USER ||--o{ AUDIT_LOG : generates
    PATIENT_PROFILE ||--o{ MEDICAL_HISTORY : has
    PATIENT_PROFILE ||--o{ CONSULTATION : submits
    CONSULTATION ||--o{ SYMPTOM_ENTRY : includes
    CONSULTATION ||--o{ TEST_RESULT : references
    CONSULTATION ||--o{ DIAGNOSIS_SUGGESTION : produces
    DIAGNOSIS_SUGGESTION ||--o{ EVIDENCE_ITEM : "supported by"
    CONSULTATION ||--o| FINAL_DIAGNOSIS : resolves_to
    FINAL_DIAGNOSIS ||--o| TREATMENT_PLAN : generates
    RULE ||--o{ EVIDENCE_ITEM : "referenced in"

    USER {
        int user_id PK
        string username
        string password_hash
        string role
        string encrypted_email
    }
    PATIENT_PROFILE {
        int patient_id PK
        int user_id FK
        string encrypted_name
        date dob
        string encrypted_contact
    }
    MEDICAL_HISTORY {
        int history_id PK
        int patient_id FK
        string condition_code
        string notes
        date recorded_on
    }
    CONSULTATION {
        int consultation_id PK
        int patient_id FK
        int assigned_doctor_id FK
        datetime submitted_at
        string status
    }
    SYMPTOM_ENTRY {
        int symptom_id PK
        int consultation_id FK
        string symptom_code
        int severity
    }
    TEST_RESULT {
        int test_id PK
        int consultation_id FK
        string test_code
        float value
        string unit
        boolean flagged
    }
    RULE {
        int rule_id PK
        string condition_code
        string rule_definition
        float threshold
        boolean active
    }
    DIAGNOSIS_SUGGESTION {
        int suggestion_id PK
        int consultation_id FK
        string condition_code
        float confidence_score
        int rank
    }
    EVIDENCE_ITEM {
        int evidence_id PK
        int suggestion_id FK
        int rule_id FK
        string description
        float contribution_weight
    }
    FINAL_DIAGNOSIS {
        int diagnosis_id PK
        int consultation_id FK
        string condition_code
        int confirmed_by_doctor_id FK
        string decision_type
        datetime decided_at
    }
    TREATMENT_PLAN {
        int plan_id PK
        int diagnosis_id FK
        string protocol_text
        string prescription
    }
    AUDIT_LOG {
        int log_id PK
        int user_id FK
        string action
        string entity_affected
        datetime timestamp
    }
```

---

## 8. Security & Privacy Design

| Concern | Approach |
|---|---|
| Authentication | Spring Security, BCrypt password hashing |
| Authorization | Role-based access control at controller + service layer (`@PreAuthorize`) |
| PII protection | AES encryption for name/contact fields, applied/decrypted in the DAO layer before/after JDBC calls |
| Data-in-transit | HTTPS (self-signed cert acceptable for demo) |
| Audit trail | Every read/write on patient records logged to `AUDIT_LOG` — required for any real healthcare system (this maps to real-world HIPAA-style accountability, good talking point in viva) |
| Session security | Server-side sessions, session timeout, CSRF tokens on all JSP forms |

---

## 9. Key Endpoints (Servlet + Spring MVC mix)

| Endpoint | Layer | Purpose |
|---|---|---|
| `/auth/login` | Servlet | Session-based login |
| `/patient/register` | Spring MVC | Patient signup |
| `/consultation/submit` | Spring MVC | Submit symptoms + trigger DSE |
| `/lab/upload-result` | Servlet (multipart) | Lab tech uploads/enters test results |
| `/dashboard/pending` | Spring MVC | Doctor's pending review queue |
| `/diagnosis/finalize` | Spring MVC | Doctor confirms/overrides diagnosis |
| `/admin/rules` | Spring MVC | Admin manages rule knowledge-base |
| `/audit/logs` | Spring MVC | Admin views audit trail |

---

## 10. Suggested Package Structure

```
medassist/
├── src/main/java/com/medassist/
│   ├── controller/        (Spring MVC controllers)
│   ├── servlet/           (Front controller + upload servlets)
│   ├── service/           (PatientService, ConsultationService, DecisionSupportEngine, TreatmentService, AuditService)
│   ├── dao/               (JDBC DAOs — PatientDAO, SymptomDAO, TestResultDAO, RuleDAO, DiagnosisDAO)
│   ├── model/             (POJOs: Patient, Consultation, Symptom, TestResult, Rule, Diagnosis)
│   ├── security/          (Spring Security config, encryption utils)
│   └── config/            (Spring config, DataSource/JDBC config)
├── src/main/webapp/
│   ├── WEB-INF/jsp/       (patient-portal, doctor-dashboard, admin-console)
│   └── WEB-INF/web.xml
└── src/main/resources/
    └── application properties / spring context XML
```

---

## 11. Why This Is Defensible as a "Real Product Workflow" (viva talking points)

- **Explainability over black-box AI** — every suggestion shows its evidence trail; you can defend this against "is this just hardcoded if-else?" by pointing to the weighted rule-scoring + threshold-based confidence system, which is how real early clinical decision support tools (e.g., rule-based CDSS before ML-era) actually worked.
- **Human-in-the-loop by design** — the system never auto-diagnoses; doctor confirmation is mandatory. This is the correct, responsible pattern for any real healthcare software.
- **Audit trail** — non-negotiable in any real healthcare system; shows you understand compliance concerns beyond just "make the CRUD work."
- **Layered architecture matching the mandated stack** — Servlets, JSP, Spring, JDBC each doing the job they're actually good at, not just present for the sake of the syllabus checklist.

---

## 12. Future Enhancements (mention in report, don't over-promise)

- Swap the rule engine for a trained ML classifier once labeled clinical data is available (out of scope for this project).
- Add a notification service (email/SMS) for patients when diagnosis is finalized.
- Multi-facility support (hospital_id scoping across all tables).
