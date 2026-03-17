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


D!$$&3949acq

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_service_checker

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@45.201.196.19:~/sv_service_checker

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_service_checker/

sudo nano /etc/systemd/system/service_checker.service
sudo systemctl daemon-reload
sudo systemctl stop service_checker
sudo systemctl start service_checker
sudo systemctl enable service_checker
sudo systemctl status service_checker.service
sudo systemctl status -f service_checker.service
journalctl -u service_checker.service -f


server.port=8084

[Unit]
Description=SV Safety Checklists Application
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


# Repairman Request
server.port=8083
sudo nano /etc/systemd/system/sv_repairman.service


[Unit]
Description=SV Safety Checklists Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_repairman
ExecStart=/usr/bin/java -jar /home/deverloper/sv_repairman/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_repairman/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target


sudo systemctl daemon-reload
sudo systemctl enable sv_repairman
sudo systemctl restart sv_repairman
sudo systemctl status sv_repairman

sudo journalctl -u sv_repairman.service -f

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_repairman/
scp -P 22236 ./sv_repairman.sql deverloper@192.168.1.249:~/sv_repairman/

#import
mysql -u root -p sv_repairman < ~/sv_repairman/sv_repairman.sql
