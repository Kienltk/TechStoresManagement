CREATE DATABASE tech_store_manager;
USE tech_store_manager;

-- PRIMARY KEY
CREATE TABLE categories (
	id int auto_increment primary key,
    category_name varchar(100)
);

CREATE TABLE components (
	id int auto_increment primary key,
    component_name varchar(50)
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

CREATE TABLE roles (
	id int auto_increment primary key,
    role_name varchar(100)
);

CREATE TABLE customer (
	id int auto_increment primary key,
    name varchar(255),
    phone_number varchar(15),
    email varchar(100)
);


-- FOREIGN KEY
-- PRODUCT
CREATE TABLE products (
	id int auto_increment primary key,
    product_name varchar(255),
    purchase_price decimal(10,2),
    sale_price decimal(10,2),
    description varchar(255),
	firm varchar(50)
);

CREATE TABLE child_categories (
	id int auto_increment primary key,
    id_category int,
    child_category_name varchar(100),
    foreign key (id_category) references categories(id)
);

CREATE TABLE product_child_category (
	id_product int,
    id_child_category int,
    foreign key (id_product) references products(id),
    foreign key (id_child_category) references child_categories(id)
);

CREATE TABLE product_component (
	id_product int,
    id_component int,
    component_description varchar(255),
    foreign key (id_product) references products(id),
    foreign key (id_component) references components(id)
);

-- WAREHOUSES
CREATE TABLE product_warehouse (
    id_product int,	
    id_warehouse int, 
    quantity int,
	foreign key (id_warehouse) references warehouses(id),
	foreign key (id_product) references products(id)
);

CREATE TABLE products_received_wavehouse (
	id_product int,	
    id_warehouse int, 
	product_import_date datetime,
    number_of_imported_product int,
    foreign key (id_warehouse) references warehouses(id),
	foreign key (id_product) references products(id)
);

CREATE TABLE warehouse_to_store_shipments (
	id_product int,
	id_store int,
    id_warehouse int,
    product_delivery_date datetime,
    number_of_delivery_product int,
	foreign key (id_store) references stores(id),
	foreign key (id_product) references products(id),
	foreign key (id_warehouse) references warehouses(id)
);

-- STORES
CREATE TABLE products_store (
    id_product int, 
	id_store int,
    quantity int,
	foreign key (id_store) references stores(id),
	foreign key (id_product) references products(id)
);

CREATE TABLE products_received_store (
    id_product int,
	id_store int,
    id_warehouse int,
    product_import_date datetime,
    number_of_imported_product int,
	foreign key (id_store) references stores(id),
	foreign key (id_product) references products(id),
	foreign key (id_warehouse) references warehouses(id)
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
    id_role int,
    id_store int,
    id_warehouse int,
    status varchar(255),
	foreign key (id_role) references roles(id),
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

-- IMAGE
CREATE TABLE product_image (
	id int auto_increment primary key,
    id_product int,
    address_image varchar(255),
    type varchar(50),
	foreign key (id_product) references products(id)
);

-- ORDERS
CREATE TABLE purchase (
	id int auto_increment primary key,
    id_customer int,
    id_store int,
    purchase_date datetime,
    foreign key (id_customer) references customer(id),
    foreign key (id_store) references stores(id)
);

CREATE TABLE purchase_items (
	purchase_id int,
    product_id int,
    quantity int,
    foreign key (purchase_id) references purchase(id),
    foreign key (product_id) references products(id)
);

-- DATA DEFAULT
INSERT INTO firms (firm_name) VALUES 
('Apple'),
('Samsung'),
('Xiaomi'),
('ASUS'),
('Dell'),
('MSI'),
('Acer'),
('HP'),
('Logitech'),
('Intel');

INSERT INTO components (component_name) VALUES 
('CPU'),
('RAM'),
('Pin'),
('Mainboard'),
('VGA'),
('PSU'),
('Case'),
('Hard disk drive'),
('Screen');

INSERT INTO categories (category_name) VALUES 
('Device'),
('Components'),
('Accessory');

INSERT INTO warehouses (name, address) VALUES 
('TechStore Warehouse Base One', '43 Dohwa-gil, Mapo-gu, Seoul, South Korea'),
('TechStore Warehouse Base Two', '1 Electric Ave, Sparks, NV 89437, United States'),
('TechStore Warehouse Base Three', '1351 Railroad Street, Unit 102, Corona, California, United States');

INSERT INTO stores (name, address) VALUES 
('TechStore Store Base One', '40th Floor, Samsung Electronics Building, 11, Seocho-daero 74-gil, Seocho-gu , Seoul , South Korea'),
('TechStore Store Base Two', 'One Apple Park Way, Cupertino, CA 95014, United States'),
('TechStore Store Base Three', '1501 Page Mill Road, Palo Alto, California , United States');

INSERT INTO roles (role_name) VALUES 
('General Director'),
('Store Manager'),
('Warehouse Manager'),
('Cashier'),
('Employee');

INSERT INTO person_detail (first_name, last_name, gender, dob, email, phone_number, address) VALUES 
('John', 'Doe', 1, '1985-04-12', 'john.doe@example.com', '0123456789', '123 Elm St, City A'),
('Jane', 'Smith', 0, '1990-08-25', 'jane.smith@example.com', '0987654321', '456 Oak St, City B'),
('Michael', 'Brown', 1, '1982-02-19', 'michael.brown@example.com', '0123498765', '789 Pine St, City C'),
('Sarah', 'Johnson', 0, '1995-11-15', 'sarah.johnson@example.com', '0192837465', '321 Cedar St, City A'),
('Chris', 'Davis', 1, '1978-05-30', 'chris.davis@example.com', '0234567890', '654 Maple St, City B'),
('Emily', 'Garcia', 0, '1987-12-22', 'emily.garcia@example.com', '0765432198', '987 Birch St, City C'),
('James', 'Martinez', 1, '1992-03-05', 'james.martinez@example.com', '0321654987', '123 Ash St, City A'),
('Patricia', 'Hernandez', 0, '1988-09-09', 'patricia.hernandez@example.com', '0654321987', '456 Redwood St, City B'),
('David', 'Lopez', 1, '1980-06-18', 'david.lopez@example.com', '0482937165', '789 Cypress St, City C'),
('Linda', 'Gonzalez', 0, '1983-07-21', 'linda.gonzalez@example.com', '0321987456', '321 Palm St, City A'),
('Robert', 'Wilson', 1, '1975-12-12', 'robert.wilson@example.com', '0129873456', '654 Fir St, City B'),
('Karen', 'Moore', 0, '1993-11-30', 'karen.moore@example.com', '0234561987', '987 Willow St, City C'),
('Joseph', 'Taylor', 1, '1984-10-10', 'joseph.taylor@example.com', '0567894321', '123 Spruce St, City A'),
('Nancy', 'Anderson', 0, '1979-03-23', 'nancy.anderson@example.com', '0789456123', '456 Poplar St, City B'),
('Thomas', 'Lee', 1, '1991-05-12', 'thomas.lee@example.com', '0345678921', '789 Beech St, City C'),
('Sandra', 'Perez', 0, '1986-08-08', 'sandra.perez@example.com', '0192834657', '321 Juniper St, City A'),
('Paul', 'White', 1, '1981-09-17', 'paul.white@example.com', '0276543987', '654 Walnut St, City B'),
('Laura', 'King', 0, '1994-02-14', 'laura.king@example.com', '0321476985', '987 Chestnut St, City C'),
('Daniel', 'Scott', 1, '1989-04-09', 'daniel.scott@example.com', '0543298761', '123 Dogwood St, City A'),
('Betty', 'Allen', 0, '1977-10-30', 'betty.allen@example.com', '0765432189', '456 Laurel St, City B'),
('Mark', 'Young', 1, '1990-07-22', 'mark.young@example.com', '0219876543', '789 Hickory St, City C'),
('Barbara', 'Walker', 0, '1985-05-17', 'barbara.walker@example.com', '0123564789', '321 Magnolia St, City A'),
('Steven', 'Hall', 1, '1983-06-06', 'steven.hall@example.com', '0567891234', '654 Alder St, City B'),
('Susan', 'Allen', 0, '1992-01-19', 'susan.allen@example.com', '0789123456', '987 Sycamore St, City C'),
('Charles', 'Robinson', 1, '1987-03-28', 'charles.robinson@example.com', '0341256789', '123 Oakwood St, City A');


