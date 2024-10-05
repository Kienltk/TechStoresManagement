CREATE DATABASE tech_store_manager;
USE tech_store_manager;

-- PRIMARY KEY
CREATE TABLE products (
	id int auto_increment primary key,
    product_name varchar(255),
    purchase_price decimal(10,2),
    sale_price decimal(10,2),
	brand varchar(50),
    img_address varchar(255)
);

CREATE TABLE categories (
	id int auto_increment primary key,
    category_name varchar(100)
);

CREATE TABLE warehouses (
	id int auto_increment primary key,
    name varchar(100),
    address varchar(255)
);

CREATE TABLE stores (
	id int auto_increment primary key,
    name varchar(100),
    address varchar(255)
);

CREATE TABLE customers (
	id int auto_increment primary key,
    name varchar(255),
    phone_number varchar(15),
    email varchar(100)
);

-- FOREIGN KEY
-- PRODUCT
CREATE TABLE product_categories (
	id_product int,
    id_category int,
    foreign key (id_product) references products(id),
    foreign key (id_category) references categories(id)
);

-- WAREHOUSES
CREATE TABLE product_warehouse (
    id_product int,	
    id_warehouse int,
    quantity int,
	foreign key (id_warehouse) references warehouses(id),
	foreign key (id_product) references products(id)
);

CREATE TABLE import_warehouse (
    id int auto_increment primary key,
    id_warehouse int, 
    product_import_date datetime,
    foreign key (id_warehouse) references warehouses(id)
);

CREATE TABLE import_warehouse_details (
    id_import int,
    id_product int,
    number_of_imported_product int,
    foreign key (id_import) references import_warehouse(id),
    foreign key (id_product) references products(id)
);

CREATE TABLE warehouse_shipments (
    id int auto_increment primary key,
    id_warehouse int,
    id_store int,
    product_delivery_date datetime,
	status varchar(50),
    foreign key (id_store) references stores(id),
    foreign key (id_warehouse) references warehouses(id)
);

CREATE TABLE warehouse_shipment_details (
    id_shipment int,
    id_product int,
    number_of_delivery_product int,
    foreign key (id_shipment) references warehouse_shipments(id),
    foreign key (id_product) references products(id)
);

-- STORES
CREATE TABLE products_store (
    id_product int, 
	id_store int,
    quantity int,
	foreign key (id_store) references stores(id),
	foreign key (id_product) references products(id)
);

CREATE TABLE import_store (
    id int auto_increment primary key,
    id_store int,
    received_date datetime,
    foreign key (id_store) references stores(id)
);

CREATE TABLE import_store_details (
    id_import int,
    id_shipment int,
    id_product int, 
    quantity int,
	foreign key (id_import) references import_store(id),
	foreign key (id_shipment) references warehouse_shipments(id),
    foreign key (id_product) references products(id)
);

-- PERSON
CREATE TABLE persons (
	id int auto_increment primary key,
    first_name varchar(50),
    last_name varchar(50),
    gender boolean,
    dob date,
    email varchar(255),
    phone_number varchar(15),
    address varchar(255),
    hire_date date,
    salary varchar(20),
    role int,
    id_store int,
    id_warehouse int,
    status varchar(255),
    foreign key (id_store) references stores(id),
	foreign key (id_warehouse) references warehouses(id)
);

CREATE TABLE accounts (
	id int auto_increment primary key,
    username varchar(50),
    password varchar(255),
    id_person int,
    foreign key (id_person) references persons(id)
);

-- ORDERS
CREATE TABLE receipts (
	id int auto_increment primary key,
    id_customer int,
    id_store int,
    purchase_date datetime,
    foreign key (id_customer) references customers(id),
    foreign key (id_store) references stores(id)
);

CREATE TABLE products_receipt (
	id_receipt int,
    id_product int,
    quantity int,
    foreign key (id_receipt) references receipts(id),
    foreign key (id_product) references products(id)
);

