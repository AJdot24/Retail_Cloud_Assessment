-- =====================================================================
-- Seed data for PostgreSQL (same employees/departments as data.sql).
-- The only difference from the H2 script: identity sequences are
-- advanced with ALTER SEQUENCE instead of H2's ALTER COLUMN RESTART WITH.
-- =====================================================================

INSERT INTO department (id, name, creation_date) VALUES
    (1, 'Engineering',        '2020-04-01'),
    (2, 'Sales',              '2020-06-15'),
    (3, 'Human Resources',    '2020-08-01');

INSERT INTO employee (id, name, date_of_birth, salary, department_id, address, role_title, joining_date, yearly_bonus_percentage, reporting_manager_id) VALUES
    (1,  'Aarav Sharma',       '1980-05-14', 4500000.00, 1, 'Mumbai, Maharashtra',        'Chief Executive Officer',     '2019-01-15', 20.00, NULL),
    (2,  'Priya Nair',         '1986-02-21', 2400000.00, 1, 'Bengaluru, Karnataka',       'Engineering Manager',         '2020-04-01', 15.00, 1),
    (3,  'Rohan Mehta',        '1985-11-03', 2200000.00, 2, 'Mumbai, Maharashtra',        'Sales Manager',               '2020-06-15', 15.00, 1),
    (4,  'Ananya Iyer',        '1988-07-19', 1900000.00, 3, 'Pune, Maharashtra',          'HR Manager',                  '2020-08-01', 15.00, 1);

INSERT INTO employee (id, name, date_of_birth, salary, department_id, address, role_title, joining_date, yearly_bonus_percentage, reporting_manager_id) VALUES
    (5,  'Vikram Reddy',       '1992-03-11', 1800000.00, 1, 'Bengaluru, Karnataka',       'Senior Software Engineer',    '2021-01-11', 12.00, 2),
    (6,  'Sneha Kulkarni',     '1993-08-25', 1600000.00, 1, 'Pune, Maharashtra',          'Senior Software Engineer',    '2021-03-22', 12.00, 2),
    (7,  'Arjun Menon',        '1996-01-17', 1200000.00, 1, 'Kochi, Kerala',              'Software Engineer',           '2022-02-14', 10.00, 2),
    (8,  'Divya Krishnan',     '1997-06-09', 1150000.00, 1, 'Chennai, Tamil Nadu',        'Software Engineer',           '2022-05-02', 10.00, 2),
    (9,  'Karthik Rao',        '1998-11-30', 1000000.00, 1, 'Hyderabad, Telangana',       'Software Engineer',           '2023-01-09', 8.00,  2),
    (10, 'Meera Pillai',       '1995-04-12', 950000.00,  1, 'Thiruvananthapuram, Kerala', 'QA Engineer',                 '2022-07-18', 8.00,  2),
    (11, 'Nikhil Joshi',       '1999-09-05', 850000.00,  1, 'Indore, Madhya Pradesh',     'QA Engineer',                 '2023-06-12', 8.00,  2);

INSERT INTO employee (id, name, date_of_birth, salary, department_id, address, role_title, joining_date, yearly_bonus_percentage, reporting_manager_id) VALUES
    (12, 'Sanya Kapoor',       '1991-12-08', 1500000.00, 2, 'Delhi',                      'Account Manager',             '2021-04-19', 12.00, 3),
    (13, 'Aditya Deshmukh',    '1992-07-23', 1350000.00, 2, 'Pune, Maharashtra',          'Account Manager',             '2021-08-02', 12.00, 3),
    (14, 'Isha Malhotra',      '1996-02-14', 950000.00,  2, 'Gurugram, Haryana',          'Sales Executive',             '2022-03-07', 10.00, 3),
    (15, 'Rahul Verma',        '1997-05-28', 900000.00,  2, 'Noida, Uttar Pradesh',       'Sales Executive',             '2022-09-26', 10.00, 3),
    (16, 'Tanvi Shah',         '1998-10-16', 850000.00,  2, 'Ahmedabad, Gujarat',         'Sales Executive',             '2023-02-20', 8.00,  3),
    (17, 'Yash Agarwal',       '1999-03-03', 800000.00,  2, 'Jaipur, Rajasthan',          'Sales Executive',             '2023-07-17', 8.00,  3),
    (18, 'Pooja Bhatt',        '1994-01-22', 1050000.00, 2, 'Mumbai, Maharashtra',        'Sales Operations Analyst',    '2022-01-10', 10.00, 3);

INSERT INTO employee (id, name, date_of_birth, salary, department_id, address, role_title, joining_date, yearly_bonus_percentage, reporting_manager_id) VALUES
    (19, 'Ritika Saxena',      '1993-09-09', 1100000.00, 3, 'Bengaluru, Karnataka',       'Senior HR Executive',         '2021-05-03', 10.00, 4),
    (20, 'Manish Tiwari',      '1995-06-27', 950000.00,  3, 'Lucknow, Uttar Pradesh',     'HR Executive',                '2022-04-25', 8.00,  4),
    (21, 'Neha Gupta',         '1997-01-30', 900000.00,  3, 'Delhi',                      'HR Executive',                '2022-10-17', 8.00,  4),
    (22, 'Suresh Nair',        '1996-08-12', 880000.00,  3, 'Kochi, Kerala',              'Recruiter',                   '2022-11-21', 8.00,  4),
    (23, 'Kavya Rao',          '1998-04-04', 820000.00,  3, 'Hyderabad, Telangana',       'Recruiter',                   '2023-03-13', 8.00,  4),
    (24, 'Deepak Chauhan',     '1992-12-19', 980000.00,  3, 'Chandigarh',                 'Payroll Specialist',          '2021-09-06', 10.00, 4),
    (25, 'Shreya Das',         '1999-07-08', 780000.00,  3, 'Kolkata, West Bengal',       'Training Coordinator',        '2023-08-28', 8.00,  4);

UPDATE department SET department_head_id = 2 WHERE id = 1;
UPDATE department SET department_head_id = 3 WHERE id = 2;
UPDATE department SET department_head_id = 4 WHERE id = 3;

-- Advance the identity sequences past the explicitly inserted ids,
-- otherwise the next Hibernate insert would collide with a seeded key
ALTER SEQUENCE department_id_seq RESTART WITH 4;
ALTER SEQUENCE employee_id_seq RESTART WITH 26;
