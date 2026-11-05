-- Ejecutar con psql conectado a una base administrativa (por ejemplo postgres).
-- \gexec es una instrucción de psql: crea solo las bases que aún no existen.
SELECT 'CREATE DATABASE auth_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_service_db') \gexec
SELECT 'CREATE DATABASE user_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_service_db') \gexec
SELECT 'CREATE DATABASE product_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'product_service_db') \gexec
SELECT 'CREATE DATABASE category_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'category_service_db') \gexec
SELECT 'CREATE DATABASE inventory_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_service_db') \gexec
SELECT 'CREATE DATABASE order_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_service_db') \gexec
SELECT 'CREATE DATABASE payment_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_service_db') \gexec
SELECT 'CREATE DATABASE shipping_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shipping_service_db') \gexec
SELECT 'CREATE DATABASE promotion_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'promotion_service_db') \gexec
SELECT 'CREATE DATABASE review_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'review_service_db') \gexec
SELECT 'CREATE DATABASE warranty_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'warranty_service_db') \gexec
SELECT 'CREATE DATABASE notification_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_service_db') \gexec