-- DATA DEFAULT
INSERT INTO products (product_name, purchase_price, sale_price, brand, img_address) VALUES
('iPhone 15 Pro', 999.99, 1199.99, 'Apple', 'img/iphone15pro.jpg'),
('MacBook Air M2', 899.99, 1099.99, 'Apple', 'img/macbookairm2.jpg'),
('Apple Watch Series 9', 399.99, 499.99, 'Apple', 'img/applewatch9.jpg'),
('AirPods Pro 2', 199.99, 249.99, 'Apple', 'img/airpodspro2.jpg'),
('Samsung Galaxy S23', 799.99, 999.99, 'Samsung', 'img/galaxys23.jpg'),
('Samsung Galaxy Tab S9', 599.99, 749.99, 'Samsung', 'img/galaxytabs9.jpg'),
('Samsung Galaxy Watch 6', 349.99, 399.99, 'Samsung', 'img/galaxywatch6.jpg'),
('Samsung Galaxy Buds 2', 99.99, 149.99, 'Samsung', 'img/galaxybuds2.jpg'),
('Xiaomi Mi 13', 599.99, 699.99, 'Xiaomi', 'img/mi13.jpg'),
('Xiaomi Pad 6', 349.99, 449.99, 'Xiaomi', 'img/pad6.jpg'),
('Xiaomi Mi Band 8', 49.99, 59.99, 'Xiaomi', 'img/miband8.jpg'),
('ASUS ROG Phone 7', 899.99, 999.99, 'ASUS', 'img/rogphone7.jpg'),
('ASUS ZenBook 14', 749.99, 899.99, 'ASUS', 'img/zenbook14.jpg'),
('ASUS TUF Gaming Headset', 79.99, 99.99, 'ASUS', 'img/tufheadset.jpg'),
('Dell XPS 13', 999.99, 1199.99, 'Dell', 'img/xps13.jpg'),
('Dell Alienware M15', 1199.99, 1499.99, 'Dell', 'img/alienwarem15.jpg'),
('Dell Inspiron 14', 549.99, 649.99, 'Dell', 'img/inspiron14.jpg'),
('MSI Raider GE78', 1799.99, 1999.99, 'MSI', 'img/raiderge78.jpg'),
('MSI Prestige 14', 899.99, 1099.99, 'MSI', 'img/prestige14.jpg'),
('MSI Clutch GM41', 49.99, 69.99, 'MSI', 'img/clutchgm41.jpg'),
('Acer Predator Helios 300', 1199.99, 1399.99, 'Acer', 'img/helios300.jpg'),
('Acer Aspire 5', 499.99, 599.99, 'Acer', 'img/aspire5.jpg'),
('Acer Nitro Mouse', 29.99, 39.99, 'Acer', 'img/nitromouse.jpg'),
('HP Spectre x360', 1199.99, 1499.99, 'HP', 'img/spectrex360.jpg'),
('HP Envy 13', 749.99, 999.99, 'HP', 'img/envy13.jpg'),
('HP Omen Headset', 79.99, 99.99, 'HP', 'img/omenheadset.jpg'),
('Logitech G Pro X', 99.99, 129.99, 'Logitech', 'img/gprox.jpg'),
('Logitech MX Master 3', 79.99, 99.99, 'Logitech', 'img/mxmaster3.jpg'),
('Logitech G915 Keyboard', 199.99, 249.99, 'Logitech', 'img/g915.jpg'),
('Intel Core i9-13900K', 599.99, 699.99, 'Intel', 'img/i913900k.jpg'),
('Intel NUC 11', 749.99, 899.99, 'Intel', 'img/nuc11.jpg'),
('Intel Arc A750', 249.99, 299.99, 'Intel', 'img/arca750.jpg');

INSERT INTO categories (category_name) VALUES
('Smartphone'),
('Laptop'),
('Tablet'),
('PC Components'),
('Accessory');

INSERT INTO product_categories (id_product, id_category) VALUES
-- Apple iPhone 15 Pro
(1, 1), (1, 5),
(2, 2), (2, 4),
(3, 5),
(4, 5),
(5, 1), (5, 5),
(6, 3), (6, 5),
(7, 5),
(8, 5),
(9, 1), (9, 5),
(10, 3), (10, 5),
(11, 5),
(12, 1),
(13, 2), (13, 4),
(14, 5),
(15, 2), (15, 4),
(16, 2), (16, 4),
(17, 2), (17, 4),
(18, 2), (18, 4),
(19, 2), (19, 4),
(20, 5),
(21, 2), (21, 4),
(22, 2), (22, 4),
(23, 5),
(24, 2), (24, 4),
(25, 2), (25, 4),
(26, 5),
(27, 5),
(28, 5),
(29, 5),
(30, 4),
(31, 4),
(32, 4);

INSERT INTO warehouses (name, address) VALUES
('TechStore Warehouse 1', '123 Tech Avenue, San Francisco, CA 94105'),
('TechStore Warehouse 2', '456 Innovation Drive, Austin, TX 73301'),
('TechStore Warehouse 3', '789 Silicon Road, Mountain View, CA 94043');

