INSERT INTO bank_accounts (account_number, account_holder_name, account_type, balance, created_at)
VALUES
('ACC1001', 'Rahul Sharma', 'SAVINGS', 5000.00, '2024-01-01 10:00:00'),
('ACC1002', 'Priya Patel', 'CURRENT', 10000.00, '2024-01-02 11:30:00'),
('ACC1003', 'Amit Verma', 'SAVINGS', 7500.00, '2024-01-03 09:15:00'),
('ACC1004', 'Sonam Kapoor', 'SAVINGS', 12000.00, '2024-01-04 14:20:00'),
('ACC1005', 'Rajesh Khanna', 'CURRENT', 8500.00, '2024-01-05 16:45:00'),
('ACC1006', 'Anjali Singh', 'SAVINGS', 6000.00, '2024-01-06 09:30:00'),
('ACC1007', 'Vikram Malhotra', 'CURRENT', 9500.00, '2024-01-07 13:15:00');

INSERT INTO transactions (transaction_id, transaction_type, amount, description, transaction_date, account_id)
VALUES
('TXN1001', 'DEPOSIT', 2000.00, 'Initial deposit', '2024-01-01 10:00:00', 1),
('TXN1002', 'DEPOSIT', 3000.00, 'Salary credit', '2024-01-05 12:00:00', 1),
('TXN1003', 'WITHDRAWAL', 1000.00, 'ATM withdrawal', '2024-01-10 15:30:00', 1),
('TXN1004', 'DEPOSIT', 5000.00, 'Business payment', '2024-01-02 11:30:00', 2),
('TXN1005', 'DEPOSIT', 5000.00, 'Investment return', '2024-01-08 14:45:00', 2),
('TXN1006', 'DEPOSIT', 4500.00, 'Freelancing payment', '2024-01-03 09:15:00', 3),
('TXN1007', 'WITHDRAWAL', 2000.00, 'House rent', '2024-01-12 10:30:00', 3),
('TXN1008', 'DEPOSIT', 7000.00, 'Movie payment', '2024-01-04 14:20:00', 4),
('TXN1009', 'WITHDRAWAL', 1500.00, 'Shopping', '2024-01-15 16:00:00', 4);