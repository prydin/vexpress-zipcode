spring.jpa.hibernate.ddl-auto=update
spring.datasource.platform=postgres
spring.jpa.database=POSTGRESQL
spring.datasource.url=jdbc:postgresql://$( -> env.dbIp}/zipcodes
spring.datasource.username=$( -> env.dbUser}
spring.datasource.password=${ -> env.dbPassword}