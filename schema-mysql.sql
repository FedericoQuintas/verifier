CREATE TABLE IF NOT EXISTS order_events (
id bigint NOT NULL PRIMARY KEY,
side VARCHAR(5),
quantity int,
price DECIMAL(10,2),
event_type VARCHAR(30),
event_time timestamp
);

update mysql.user set host = '%' where user='root';