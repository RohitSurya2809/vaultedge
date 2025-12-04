-- V2: create auth_users table linked to customers (UUID as BINARY(16))
CREATE TABLE auth_users (
  id BINARY(16) NOT NULL PRIMARY KEY,
  customer_id BINARY(16) NOT NULL,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  roles VARCHAR(255) DEFAULT 'ROLE_USER',
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_auth_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
