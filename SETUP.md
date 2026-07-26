# Team Setup Guide — Get the Project Running

**Group 8 — SAMS (Student Academic Records & Attendance Management System)**

Follow these steps in order to run the project on your own computer after cloning
it from GitHub. Takes about 20–30 minutes the first time.

---

## 1. Install the tools you need (once)

| Tool | Version | Download |
|---|---|---|
| **JDK 21** | 21 (LTS) | https://adoptium.net/temurin/releases/?version=21 |
| **Node.js** | 18 or newer | https://nodejs.org (LTS) |
| **PostgreSQL** | 14 or newer (we used 18) | https://www.postgresql.org/download |
| **Git** | any recent | https://git-scm.com/downloads |

> During PostgreSQL install it asks for a **password for the `postgres` user** —
> **write it down**, you will need it below. Keep the default port **5432**.

Check they installed correctly (open a new terminal):

```bash
java -version      # should say 21
node -v            # should say v18 or higher
git --version
```

---

## 2. Clone the project

```bash
git clone https://github.com/TShubka/SAMS-FullStack-Project.git
cd SAMS-FullStack-Project
```

---

## 3. Create the database

Create an empty database called `sams_db` (replace the path if your PostgreSQL
version differs):

**Windows**
```bash
"C:\Program Files\PostgreSQL\18\bin\createdb.exe" -U postgres -h localhost sams_db
```

**macOS / Linux**
```bash
createdb -U postgres -h localhost sams_db
```

It will ask for the postgres password you set during install. The tables are
created automatically the first time you run the backend — you don't create them
by hand.

---

## 4. Tell the backend your database password

The password is **never** stored in the project (for security). Set it as an
environment variable so the backend can read it.

**Windows (PowerShell) — permanent:**
```bash
setx DB_PASSWORD "your_postgres_password"
```
Then **close and reopen** the terminal so it takes effect.

**macOS / Linux — permanent:** add this line to `~/.bashrc` or `~/.zshrc`:
```bash
export DB_PASSWORD="your_postgres_password"
```
Then run `source ~/.bashrc` (or open a new terminal).

> If your database host, port, name or username are different from the defaults,
> you can also set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` the same way.
> Defaults: `localhost`, `5432`, `sams_db`, `postgres`.

---

## 5. Run the backend (Spring Boot)

From the project folder:

```bash
cd backend
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

Wait until you see **`Started SamsApplication`**. The API is now on
**http://localhost:8080**. Test it in a browser:
`http://localhost:8080/api/health` → should show `{"status":"UP", ...}`.

> Maven is **not** required — the project ships `mvnw` (the Maven Wrapper).
> The first run downloads dependencies, so it takes a few minutes.

Leave this terminal running.

---

## 6. Run the frontend (React) — in a SECOND terminal

Open a **new** terminal (keep the backend running in the first one):

```bash
cd SAMS-FullStack-Project/frontend
npm install          # first time only — downloads packages
npm run dev
```

Open the address it prints — **http://localhost:5173** — in your browser.

---

## 7. Log in and test

Use any demo account. Password for all: **`Password123`** (log in by **username**).

| Role | Username |
|---|---|
| Administrator | `admin` |
| Teacher | `t.smith` |
| Student | `cs.student1` |

The demo data (students, courses, attendance, marks) is created automatically the
first time the backend runs.

---

## 8. Daily workflow (Git) — for the team

Always pull before you start, and work on your own branch.

```bash
git checkout develop
git pull origin develop            # get the latest team work

git checkout feature/your-branch   # your area's branch
# ... make your changes ...

git add .
git commit -m "clear message about what you did"
git push origin feature/your-branch
```

Feature branches (one per member's area):
- `feature/auth-security` — Shuaib
- `feature/student-course` — Dahir
- `feature/attendance-marks` — Aisha
- `feature/dashboard-reports` — Idiris

When your feature is done and tested, merge it into `develop` (open a Pull Request
on GitHub, or merge locally), and only merge `develop` into `main` for stable
releases.

---

## Common problems & fixes

| Problem | Cause | Fix |
|---|---|---|
| `password authentication failed for user "postgres"` | `DB_PASSWORD` wrong or not set | Re-do step 4; open a **new** terminal after `setx` |
| `Connection to localhost:5432 refused` | PostgreSQL not running | Start the PostgreSQL service (Windows: Services app; mac: `brew services start postgresql`) |
| `database "sams_db" does not exist` | Skipped step 3 | Run the `createdb` command in step 3 |
| Backend starts on wrong Java | Multiple JDKs installed | Set `JAVA_HOME` to your JDK 21 folder before running |
| Frontend shows "Cannot reach the server" | Backend not running | Make sure step 5 is running on port 8080 |
| Port 8080 or 5173 already in use | Another app is using it | Close the other app, or change the port |
| `npm run dev` fails | `npm install` not done | Run `npm install` first (step 6) |

---

## Quick reference

```
Backend:   cd backend  → mvnw.cmd spring-boot:run   → http://localhost:8080
Frontend:  cd frontend → npm run dev                → http://localhost:5173
Login:     admin / t.smith / cs.student1   password: Password123
Database:  sams_db on localhost:5432 (user postgres)
```

If you get stuck, check the main [README.md](README.md) or ask the team.
