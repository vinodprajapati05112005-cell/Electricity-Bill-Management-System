create database electricity_bill_management;

use electricity_bill_management;
-- ------------------------------------------------------------------------------------------------------------------------------------------------------------
-- 1. Customers table
CREATE TABLE customers (
    service_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    contact_number VARCHAR(15),
    email VARCHAR(100),
    connection_type ENUM('Domestic', 'Commercial', 'Industrial') NOT NULL,
    connection_date DATE NOT NULL,
    password varchar(50) NOT NULL
);

INSERT INTO customers (service_number, name, address, contact_number, email, connection_type, connection_date, password)
VALUES
('5000', 'Ratan Prajapati', 'Ahmedabad, Gujarat', '9876543210', 'ratan@example.com', 'Domestic', '2025-01-10', 'pass123'),
('5001', 'Jignesh Patel', 'Surat, Gujarat', '9123456780', 'jignesh@example.com', 'Commercial', '2025-02-12', 'jig123'),
('5002', 'Neha Shah', 'Vadodara, Gujarat', '9988776655', 'neha@example.com', 'Domestic', '2025-03-05', 'nehapass'),
('5003', 'Amit Kumar', 'Rajkot, Gujarat', '9876123456', 'amit@example.com', 'Industrial', '2025-04-20', 'amit2025'),
('5004', 'Pooja Mehta', 'Gandhinagar, Gujarat', '9012345678', 'pooja@example.com', 'Domestic', '2025-05-15', 'pooja456'),
('5005', 'Kiran Desai', 'Bhavnagar, Gujarat', '8765432109', 'kiran@example.com', 'Commercial', '2025-06-18', 'kiran789'),
('5006', 'Manish Trivedi', 'Surendranagar, Gujarat', '9123459876', 'manish@example.com', 'Industrial', '2025-07-22', 'manish123'),
('5007', 'Riya Vora', 'Jamnagar, Gujarat', '9988771122', 'riya@example.com', 'Domestic', '2025-08-01', 'riya456'),
('5008', 'Rajesh Solanki', 'Anand, Gujarat', '9876501234', 'rajesh@example.com', 'Commercial', '2025-08-10', 'rajesh789'),
('5009', 'Sneha Patel', 'Nadiad, Gujarat', '9012349876', 'sneha@example.com', 'Domestic', '2025-08-12', 'sneha123');

SELECT * FROM customers;

truncate table meters;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------


-- 2. Meters table
CREATE TABLE meters (
    meter_id INT PRIMARY KEY ,
    service_number VARCHAR(20),
    meter_type ENUM('Single Phase', 'Three Phase') NOT NULL,
    installation_date DATE NOT NULL,
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);

INSERT INTO meters (meter_id, service_number, meter_type, installation_date)
VALUES
(200, '5000', 'Single Phase', '2025-01-15'),
(201, '5001', 'Three Phase', '2025-02-15'),
(202, '5002', 'Single Phase', '2025-03-10'),
(203, '5003', 'Three Phase', '2025-04-25'),
(204, '5004', 'Single Phase', '2025-05-20'),
(205, '5005', 'Three Phase', '2025-06-22'),
(206, '5006', 'Three Phase', '2025-07-25'),
(207, '5007', 'Single Phase', '2025-08-05'),
(208, '5008', 'Three Phase', '2025-08-12'),
(209, '5009', 'Single Phase', '2025-08-15');

SELECT * FROM meters;

DROP TABLE meters;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 3. Readings table
CREATE TABLE readings (
    reading_id INT PRIMARY KEY AUTO_INCREMENT,
    meter_id INT,
    reading_date DATE NOT NULL,
    current_reading DECIMAL(10,2) NOT NULL,
    previous_reading DECIMAL(10,2) NOT NULL,
    units_consumed DECIMAL(10,2) GENERATED ALWAYS AS (current_reading - previous_reading) STORED,
    FOREIGN KEY (meter_id) REFERENCES meters(meter_id)
);

