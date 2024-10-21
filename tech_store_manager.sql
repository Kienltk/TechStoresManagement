CREATE
DATABASE tech_store_manager;
USE
tech_store_manager;

-- PRIMARY KEY
CREATE TABLE products
(
    id             int auto_increment primary key,
    product_name   varchar(255),
    purchase_price decimal(10, 2),
    sale_price     decimal(10, 2),
    brand          varchar(50),
    img_address    varchar(255)
);

CREATE TABLE categories
(
    id            int auto_increment primary key,
    category_name varchar(100)
);

CREATE TABLE warehouses
(
    id      int auto_increment primary key,
    name    varchar(100),
    address varchar(255)
);

CREATE TABLE stores
(
    id      int auto_increment primary key,
    name    varchar(100),
    address varchar(255)
);

CREATE TABLE customers
(
    id           int auto_increment primary key,
    name         varchar(255),
    phone_number varchar(15)
);

-- FOREIGN KEY
-- PRODUCT
CREATE TABLE product_categories
(
    id_product  int,
    id_category int,
    foreign key (id_product) references products (id),
    foreign key (id_category) references categories (id)
);

-- WAREHOUSES
CREATE TABLE products_warehouse
(
    id_product   int,
    id_warehouse int,
    quantity     int,
    foreign key (id_warehouse) references warehouses (id),
    foreign key (id_product) references products (id)
);

CREATE TABLE import_warehouse
(
    id                  int auto_increment primary key,
    id_warehouse        int,
    name varchar(50),
    total               decimal(12,2),
    status varchar(50),
    product_import_date datetime,
    foreign key (id_warehouse) references warehouses (id)
);

CREATE TABLE import_warehouse_details
(
    id_import int,
    id_product int,
    quantity int,
    total decimal(12,2),
    foreign key (id_import) references import_warehouse (id),
    foreign key (id_product) references products (id)
);

-- STORES
CREATE TABLE store_financial
(
    id             int auto_increment primary key,
    id_store       int,
    date           date,
    turnover       decimal(12,2),
    capital        decimal(12, 2),
    profit         decimal(12, 2),
    foreign key (id_store) references stores (id)
);


CREATE TABLE products_store
(
    id_product int,
    id_store   int,
    quantity   int,
    foreign key (id_store) references stores (id),
    foreign key (id_product) references products (id)
);

CREATE TABLE import_store
(
    id            int auto_increment primary key,
    id_store      int,
    id_warehouse int,
    total         decimal(10, 2),
    received_date datetime,
    status varchar(50),
    foreign key (id_warehouse) references warehouses (id),
    foreign key (id_store) references stores (id)
);

CREATE TABLE import_store_details
(
    id_import   int,
    id_product  int,
    quantity    int,
    total       decimal(10, 2),
    foreign key (id_import) references import_store (id),
    foreign key (id_product) references products (id)
);

-- PERSON
CREATE TABLE role
(
    id   int auto_increment primary key,
    role varchar(50)
);

CREATE TABLE employees
(
    id           int auto_increment primary key,
    first_name   varchar(50),
    last_name    varchar(50),
    gender       boolean,
    dob          date,
    email        varchar(255),
    phone_number varchar(15),
    address      varchar(255),
    hire_date    datetime default current_timestamp,
    salary       decimal(10, 2),
    id_role      int,
    id_store     int,
    id_warehouse int,
    foreign key (id_role) references role (id),
    foreign key (id_store) references stores (id),
    foreign key (id_warehouse) references warehouses (id)
);


CREATE TABLE accounts
(
    id        int auto_increment primary key,
    username  varchar(50),
    password  varchar(255),
    id_person int,
    foreign key (id_person) references employees (id)
);

-- ORDERS
CREATE TABLE receipts
(
    id            int auto_increment primary key,
    id_customer   int,
    id_store      int,
    id_cashier int,
    total         decimal(10, 2),
    profit        decimal(10, 2),
    purchase_date datetime,
    foreign key (id_cashier) references employees (id),
    foreign key (id_customer) references customers (id),
    foreign key (id_store) references stores (id)
);

