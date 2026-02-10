# Filestorage (Spring WebFlux + JPA + JWT)

## Старт (Docker Compose)
```bash
./gradlew clean bootJar
docker compose up --build
```


## JWT (демо)
Эндпоинт выдачи токена:

```bash
curl -X POST http://localhost:8081/auth/token   -H 'Content-Type: application/json'   -d '{"username":"alice","role":"USER"}'
```

## Загрузка файла
```bash
curl -X POST http://localhost:8081/files   -H "Authorization: Bearer $TOKEN"   -F "file=@./some-file.txt"
```
