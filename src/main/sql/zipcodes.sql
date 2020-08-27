create table zipcodes(
    zip integer primary key,
    locality varchar(200),
    state varchar(2),
    lat decimal(12,8),
    long decimal(12,8));