CREATE TABLE products_receipt
(
    id_receipt  int,
    id_product  int,
    quantity    int,
    total_amount decimal(10, 2),
    profit      decimal(10, 2),
    foreign key (id_receipt) references receipts (id),
    foreign key (id_product) references products (id)
);

-- ENTERPRISE 
CREATE TABLE business_financial
(
    id             int auto_increment primary key,
    date           date,
    turnover       decimal(12,2),
    capital        decimal(12, 2),
    profit         decimal(12, 2)
);


-- DATA DEFAULT
INSERT INTO products (product_name, purchase_price, sale_price, brand, img_address)
VALUES ('iPhone 15 Pro', 999.99, 1199.99, 'Apple', 'iphone15pro.jpg'),
       ('MacBook Air M2', 899.99, 1099.99, 'Apple', 'macbookairm2.jpg'),
       ('Apple Watch Series 9', 399.99, 499.99, 'Apple', 'applewatch9.jpg'),
       ('AirPods Pro 2', 199.99, 249.99, 'Apple', 'airpodspro2.jpg'),
       ('Samsung Galaxy S23', 799.99, 999.99, 'Samsung', 'galaxys23.jpg'),
       ('Samsung Galaxy Tab S9', 599.99, 749.99, 'Samsung', 'galaxytabs9.jpg'),
       ('Samsung Galaxy Watch 6', 349.99, 399.99, 'Samsung', 'galaxywatch6.jpg'),
       ('Samsung Galaxy Buds 2', 99.99, 149.99, 'Samsung', 'galaxybuds2.jpg'),
       ('Xiaomi Mi 13', 599.99, 699.99, 'Xiaomi', 'xiaomi-13.jpg'),
       ('Xiaomi Pad 6', 349.99, 449.99, 'Xiaomi', 'xiaomimipad6.jpg'),
       ('Xiaomi Mi Band 8', 49.99, 59.99, 'Xiaomi', 'miband8.jpg'),
       ('ASUS ROG Phone 7', 899.99, 999.99, 'ASUS', 'rogphone7.jpg'),
       ('ASUS ZenBook 14', 749.99, 899.99, 'ASUS', 'zenbook14.jpg'),
       ('ASUS TUF Gaming Headset', 79.99, 99.99, 'ASUS', 'tufgamingheadset.jpg'),
       ('Dell XPS 13', 999.99, 1199.99, 'Dell', 'xps13.jpg'),
       ('Dell Alienware M15', 1199.99, 1499.99, 'Dell', 'alienwarem15.jpg'),
       ('Dell Inspiron 14', 549.99, 649.99, 'Dell', 'inspiron14.jpg'),
       ('MSI Raider GE78', 1799.99, 1999.99, 'MSI', 'raiderge78.jpg'),
       ('MSI Prestige 14', 899.99, 1099.99, 'MSI', 'prestige14.jpg'),
       ('MSI Clutch GM41', 49.99, 69.99, 'MSI', 'clutchgm41.jpg'),
       ('Acer Predator Helios 300', 1199.99, 1399.99, 'Acer', 'helios300.jpg'),
       ('Acer Aspire 5', 499.99, 599.99, 'Acer', 'aspire5.jpg'),
       ('Acer Nitro Mouse', 29.99, 39.99, 'Acer', 'nitromouse.jpg'),
       ('HP Spectre x360', 1199.99, 1499.99, 'HP', 'spectrex360.jpg'),
       ('HP Envy 13', 749.99, 999.99, 'HP', 'envy13.jpg'),
       ('HP Omen Headset', 79.99, 99.99, 'HP', 'omenheadset.jpg'),
       ('Logitech G Pro X', 99.99, 129.99, 'Logitech', 'gprox.jpg'),
       ('Logitech MX Master 3', 79.99, 99.99, 'Logitech', 'mxmaster3.jpg'),
       ('Logitech G915 Keyboard', 199.99, 249.99, 'Logitech', 'g915keyboard.jpg'),
       ('Intel Core i9-13900K', 599.99, 699.99, 'Intel', 'i913900k.jpg'),
       ('Intel NUC 11', 749.99, 899.99, 'Intel', 'nuc11.jpg'),
       ('Intel Arc A750', 249.99, 299.99, 'Intel', 'arca750.jpg');

