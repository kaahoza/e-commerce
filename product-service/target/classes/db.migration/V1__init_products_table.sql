CREATE TABLE products(
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description Text,
    category VARCHAR(100) NOT NULL ,
    price NUMERIC(12, 2),
    stock_quantity INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIME
);



INSERT INTO  products (sku, name, description, category, price, stock_quantity)
VALUES ('PROD-ABC-01', 'Enterprise Cloud Router', 'High performance networking gear', 'gadget',4500.00, 15)