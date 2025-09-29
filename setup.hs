[Unit]
Description=Spring Boot - sv-service-checker
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_service_checker
ExecStart=/usr/bin/java -jar /home/deverloper/sv_service_checker/timdev-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=sv-service-checker

[Install]
WantedBy=multi-user.target




scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_service_checker

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@45.201.196.19:~/sv_service_checker


[Unit]
Description=SV Services Checker Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_service_checker
ExecStart=/usr/bin/java -jar /home/deverloper/sv_service_checker/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_service_checker/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target