INSERT INTO stores (name, address) VALUES
('TechStore Branch 1', '350 Fifth Avenue, New York, NY 10118'),
('TechStore Branch 2', '1 Infinite Loop, Cupertino, CA 95014'),
('TechStore Branch 3', '1600 Amphitheatre Parkway, Mountain View, CA 94043');


INSERT INTO customers (name, phone_number, email) VALUES
('John Doe', '123-456-7890', 'john.doe@example.com'),
('Jane Smith', '987-654-3210', 'jane.smith@example.com'),
('Michael Johnson', '456-789-1230', 'michael.johnson@example.com'),
('Emily Davis', '789-123-4560', 'emily.davis@example.com'),
('David Martinez', '321-654-9870', 'david.martinez@example.com'),
('Sophia Lee', '654-321-7890', 'sophia.lee@example.com'),
('James Brown', '123-987-6540', 'james.brown@example.com'),
('Olivia Wilson', '321-123-4567', 'olivia.wilson@example.com'),
('Liam Harris', '987-321-6540', 'liam.harris@example.com'),
('Emma Clark', '456-654-3210', 'emma.clark@example.com');

INSERT INTO product_warehouse (id_product, id_warehouse, quantity) VALUES
(1, 1, 100), (2, 1, 50), (3, 1, 75), (4, 1, 150),
(5, 1, 120), (6, 1, 90), (7, 1, 110), (8, 1, 200),
(9, 1, 130), (10, 1, 80), (11, 1, 140), (12, 1, 70),
(13, 1, 95), (14, 1, 85), (15, 1, 55), (16, 1, 60),
(17, 1, 65), (18, 1, 45), (19, 1, 100), (20, 1, 50),
(21, 1, 60), (22, 1, 70), (23, 1, 75), (24, 1, 80),
(25, 1, 90), (26, 1, 110), (27, 1, 120), (28, 1, 100),
(29, 1, 95), (30, 1, 60), (31, 1, 75), (32, 1, 50);

INSERT INTO product_warehouse (id_product, id_warehouse, quantity) VALUES
(1, 2, 200), (2, 2, 80), (3, 2, 90), (4, 2, 180),
(5, 2, 130), (6, 2, 85), (7, 2, 120), (8, 2, 220),
(9, 2, 110), (10, 2, 75), (11, 2, 150), (12, 2, 90),
(13, 2, 105), (14, 2, 95), (15, 2, 65), (16, 2, 70),
(17, 2, 75), (18, 2, 55), (19, 2, 110), (20, 2, 60),
(21, 2, 70), (22, 2, 75), (23, 2, 80), (24, 2, 90),
(25, 2, 85), (26, 2, 120), (27, 2, 130), (28, 2, 110),
(29, 2, 105), (30, 2, 70), (31, 2, 85), (32, 2, 60);

INSERT INTO product_warehouse (id_product, id_warehouse, quantity) VALUES
(1, 3, 150), (2, 3, 70), (3, 3, 80), (4, 3, 140),
(5, 3, 125), (6, 3, 95), (7, 3, 115), (8, 3, 210),
(9, 3, 115), (10, 3, 70), (11, 3, 145), (12, 3, 85),
(13, 3, 100), (14, 3, 85), (15, 3, 60), (16, 3, 65),
(17, 3, 70), (18, 3, 50), (19, 3, 105), (20, 3, 55),
(21, 3, 65), (22, 3, 75), (23, 3, 85), (24, 3, 85),
(25, 3, 80), (26, 3, 115), (27, 3, 125), (28, 3, 105),
(29, 3, 100), (30, 3, 65), (31, 3, 80), (32, 3, 55);

INSERT INTO products_store (id_product, id_store, quantity) VALUES
(1, 1, 30), (2, 1, 25), (3, 1, 40), (4, 1, 50),
(5, 1, 35), (6, 1, 30), (7, 1, 45), (8, 1, 60),
(9, 1, 40), (10, 1, 35), (11, 1, 50), (12, 1, 30),
(13, 1, 25), (14, 1, 40), (15, 1, 20), (16, 1, 25),
(17, 1, 30), (18, 1, 20), (19, 1, 35), (20, 1, 25),
(21, 1, 30), (22, 1, 40), (23, 1, 45), (24, 1, 35),
(25, 1, 40), (26, 1, 50), (27, 1, 35), (28, 1, 30),
(29, 1, 25), (30, 1, 20), (31, 1, 35), (32, 1, 25);

