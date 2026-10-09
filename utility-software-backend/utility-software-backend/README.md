# Utility Software - Backend (Spring Boot, port 8081)

## Run
1. MySQL chalu rakho, `application.properties` me username/password change karo.
2. `mvn spring-boot:run`

## APIs
| Method | URL | Kaam |
|---|---|---|
| POST | /api/request/send | Request Builder -> Send Request |
| GET | /api/history | Request History (latest 10) |
| GET | /api/history/{id} | Ek history item |
| DELETE | /api/history/{id} | Ek item delete |
| DELETE | /api/history | Saari history clear |
| GET | /api/meters | Real Time Data View |
| POST | /api/meters/refresh | Refresh button |

## Sample
curl -X POST http://localhost:8081/api/request/send -H "Content-Type: application/json" \
 -d '{"messageId":"device/naineet/res","opType":"READ","meterId":2,"ipAddress":"192.168.1.100:100"}'