INSERT INTO readings (meter_id, reading_date, previous_reading, current_reading)
VALUES
(200, '2025-07-14', 100.00, 150.00),
(201, '2025-07-14', 200.00, 250.50),
(202, '2025-07-14', 50.00, 75.00),
(203, '2025-07-14', 300.00, 360.00),
(204, '2025-07-14', 120.00, 180.00),
(205, '2025-07-14', 400.00, 450.00),
(206, '2025-07-14', 500.00, 550.00),
(207, '2025-07-14', 80.00, 130.00),
(208, '2025-07-14', 250.00, 300.00),
(209, '2025-07-14', 60.00, 95.00);

select * from readings;
DROP TABLE readings;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 4. Bills table
CREATE TABLE bills (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    service_number VARCHAR(20),
    billing_year YEAR NOT NULL,
    billing_month TINYINT NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
    billing_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_units DECIMAL(10,2) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('Paid', 'Unpaid', 'Overdue') DEFAULT 'Unpaid',
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);


INSERT INTO bills (service_number, billing_year, billing_month, billing_date, due_date, total_units, amount, status)
VALUES
('5000', 2025, 7, '2025-07-14', '2025-07-29', 50.00, 600.00, 'Unpaid'),
('5001', 2025, 7, '2025-07-14', '2025-07-29', 75.00, 900.00, 'Unpaid'),
('5002', 2025, 7, '2025-07-14', '2025-07-29', 30.00, 360.00, 'Unpaid'),
('5003', 2025, 7, '2025-07-14', '2025-07-29', 60.00, 720.00, 'Unpaid'),
('5004', 2025, 7, '2025-07-14', '2025-07-29', 40.00, 480.00, 'Unpaid'),
('5005', 2025, 7, '2025-07-14', '2025-07-29', 80.00, 960.00, 'Unpaid'),
('5006', 2025, 7, '2025-07-14', '2025-07-29', 100.00, 1200.00, 'Unpaid'),
('5007', 2025, 7, '2025-07-14', '2025-07-29', 35.00, 420.00, 'Unpaid'),
('5008', 2025, 7, '2025-07-14', '2025-07-29', 90.00, 1080.00, 'Unpaid'),
('5009', 2025, 7, '2025-07-14', '2025-07-29', 55.00, 660.00, 'Unpaid');

SELECT * FROM bills;
DROP TABLE bills;



-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 5. bill_history table
CREATE TABLE bill_history (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    service_number VARCHAR(20) NOT NULL,
    billing_date DATE NOT NULL,
    total_units DECIMAL(10,2),
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('Paid', 'Unpaid', 'Overdue') DEFAULT 'paid',
    connection_type ENUM('Domestic', 'Commercial', 'Industrial') NOT NULL,
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);


select * from bill_history;

truncate table bill_history;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 6. complaints table
CREATE TABLE complaints (
    complaint_id INT PRIMARY KEY AUTO_INCREMENT,
    service_number VARCHAR(20) NOT NULL,
    complaint_text VARCHAR(500) NOT NULL,
    complaint_date DATE NOT NULL,
    status ENUM('Pending', 'Resolved') DEFAULT 'Pending',
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);

INSERT INTO complaints (service_number, complaint_text, complaint_date, status)
VALUES
('5001', 'No electricity for 2 hours', '2025-08-01', 'Pending'),
('5002', 'Meter not working properly', '2025-08-02', 'Pending'),
('5003', 'High bill complaint', '2025-08-03', 'Pending'),
('5004', 'Fuse blown repeatedly', '2025-08-04', 'Pending'),
('5005', 'Voltage fluctuation issue', '2025-08-05', 'Pending'),
('5006', 'Bill not reflecting payments', '2025-08-06', 'Pending'),
('5007', 'Connection issue during rain', '2025-08-07', 'Pending'),
('5008', 'Meter reading seems incorrect', '2025-08-08', 'Pending'),
('5009', 'Electricity outage at night', '2025-08-09', 'Pending'),
('5000', 'Request for new connection', '2025-08-10', 'Pending');

