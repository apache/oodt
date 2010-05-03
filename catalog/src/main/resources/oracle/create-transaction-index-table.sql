
CREATE TABLE transactions
(
  transaction_id varchar2(256) NOT NULL,
  transaction_date varchar2(256) NOT NULL
);

CREATE TABLE transaction_terms
(
  transaction_id varchar2(256) NOT NULL,
  bucket_name varchar2(256) NOT NULL,
  term_name varchar2(256) NOT NULL,
  term_value varchar2(1000) NOT NULL
);

CREATE INDEX transactions_transid_idx ON transactions(transaction_id);  
CREATE INDEX transaction_terms_transid_idx ON transaction_terms(transaction_id);  