docker compose --env-file .env up -d postgres

mvn spring-boot:run

http://localhost:8080/swagger-ui.html

потом находите POST /api/auth/login, там Try it out, вниз вставляете:
{
  "email": "admin@match.local",
  "password": "Admin123!ChangeMe"
}
нажимаете Execute, копируете access token и в самом верху в authorize вставляете его
дальше работаете со сваггером