select * from complaints;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 7. feedback table
CREATE TABLE feedback (
    id INT PRIMARY KEY AUTO_INCREMENT,
    service_number VARCHAR(20) NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments VARCHAR(500),
    feedback_date DATE NOT NULL,
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);

INSERT INTO feedback (service_number, rating, comments, feedback_date)
VALUES
('5001', 5, 'Excellent service', '2025-08-01'),
('5002', 4, 'Good, but bill was high', '2025-08-02'),
('5003', 3, 'Average experience', '2025-08-03'),
('5004', 5, 'Very prompt service', '2025-08-04'),
('5005', 2, 'Frequent outages', '2025-08-05'),
('5006', 4, 'Satisfied with the response', '2025-08-06'),
('5007', 1, 'Poor service', '2025-08-07'),
('5008', 3, 'Okay, nothing special', '2025-08-08'),
('5009', 5, 'Great support team', '2025-08-09'),
('5000', 4, 'Good, but can improve', '2025-08-10');

select * from feedback;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------



-- 8. Payments table
CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT,
    payment_date DATE NOT NULL,
    payment_method ENUM('UPI', 'Debit Card', 'Credit Card','Net Banking') NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id)
);

select * from payments;
truncate table payments;



-- ------------------------------------------------------------------------------------------------------------------------------------------------------------


-- 9. advance_payments table

CREATE TABLE advance_payments (
    advance_payment_id INT AUTO_INCREMENT PRIMARY KEY,
    service_number VARCHAR(20),
    amount DOUBLE,
    payment_date DATE,
    discount_bills_number int,
    discount_number varchar(20),
    FOREIGN KEY (service_number) REFERENCES customers(service_number)
);

select * from advance_payments;

Drop table advance_payments;



-- ------------------------------------------------------------------------------------------------------------------------------------------------------------

-- 10. Procedure for payment

DELIMITER $$

CREATE PROCEDURE AddPayment(
    IN p_bill_id INT,
    IN p_payment_method VARCHAR(50),
    IN p_amount_paid DECIMAL(10,2)
)
BEGIN
    INSERT INTO payments (bill_id, payment_date, payment_method, amount_paid)
    VALUES (p_bill_id, CURDATE(), p_payment_method, p_amount_paid);
END $$

DELIMITER ;


-- ------------------------------------------------------------------------------------------------------------------------------------------------------------

-- 11. Trigger when enter a reading take previous reading


DELIMITER $$

CREATE FUNCTION get_last_reading(mId INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE lastReading DECIMAL(10,2);

    SELECT current_reading
    INTO lastReading
    FROM readings
    WHERE meter_id = mId
    ORDER BY reading_date DESC
    LIMIT 1;

    RETURN IFNULL(lastReading, 0);
END$$

DELIMITER ;
 

DELIMITER $$

CREATE TRIGGER set_previous_reading
BEFORE INSERT ON readings
FOR EACH ROW
BEGIN
    SET NEW.previous_reading = get_last_reading(NEW.meter_id);
END$$

DELIMITER ;



-- ------------------------------------------------------------------------------------------------------------------------------------------------------------

-- 12. Trigger when enter a tuple in bills table

DELIMITER $$

CREATE TRIGGER trg_before_bill_insert
BEFORE INSERT ON bills
FOR EACH ROW
BEGIN
    DECLARE remainingBills INT DEFAULT 0;
    DECLARE discountRate DECIMAL(5,2) DEFAULT 0;

    -- Check advance payment
    SELECT discount_bills_number, discount_number
    INTO remainingBills, discountRate
    FROM advance_payments
    WHERE service_number = NEW.service_number
    LIMIT 1;

    -- Apply discount if eligible
    IF remainingBills > 0 THEN
        SET NEW.amount = NEW.amount - (NEW.amount * discountRate / 100);

        -- Reduce advance count
        UPDATE advance_payments
        SET discount_bills_number = discount_bills_number - 1
        WHERE service_number = NEW.service_number;
    END IF;
END$$

DELIMITER ;


DROP TRIGGER IF EXISTS trg_after_bill_insert;
DROP procedure HandleAdvancePayment;
SHOW TRIGGERS;

