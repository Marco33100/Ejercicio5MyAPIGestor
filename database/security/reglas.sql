-- Casos negativos sinteticos: nunca se ejecutan contra MySQL.
# ruleid: db-no-drop-database
DROP DATABASE ejemplo;
# ruleid: db-no-drop-table
DROP TABLE ejemplo;
# ruleid: db-no-truncate
TRUNCATE TABLE ejemplo;
# ruleid: db-no-global-grants
GRANT SELECT ON *.* TO 'cuenta'@'%';
# ruleid: db-no-inline-password
CREATE USER 'ejemplo'@'%' IDENTIFIED BY 'valor-sintetico-de-prueba';
# ruleid: db-no-disabled-foreign-keys
SET FOREIGN_KEY_CHECKS=0;
# ok: db-no-drop-database,db-no-drop-table,db-no-truncate,db-no-global-grants,db-no-inline-password,db-no-disabled-foreign-keys
CREATE TABLE IF NOT EXISTS ejemplo (id INT PRIMARY KEY);
