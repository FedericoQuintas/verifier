CREATE TABLE IF NOT EXISTS order_events (
id bigint NOT NULL,
side VARCHAR(5) NOT NULL,
quantity int NOT NULL,
price DECIMAL(10,2) NOT NULL,
event_type VARCHAR(30) NOT NULL,
event_time timestamp NOT NULL
);

CREATE INDEX index_id ON order_events (id);

update mysql.user set host = '%' where user='root';