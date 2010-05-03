CREATE TABLE YourProductTypeName_metadata
(
  product_id int NOT NULL,
  element_id varchar(1000) NOT NULL,
  metadata_value varchar(2500) NOT NULL
)

CREATE TABLE YourProductTypeName_reference
(
  product_id int NOT NULL,
  product_orig_reference varchar(2000) NOT NULL,
  product_datastore_reference varchar(2000), 
  product_reference_filesize int NOT NULL,
  product_reference_mimetype varchar(50)
)


