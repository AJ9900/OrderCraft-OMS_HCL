# Deploy OrderCraft on Render and Vercel

The backend runs as a Docker web service on Render. The Angular frontend is built and served by Vercel. The Render Blueprint is at the repository root (`render.yaml`); the Vercel config is `frontend/vercel.json`.

## Database prerequisite

Render does not provide a managed MySQL service. Create a reachable MySQL 8 database with an external provider before deploying the backend. Configure its network access to allow the Render service to connect. Use the provider's JDBC URL, database name, username, and password. The application runs Hibernate with `ddl-auto=update`, then seeds its initial demo data on first startup.

## Deploy the backend to Render

1. Push this repository to GitHub and create a new Render Blueprint from the repository root. Render reads `render.yaml` and creates the `ordercraft-api` Docker web service.
2. When prompted for values, provide:
   - `DB_URL`: full JDBC URL, for example `jdbc:mysql://HOST:3306/DATABASE?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true`.
   - `DB_USERNAME` and `DB_PASSWORD`: credentials for that MySQL database.
   - `ADMIN_PASSWORD`: a new, strong password. The app also uses this to seed the demo role accounts.
   - `CORS_ALLOWED_ORIGINS`: the exact Vercel production origin, such as `https://your-project.vercel.app` (no trailing slash). Add exact preview origins as comma-separated entries if previews must call the API.
3. Render generates `JWT_SECRET` automatically. Keep it private and stable between deploys.
4. Wait for the service health check at `/api/health` to pass. Its URL will be `https://ordercraft-api.onrender.com/api/health` when the generated Render hostname matches the Blueprint service name.

If Render assigns a different hostname, update the API base URL in `frontend/src/environments/environment.prod.ts` and the CORS origin in Render, then redeploy both services. CORS is exact-origin; wildcard subdomains are not enabled.

## Deploy the frontend to Vercel

1. Import the same GitHub repository into Vercel.
2. Set **Root Directory** to `frontend`.
3. Let `frontend/vercel.json` configure the production build command, output directory (`dist/frontend/browser`), and SPA fallback. No separate Vercel output override is needed.
4. Deploy. Direct navigation to Angular routes is handled by the rewrite to `index.html`.

The production API URL is compiled into Angular from `frontend/src/environments/environment.prod.ts`. It currently targets `https://ordercraft-api.onrender.com/api`; keep it in sync with the backend's actual Render hostname.

## Local backend configuration

The checked-in `application.properties` contains safe local defaults and no database password. Set `DB_PASSWORD` in the shell before running `backend/mvnw.cmd spring-boot:run` if your local MySQL account requires one. The same environment-variable names used in Render can override the local URL, username, admin password, and JWT secret.

Do not commit production credentials. If a database password that was previously present in Git is real or reused, rotate it with the database provider before deployment.
