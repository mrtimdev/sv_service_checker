
#sv_vehicle_maintenace

ssh -p 2236 deverloper@154.26.134.117
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

scp -P 2236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@154.26.134.117:~/sv_vehicle_maintenace/
scp -P 2236 ./messages_km.properties deverloper@154.26.134.117:~/sv_vehicle_maintenace/
scp -P 2236 ./messages.properties deverloper@154.26.134.117:~/sv_vehicle_maintenace/

scp -P 2236 ./vehicle_fuel_maintenace.sql deverloper@154.26.134.117:~/sv_vehicle_maintenace/
#import
mysql -u root -p sv_vehicle_maintenace < ~/sv_vehicle_maintenace/vehicle_fuel_maintenace.sql

#export
mysqldump -u root -p sv_vehicle_maintenace > ~/backup/sv_vehicle_maintenace_backup_Dec_18_2025.sql;

#save to local
scp -P 2236 deverloper@154.26.134.117:~/backup/sv_vehicle_maintenace_backup_Dec_18_2025.sql;

sudo nano /etc/systemd/system/sv_vehicle_maintenace.service

sudo systemctl daemon-reload
sudo systemctl start sv_vehicle_maintenace
sudo systemctl stop sv_vehicle_maintenace
sudo systemctl enable sv_vehicle_maintenace
sudo systemctl restart sv_vehicle_maintenace
sudo systemctl status sv_vehicle_maintenace

mysqldump -u root -p sv_vehicle_maintenace > ~/backup/sv_vehicle_maintenace_backup_feb04.sql;

scp -P 2236 deverloper@154.26.134.117:~/backup/sv_vehicle_maintenace_backup_feb04.sql sv_vehicle_maintenace_backup_feb04.sql

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
sudo systemctl restart sv_repairman
sudo systemctl status sv_repairman

sudo journalctl -u sv_repairman.service -f

scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@192.168.1.249:~/sv_repairman/
scp -P 22236 ./sv_repairman.sql deverloper@192.168.1.249:~/sv_repairman/

#import
mysql -u root -p sv_repairman < ~/sv_repairman/sv_repairman.sql


CREATE INDEX idx_destinations_date ON destinations(date);
CREATE INDEX idx_destinations_status ON destinations(status);
CREATE INDEX idx_destinations_truck ON destinations(truck_id);





# Scale and Parking Application
sudo nano /etc/systemd/system/sv_scale_parking.service


scp -P 22236 ./timdev-0.0.1-SNAPSHOT.jar deverloper@154.26.134.117:~/sv_scale_parking/

mysql -u root -p sv_scale_parking < ~/sv_scale_parking/sv_scale_and_parking.sql

sudo nano /etc/systemd/system/sv_scale_parking.service

sudo systemctl daemon-reload
sudo systemctl enable sv_scale_parking
sudo systemctl restart sv_scale_parking
sudo systemctl start sv_scale_parking
sudo systemctl stop sv_scale_parking
sudo systemctl status sv_scale_parking

sudo journalctl -u sv_scale_parking.service -f

[Unit]
Description=SV sv_scale_parking Application
After=syslog.target

[Service]
User=deverloper
WorkingDirectory=/home/deverloper/sv_scale_parking
ExecStart=/usr/bin/java -jar /home/deverloper/sv_scale_parking/timdev-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.location=file:/home/deverloper/sv_scale_parking/application-prod.properties
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target