-- Create the database
CREATE DATABASE ecommerce;

-- Create the user with password
CREATE USER 'ecommerce'@'localhost' IDENTIFIED BY 'ashutosh';

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce'@'localhost';

-- Select the database
USE ecommerce;

-- Create the customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100)
);


CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    price DECIMAL(10, 2),
    quantity INT
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    product_id INT,
    quantity INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
SELECT * FROM products WHERE name = 'apple';

SELECT * FROM customers;
SELECT * FROM orders;
SELECT * FROM products;





