-- Migration: add sort_order to store_categories
-- Compatible with MySQL (Workbench). Run once before deploying this version.

-- Add column only if it does not already exist
SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'store_categories'
      AND COLUMN_NAME  = 'sort_order'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE store_categories ADD COLUMN sort_order INT NOT NULL DEFAULT 999',
    'SELECT ''sort_order already exists, skipping ALTER'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Set tennis-specific order for existing categories.
-- Lower value = shown first in discovery sections.
-- Multiples of 10 allow inserting new categories in-between without renumbering.
UPDATE store_categories SET sort_order = 10  WHERE category_id = 7;  -- Grips y Overgrips
UPDATE store_categories SET sort_order = 20  WHERE category_id = 3;  -- Pelotas
UPDATE store_categories SET sort_order = 30  WHERE category_id = 1;  -- Raquetas
UPDATE store_categories SET sort_order = 40  WHERE category_id = 2;  -- Cuerdas
UPDATE store_categories SET sort_order = 50  WHERE category_id = 8;  -- Antivibradores
UPDATE store_categories SET sort_order = 60  WHERE category_id = 4;  -- Zapatillas de Tenis
UPDATE store_categories SET sort_order = 70  WHERE category_id = 5;  -- Ropa
UPDATE store_categories SET sort_order = 80  WHERE category_id = 6;  -- Bolsos y Mochilas
UPDATE store_categories SET sort_order = 90  WHERE category_id = 9;  -- Equipamiento de Entrenamiento
UPDATE store_categories SET sort_order = 100 WHERE category_id = 10; -- Accesorios