INSERT INTO products_store (id_product, id_store, quantity) VALUES
(1, 2, 20), (3, 2, 25), (5, 2, 20), (6, 2, 15),
(8, 2, 30), (9, 2, 20), (11, 2, 25), (13, 2, 20),
(14, 2, 25), (17, 2, 15), (18, 2, 10), (21, 2, 20),
(22, 2, 25), (26, 2, 30), (28, 2, 20);

INSERT INTO products_store (id_product, id_store, quantity) VALUES
(2, 3, 20), (4, 3, 30), (5, 3, 25), (7, 3, 20),
(10, 3, 15), (12, 3, 20), (15, 3, 10), (16, 3, 15),
(19, 3, 20), (20, 3, 10), (23, 3, 25), (24, 3, 30),
(27, 3, 20), (29, 3, 25), (31, 3, 15);

INSERT INTO persons (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, role, id_store, id_warehouse, status) VALUES
('John', 'Doe', 1, '1980-05-15', 'john.doe@company.com', '1234567890', '123 side.Main St', '2020-01-01', '150000', 1, NULL, NULL, 'Active');

INSERT INTO persons (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, role, id_store, id_warehouse, status) VALUES
('Jane', 'Smith', 0, '1985-03-10', 'jane.smith@company.com', '0987654321', '456 Oak St', '2021-03-15', '70000', 2, 1, NULL, 'Active'),
('Alice', 'Johnson', 0, '1990-07-22', 'alice.johnson@company.com', '1122334455', '789 Pine St', '2022-04-20', '70000', 2, 2, NULL, 'Active'),
('Bob', 'Williams', 1, '1988-09-12', 'bob.williams@company.com', '6677889900', '321 Birch St', '2021-06-30', '70000', 2, 3, NULL, 'Active');

INSERT INTO persons (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, role, id_store, id_warehouse, status) VALUES
('Charlie', 'Brown', 1, '1978-11-05', 'charlie.brown@company.com', '7788990011', '147 Cedar St', '2019-07-25', '80000', 3, NULL, 1, 'Active'),
('Dave', 'Miller', 1, '1983-01-17', 'dave.miller@company.com', '8899001122', '963 Spruce St', '2020-05-05', '80000', 3, NULL, 2, 'Active'),
('Eve', 'Davis', 0, '1992-02-14', 'eve.davis@company.com', '2233445566', '852 Maple St', '2021-09-10', '80000', 3, NULL, 3, 'Active');

INSERT INTO persons (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, role, id_store, id_warehouse, status) VALUES
('Frank', 'Wilson', 1, '1995-08-29', 'frank.wilson@company.com', '3344556677', '654 Elm St', '2023-02-01', '40000', 4, 1, NULL, 'Active'),
('Grace', 'Moore', 0, '1997-10-12', 'grace.moore@company.com', '4455667788', '741 Ash St', '2023-03-15', '40000', 4, 2, NULL, 'Active'),
('Hannah', 'Taylor', 0, '1998-12-05', 'hannah.taylor@company.com', '5566778899', '159 Beech St', '2023-04-20', '40000', 4, 3, NULL, 'Active');

INSERT INTO receipts (id_customer, id_store, purchase_date) VALUES
(1, 1, '2024-05-10 12:30:00'),
(2, 2, '2024-05-12 14:00:00'),
(3, 3, '2024-05-15 16:45:00'),
(4, 1, '2024-05-18 11:15:00'),
(5, 2, '2024-05-20 13:00:00'),
(6, 3, '2024-05-22 15:30:00'),
(7, 1, '2024-05-25 10:00:00'),
(8, 2, '2024-05-28 14:20:00'),
(9, 3, '2024-05-30 17:00:00'),
(10, 1, '2024-06-02 09:45:00'),
(1, 2, '2024-06-05 12:30:00'),
(2, 3, '2024-06-07 14:00:00'),
(3, 1, '2024-06-10 16:45:00');

INSERT INTO products_receipt (id_receipt, id_product, quantity) VALUES
(1, 1, 2),
(1, 3, 1),
(2, 2, 3),
(2, 4, 2),
(3, 5, 1),
(3, 6, 1),
(4, 7, 3),
(4, 8, 2),
(5, 9, 1),
(5, 10, 4),
(6, 11, 2),
(6, 12, 1),
(7, 13, 1),
(7, 14, 2),
(8, 15, 3),
(8, 16, 1),
(9, 17, 1),
(9, 18, 2),
(10, 19, 3),
(10, 20, 1),
(11, 3, 1),
(12, 4, 2),
(13, 5, 1);

INSERT INTO accounts (username, password, id_person) VALUES
('director', 'password_director', 1);

