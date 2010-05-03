DROP TABLE transactions IF EXISTS;
DROP TABLE transaction_terms IF EXISTS;

CREATE TABLE transactions
(
  transaction_id varchar(256) NOT NULL,
  transaction_date varchar(256) NOT NULL
);

CREATE TABLE transaction_terms
(
  transaction_id varchar(256) NOT NULL,
  bucket_name varchar(256) NOT NULL,
  term_name varchar(256) NOT NULL,
  term_value varchar(1000) NOT NULL
);