INSERT INTO categories (category_name)
VALUES ('Smartphone'),
       ('Laptop'),
       ('Tablet'),
       ('PC Components'),
       ('Accessory');

INSERT INTO product_categories (id_product, id_category)
VALUES (1, 1),
       (1, 5),
       (2, 2),
       (2, 4),
       (3, 5),
       (4, 5),
       (5, 1),
       (5, 5),
       (6, 3),
       (6, 5),
       (7, 5),
       (8, 5),
       (9, 1),
       (9, 5),
       (10, 3),
       (10, 5),
       (11, 5),
       (12, 1),
       (13, 2),
       (13, 4),
       (14, 5),
       (15, 2),
       (15, 4),
       (16, 2),
       (16, 4),
       (17, 2),
       (17, 4),
       (18, 2),
       (18, 4),
       (19, 2),
       (19, 4),
       (20, 5),
       (21, 2),
       (21, 4),
       (22, 2),
       (22, 4),
       (23, 5),
       (24, 2),
       (24, 4),
       (25, 2),
       (25, 4),
       (26, 5),
       (27, 5),
       (28, 5),
       (29, 5),
       (30, 4),
       (31, 4),
       (32, 4);

INSERT INTO warehouses (name, address)
VALUES ('TechStore Warehouse 1', '123 Tech Avenue, San Francisco, CA 94105'),
       ('TechStore Warehouse 2', '456 Innovation Drive, Austin, TX 73301'),
       ('TechStore Warehouse 3', '789 Silicon Road, Mountain View, CA 94043');

INSERT INTO stores (name, address)
VALUES ('TechStore Branch 1', '350 Fifth Avenue, New York, NY 10118'),
       ('TechStore Branch 2', '1 Infinite Loop, Cupertino, CA 95014'),
       ('TechStore Branch 3', '1600 Amphitheatre Parkway, Mountain View, CA 94043');

INSERT INTO products_warehouse (id_product, id_warehouse, quantity)
VALUES (1, 1, 100),
       (2, 1, 50),
       (3, 1, 75),
       (4, 1, 150),
       (5, 1, 120),
       (6, 1, 90),
       (7, 1, 110),
       (8, 1, 200),
       (9, 1, 130),
       (10, 1, 80),
       (11, 1, 140),
       (12, 1, 70),
       (13, 1, 95),
       (14, 1, 85),
       (15, 1, 55),
       (16, 1, 60),
       (17, 1, 65),
       (18, 1, 45),
       (19, 1, 100),
       (20, 1, 50),
       (21, 1, 60),
       (22, 1, 70),
       (23, 1, 75),
       (24, 1, 80),
       (25, 1, 90),
       (26, 1, 110),
       (27, 1, 120),
       (28, 1, 100),
       (29, 1, 95),
       (30, 1, 60),
       (31, 1, 75),
       (32, 1, 50);

INSERT INTO products_warehouse (id_product, id_warehouse, quantity)
VALUES (1, 2, 200),
       (2, 2, 80),
       (3, 2, 90),
       (4, 2, 180),
       (5, 2, 130),
       (6, 2, 85),
       (7, 2, 120),
       (8, 2, 220),
       (9, 2, 110),
       (10, 2, 75),
       (11, 2, 150),
       (12, 2, 90),
       (13, 2, 105),
       (14, 2, 95),
       (15, 2, 65),
       (16, 2, 70),
       (17, 2, 75),
       (18, 2, 55),
       (19, 2, 110),
       (20, 2, 60),
       (21, 2, 70),
       (22, 2, 75),
       (23, 2, 80),
       (24, 2, 90),
       (25, 2, 85),
       (26, 2, 120),
       (27, 2, 130),
       (28, 2, 110),
       (29, 2, 105),
       (30, 2, 70),
       (31, 2, 85),
       (32, 2, 60);