-- Store Managers
INSERT INTO accounts (username, password, id_person) VALUES
('managerStore1', 'password_manager1', 2),
('managerStore2', 'password_manager2', 3),
('managerStore3', 'password_manager3', 4);

-- Warehouse Managers
INSERT INTO accounts (username, password, id_person) VALUES
('managerWarehouse1', 'password_manager1', 5),
('managerWarehouse2', 'password_manager2', 6),
('managerWarehouse3', 'password_manager3', 7);

-- Cashiers
INSERT INTO accounts (username, password, id_person) VALUES
('cashierStore1', 'password_cashier1', 8),
('cashierStore2', 'password_cashier2', 9),
('hcashierStore3', 'password_cashier3', 10);

INSERT INTO import_warehouse (id_warehouse, product_import_date) VALUES
(1, '2024-01-10 10:00:00'),
(1, '2024-02-15 11:00:00'),
(1, '2024-03-20 12:00:00'),
(2, '2024-01-12 09:30:00'),
(2, '2024-02-18 10:45:00'),
(2, '2024-03-25 14:15:00'),
(3, '2024-01-20 13:00:00'),
(3, '2024-02-20 14:30:00'),
(3, '2024-03-30 16:00:00');

INSERT INTO import_warehouse_details (id_import, id_product, number_of_imported_product) VALUES
(1, 1, 50), (1, 2, 30), (1, 3, 40),
(2, 4, 20), (2, 5, 15), (2, 6, 35),
(3, 7, 25), (3, 8, 50), (3, 9, 45),
(4, 10, 30), (4, 11, 40), (4, 12, 20),
(5, 13, 35), (5, 14, 25), (5, 15, 45),
(6, 16, 50), (6, 17, 30), (6, 18, 20),
(7, 19, 15), (7, 20, 25), (7, 21, 50),
(8, 22, 40), (8, 23, 35), (8, 24, 20),
(9, 25, 45), (9, 26, 30), (9, 27, 50);

INSERT INTO warehouse_shipments (id_warehouse, id_store, product_delivery_date, status) VALUES
(1, 1, '2024-01-20 14:00:00', 'Delivered'),
(1, 2, '2024-02-25 15:00:00', 'Shipped'),
(1, 3, '2024-03-30 16:30:00', 'Preparing'),
(2, 1, '2024-01-22 12:30:00', 'Delivered'),
(2, 2, '2024-02-28 13:45:00', 'Shipped'),
(2, 3, '2024-03-25 11:00:00', 'Preparing'),
(3, 1, '2024-01-30 10:15:00', 'Delivered'),
(3, 2, '2024-02-18 14:00:00', 'Shipped');

INSERT INTO warehouse_shipment_details (id_shipment, id_product, number_of_delivery_product) VALUES
(1, 1, 20), (1, 2, 15), (1, 3, 10),
(2, 4, 25), (2, 5, 20), (2, 6, 15),
(3, 7, 30), (3, 8, 35), (3, 9, 20),
(4, 10, 25), (4, 11, 30), (4, 12, 15),
(5, 13, 20), (5, 14, 25), (5, 15, 30),
(6, 16, 15), (6, 17, 20), (6, 18, 35),
(7, 19, 40), (7, 20, 30), (7, 21, 25),
(8, 22, 20), (8, 23, 35), (8, 24, 15);

INSERT INTO import_store (id_store, received_date) VALUES
(1, '2024-02-01 12:00:00'),
(1, '2024-03-05 15:00:00'),
(1, '2024-04-10 10:30:00'),
(2, '2024-02-10 13:00:00'),
(2, '2024-03-15 11:45:00'),
(2, '2024-04-20 16:00:00'),
(3, '2024-02-20 14:15:00'),
(3, '2024-03-25 17:30:00'),
(3, '2024-04-30 09:00:00');

INSERT INTO import_store_details (id_import, id_shipment, id_product, quantity) VALUES
(1, 1, 1, 10), (1, 1, 2, 5), (1, 1, 3, 8),
(2, 2, 4, 12), (2, 2, 5, 7), (2, 2, 6, 6),
(3, 3, 7, 14), (3, 3, 8, 10), (3, 3, 9, 12),
(4, 4, 10, 11), (4, 4, 11, 9), (4, 4, 12, 13),
(5, 5, 13, 8), (5, 5, 14, 15), (5, 5, 15, 10),
(6, 6, 16, 7), (6, 6, 17, 12), (6, 6, 18, 9),
(7, 7, 19, 14), (7, 7, 20, 11), (7, 7, 21, 13),
(8, 8, 22, 9), (8, 8, 23, 8), (8, 8, 24, 10);




-- FINANCE

