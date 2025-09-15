
#sv_service_checker

server.port=8084

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


# Repairman Request
server.port=8083
sudo nano /etc/systemd/system/sv_repairman.service

sudo nano /etc/systemd/system/sv_truck_fats.service

sudo systemctl daemon-reload
sudo systemctl enable sv_truck_fats
sudo systemctl restart sv_truck_fats
sudo systemctl status sv_truck_fats

sudo journalctl -u sv_truck_fats.service -f

[Unit]
Description=SV Services Checker Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_truck_fats
ExecStart=/usr/bin/java -jar /home/deverloper/sv_truck_fats/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_truck_fats/application-prod.properties
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
sudo systemctl restart sv_repairman
sudo systemctl status sv_repairman

sudo journalctl -u sv_repairman.service -f

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_repairman/
scp -P 22236 ./sv_repairman.sql deverloper@192.168.1.249:~/sv_repairman/

#import
mysql -u root -p sv_repairman < ~/sv_repairman/sv_repairman.sql
