
#sv_vehicle_maintenace

ssh -p 22 deverloper@154.26.134.117
D!$$&3949acq

server.port=8085

[Unit]
Description=SV Vehicle Fuel Maintenace Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_vehicle_maintenace
ExecStart=/usr/bin/java -jar /home/deverloper/sv_vehicle_maintenace/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_vehicle_maintenace/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target

Save and exit (Ctrl+O, Enter, Ctrl+X).

on Local Server ?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
Bong sasna

sudo nano /etc/systemd/system/sv_survey.service

sudo systemctl daemon-reload
sudo systemctl start sv_survey
sudo systemctl stop sv_survey
sudo systemctl enable sv_survey
sudo systemctl status sv_survey

sudo journalctl -u sv_survey.service -f

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@45.201.196.19:~/sv_survey/
scp -P 22236 ./messages_km.properties deverloper@45.201.196.19:~/sv_survey/
scp -P 22236 ./messages.properties deverloper@45.201.196.19:~/sv_survey/


[Unit]
Description=SV Survey Spring Boot Application
After=network.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_survey
ExecStart=/usr/bin/java -jar /home/deverloper/sv_survey/target/sv_survey-0.0.1-SNAPSHOT.jar --spring.config.location=file:/home/deverloper/sv_survey/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=file:/home/deverloper/sv_survey/sv_survey.log
StandardError=file:/home/deverloper/sv_survey/sv_survey.err

[Install]
WantedBy=multi-user.target




Bro chheang

scp ./timdev-0.0.1-SNAPSHOT.jar deverloper@154.26.134.117:~/sv_vehicle_maintenace/
scp ./messages_km.properties deverloper@154.26.134.117:~/sv_vehicle_maintenace/
scp ./messages.properties deverloper@154.26.134.117:~/sv_vehicle_maintenace/

scp ./vehicle_fuel_maintenace.sql deverloper@154.26.134.117:~/sv_vehicle_maintenace/
#import
mysql -u root -p sv_vehicle_maintenace < ~/sv_vehicle_maintenace/vehicle_fuel_maintenace.sql

sudo nano /etc/systemd/system/sv_vehicle_maintenace.service

sudo systemctl daemon-reload
sudo systemctl stop sv_vehicle_maintenace
sudo systemctl enable sv_vehicle_maintenace
sudo systemctl restart sv_vehicle_maintenace
sudo systemctl status sv_vehicle_maintenace

sudo journalctl -u sv_vehicle_maintenace.service -f

[Unit]
Description=SV Services Checker Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_vehicle_maintenace
ExecStart=/usr/bin/java -jar /home/deverloper/sv_vehicle_maintenace/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_vehicle_maintenace/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target


[Unit]
Description=SV Services Checker Application
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
sudo systemctl stop sv_repairman
sudo systemctl restart sv_repairman
sudo systemctl status sv_repairman

sudo journalctl -u sv_repairman.service -f

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_repairman/
scp -P 22236 ./sv_repairman.sql deverloper@192.168.1.249:~/sv_repairman/

#import
mysql -u root -p sv_repairman < ~/sv_repairman/sv_repairman.sql



sudo nano /etc/systemd/system/sv_service_checker.service