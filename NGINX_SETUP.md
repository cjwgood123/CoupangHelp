# Nginx 리버스 프록시 설정 가이드

## 1. Nginx 설치 (Ubuntu)

```bash
sudo apt update
sudo apt install nginx -y
```

## 2. Nginx 설정 파일 생성

```bash
sudo nano /etc/nginx/sites-available/coupang
```

위 파일에 `nginx-config-example.conf` 내용을 복사하여 붙여넣기

## 3. 심볼릭 링크 생성 (사이트 활성화)

```bash
sudo ln -s /etc/nginx/sites-available/coupang /etc/nginx/sites-enabled/
```

## 4. 기본 설정 파일 제거 (선택사항)

```bash
sudo rm /etc/nginx/sites-enabled/default
```

## 5. Nginx 설정 테스트

```bash
sudo nginx -t
```

## 6. Nginx 재시작

```bash
sudo systemctl restart nginx
```

## 7. Nginx 자동 시작 설정

```bash
sudo systemctl enable nginx
```

## 8. 방화벽 설정 (UFW 사용 시)

```bash
sudo ufw allow 'Nginx Full'
# 또는
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp  # HTTPS 사용 시
```

## 9. Spring Boot 애플리케이션 실행

애플리케이션은 **9000 포트**로 실행:

```bash
java -jar cou-html-0.0.1-SNAPSHOT.jar
```

또는 systemd 서비스로 실행:

```bash
# /etc/systemd/system/coupang.service 파일 생성
sudo nano /etc/systemd/system/coupang.service
```

서비스 파일 내용:
```ini
[Unit]
Description=Coupang Data Analysis Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/cou-html-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

서비스 활성화:
```bash
sudo systemctl daemon-reload
sudo systemctl enable coupang
sudo systemctl start coupang
```

## 10. 확인

- 브라우저에서 `http://www.helpcoupang.com` 접속
- 또는 `http://서버IP` 접속

## 문제 해결

### Nginx 로그 확인
```bash
sudo tail -f /var/log/nginx/coupang-error.log
sudo tail -f /var/log/nginx/coupang-access.log
```

### Spring Boot 애플리케이션 로그 확인
```bash
sudo journalctl -u coupang -f
```

### 포트 확인
```bash
sudo netstat -tlnp | grep :9000
sudo netstat -tlnp | grep :80
```





