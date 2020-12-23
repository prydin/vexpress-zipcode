#!/bin/bash
mkdir vexpress-zipcode
chown $1 vexpress-zipcode
cd vexpress-zipcode
mv /tmp/application.properties
wget --auth-no-challenge --user=$2 --password=$3 $4/artifact/build/libs/zipcode-$5.jar -o zipcode.jar
mv /tmp/vexpress-zipcode.service /etc/systemd/system
chmod 664 /etc/systemd/system/vexpress-zipcode.service
systemctl enable vexpress-zipcode
