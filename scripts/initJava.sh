#/bin/bash

# Wait for Java to be installed
until [ -f /tmp/jdk-14-installed ]
do
     sleep 10
done

curl -fL https://getcli.jfrog.io | sh
export JFROG_CLI_OFFER_CONFIG=false
./jfrog rt dl --user admin --apikey ${input.artifactory_apikey} --url ${input.artifactory_url} generic-local/zipcode-*.jar

cat > application.properties << EOF
spring.jpa.hibernate.ddl-auto=update
spring.datasource.platform=postgres
spring.jpa.database=POSTGRESQL
spring.datasource.url=jdbc:postgresql://$DBSERVER/zipcodes
spring.datasource.username=$DBUSER
spring.datasource.password=$DBPASSWORD
EOF

nohup java -jar zipcode-*.jar &