INSERT INTO products_warehouse (id_product, id_warehouse, quantity)
VALUES (1, 3, 150),
       (2, 3, 70),
       (3, 3, 80),
       (4, 3, 140),
       (5, 3, 125),
       (6, 3, 95),
       (7, 3, 115),
       (8, 3, 210),
       (9, 3, 115),
       (10, 3, 70),
       (11, 3, 145),
       (12, 3, 85),
       (13, 3, 100),
       (14, 3, 85),
       (15, 3, 60),
       (16, 3, 65),
       (17, 3, 70),
       (18, 3, 50),
       (19, 3, 105),
       (20, 3, 55),
       (21, 3, 65),
       (22, 3, 75),
       (23, 3, 85),
       (24, 3, 85),
       (25, 3, 80),
       (26, 3, 115),
       (27, 3, 125),
       (28, 3, 105),
       (29, 3, 100),
       (30, 3, 65),
       (31, 3, 80),
       (32, 3, 55);

INSERT INTO products_store (id_product, id_store, quantity)
VALUES (1, 1, 30),
       (2, 1, 25),
       (3, 1, 40),
       (4, 1, 50),
       (5, 1, 35),
       (6, 1, 30),
       (7, 1, 45),
       (8, 1, 60),
       (9, 1, 40),
       (10, 1, 35),
       (11, 1, 50),
       (12, 1, 30),
       (13, 1, 25),
       (14, 1, 40),
       (15, 1, 20),
       (16, 1, 25),
       (17, 1, 30),
       (18, 1, 20),
       (19, 1, 35),
       (20, 1, 25),
       (21, 1, 30),
       (22, 1, 40),
       (23, 1, 45),
       (24, 1, 35),
       (25, 1, 40),
       (26, 1, 50),
       (27, 1, 35),
       (28, 1, 30),
       (29, 1, 25),
       (30, 1, 20),
       (31, 1, 35),
       (32, 1, 25);

INSERT INTO products_store (id_product, id_store, quantity)
VALUES (1, 2, 20),
       (3, 2, 25),
       (5, 2, 20),
       (6, 2, 15),
       (8, 2, 30),
       (9, 2, 20),
       (11, 2, 25),
       (13, 2, 20),
       (14, 2, 25),
       (17, 2, 15),
       (18, 2, 10),
       (21, 2, 20),
       (22, 2, 25),
       (26, 2, 30),
       (28, 2, 20);

INSERT INTO products_store (id_product, id_store, quantity)
VALUES (2, 3, 20),
       (4, 3, 30),
       (5, 3, 25),
       (7, 3, 20),
       (10, 3, 15),
       (12, 3, 20),
       (15, 3, 10),
       (16, 3, 15),
       (19, 3, 20),
       (20, 3, 10),
       (23, 3, 25),
       (24, 3, 30),
       (27, 3, 20),
       (29, 3, 25),
       (31, 3, 15);

INSERT INTO role (role)
VALUES ('General Director'),
       ('Store Management'),
       ('Warehouse Management'),
       ('Cashier'),
       ('Employee');

INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role,
                       id_store, id_warehouse)
VALUES ('John', 'Doe', 1, '1980-05-15', 'john.doe@company.com', '1234567890', '123 view.Main St', now(),
        '15000000', 1, NULL, NULL);

INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role,
                       id_store, id_warehouse)
VALUES ('Jane', 'Smith', 0, '1985-03-10', 'jane.smith@company.com', '0987654321', '456 Oak St', now(), '70000',
        2, 1, NULL),
       ('Alice', 'Johnson', 0, '1990-07-22', 'alice.johnson@company.com', '1122334455', '789 Pine St', now(),
        '70000', 2, 2, NULL),
       ('Bob', 'Williams', 1, '1988-09-12', 'bob.williams@company.com', '6677889900', '321 Birch St', now(),
        '70000', 2, 3, NULL);

INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role,
                       id_store, id_warehouse)
VALUES ('Charlie', 'Brown', 1, '1978-11-05', 'charlie.brown@company.com', '7788990011', '147 Cedar St', now(),
        '80000', 3, NULL, 1),
       ('Dave', 'Miller', 1, '1983-01-17', 'dave.miller@company.com', '8899001122', '963 Spruce St', now(),
        '80000', 3, NULL, 2),
       ('Eve', 'Davis', 0, '1992-02-14', 'eve.davis@company.com', '2233445566', '852 Maple St', now(), '80000',
        3, NULL, 3);

INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role,
                       id_store, id_warehouse)
VALUES ('Frank', 'Wilson', 1, '1995-08-29', 'frank.wilson@company.com', '3344556677', '654 Elm St', now(),
        '40000', 4, 1, NULL),
       ('Grace', 'Moore', 0, '1997-10-12', 'grace.moore@company.com', '4455667788', '741 Ash St', now(), '40000',
        4, 2, NULL),
       ('Hannah', 'Taylor', 0, '1998-12-05', 'hannah.taylor@company.com', '5566778899', '159 Beech St', now(),
        '40000', 4, 3, NULL);

INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role,
                       id_store, id_warehouse)
VALUES ('Michael', 'Brown', 1, '1990-04-10', 'michael.brown@company.com', '3216549870', '258 Willow St', now(),
        75000.00, 5, 1, NULL),
       ('Olivia', 'Garcia', 0, '1987-06-15', 'olivia.garcia@company.com', '4561237890', '369 Fir St', now(),
        75000.00, 5, 2, NULL),
       ('William', 'Martinez', 1, '1993-09-20', 'william.martinez@company.com', '1597534862', '753 Spruce St',
        now(), 75000.00, 5, 3, NULL),
       ('Sophia', 'Wilson', 0, '1994-11-25', 'sophia.wilson@company.com', '9876543210', '123 Elm St', now(),
        65000.00, 5, NULL, 1),
       ('James', 'Lee', 1, '1989-02-05', 'james.lee@company.com', '1357924680', '456 Cedar St', now(), 65000.00,
        5, NULL, 2),
       ('Isabella', 'Hernandez', 0, '1996-12-30', 'isabella.hernandez@company.com', '2468135790', '789 Birch St',
        now(), 65000.00, 5, NULL, 3);
    INSERT
INTO accounts (username, password, id_person)
VALUES
    ('director', 'password_director', 1);

-- Store Managers
INSERT INTO accounts (username, password, id_person)
VALUES ('managerStore1', 'password_manager1', 2),
       ('managerStore2', 'password_manager2', 3),
       ('managerStore3', 'password_manager3', 4);

-- Warehouse Managers
INSERT INTO accounts (username, password, id_person)
VALUES ('managerWarehouse1', 'password_manager1', 5),
       ('managerWarehouse2', 'password_manager2', 6),
       ('managerWarehouse3', 'password_manager3', 7);

-- Cashiers
INSERT INTO accounts (username, password, id_person)
VALUES ('cashierStore1', 'password_cashier1', 8),
       ('cashierStore2', 'password_cashier2', 9),
       ('cashierStore3', 'password_cashier3', 10);

INSERT INTO customers (name, phone_number)
VALUES ('Customer', '1234567890'),
       ('John Doe', '123-456-7890'),
       ('Jane Smith', '987-654-3210'),
       ('Michael Johnson', '456-789-1230'),
       ('Emily Davis', '789-123-4560'),
       ('David Martinez', '321-654-9870'),
       ('Sophia Lee', '654-321-7890'),
       ('James Brown', '123-987-6540'),
       ('Olivia Wilson', '321-123-4567'),
       ('Liam Harris', '987-321-6540'),
       ('Emma Clark', '456-654-3210');
       
       
       
       SELECT p.id, p.product_name, p.brand, ps.quantity AS stock, 
                    IFNULL(SUM(pr.quantity), 0) AS sold_quantity,
                    IFNULL(SUM(pr.profit), 0) AS profit 
                    FROM products p 
                    JOIN products_store ps ON p.id = ps.id_product 
                    LEFT JOIN products_receipt pr ON p.id = pr.id_product 
                    LEFT JOIN receipts r ON pr.id_receipt = r.id AND r.id_store = 1 
                    WHERE ps.id_store = 1
                    GROUP BY p.id, ps.quantity;
                    
                    
                    SELECT SUM(quantity) AS total_quantity FROM products_store WHERE id_store = 1;

SELECT CONCAT(e.first_name, ' ', e.last_name) AS cashier_name 
                FROM employees e 
                JOIN receipts r ON e.id = r.id_cashier 
                WHERE r.id = 1