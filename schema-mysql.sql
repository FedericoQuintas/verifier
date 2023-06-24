CREATE TABLE IF NOT EXISTS order_events (
id bigint NOT NULL,
side VARCHAR(5),
quantity int,
price DECIMAL(10,2),
event_type VARCHAR(30),
event_time timestamp
);

CREATE INDEX index_id ON order_events (id);

update mysql.user set host = '%' where user='root';