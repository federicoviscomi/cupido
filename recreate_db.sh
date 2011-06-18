#!/bin/bash

echo "If this is the first run, enter an empty MySQL password."
echo "In subsequent runs, enter 'cupido' as password (without quotes)."
echo
echo "Deleting the existing DB (if any)..."
{
mysql -u root -p <<"EOF"
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('cupido');
DROP DATABASE cupido;
EOF

} &>/dev/null || echo "Warning: error while deleting the DB, continuing anyway..."

echo
echo "Creating the new DB..."
{
mysql -u root -p <<"EOF"
CREATE DATABASE cupido;
USE cupido;
CREATE TABLE `cupido`.`User` (
  `name` VARCHAR(16)  NOT NULL,
  `password` CHAR(8) UNICODE NOT NULL,
  `score` INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`),
  INDEX `scoreIndex`(`score`, `name`)
);
EOF

} || exit 1

echo "The DB has been created successfully."
