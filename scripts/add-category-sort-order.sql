-- Migration: add sort_order to store_categories
-- Run once against the production database before deploying this version.

ALTER TABLE store_categories
    ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 999;

-- Set tennis-specific order for existing categories.
-- Lower value = shown first in discovery sections.
-- Uses multiples of 10 so new categories can be inserted in-between without renumbering.
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
