-- ============================================================
-- PREREQUISITOS: store-seed.sql debe haberse ejecutado primero.
-- Requiere al menos un perfil vendedor (store_profile_id = 1).
-- ============================================================

-- =====================
-- ATRIBUTOS
-- =====================
INSERT IGNORE INTO store_attributes (attribute_id, description) VALUES
(1, 'Tamaño de Grip'),
(2, 'Grosor de Cuerda'),
(3, 'Tamaño de Paquete'),
(4, 'Talla de Zapatilla'),
(5, 'Color'),
(6, 'Talla de Ropa');

-- =====================
-- VALORES DE ATRIBUTO
-- =====================

-- Tamaño de Grip (1)
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(1,  1, 'L1 (4 1/8")'),
(2,  1, 'L2 (4 1/4")'),
(3,  1, 'L3 (4 3/8")'),
(4,  1, 'L4 (4 1/2")');

-- Grosor de Cuerda (2)
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(5,  2, '1.15mm'),
(6,  2, '1.20mm'),
(7,  2, '1.25mm'),
(8,  2, '1.30mm');

-- Tamaño de Paquete (3)
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(9,  3, '3 Pelotas'),
(10, 3, '4 Pelotas');

-- Talla de Zapatilla (4) — tallas EU
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(11, 4, '38'),
(12, 4, '39'),
(13, 4, '40'),
(14, 4, '41'),
(15, 4, '42'),
(16, 4, '43'),
(17, 4, '44'),
(18, 4, '45');

-- Color (5)
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(19, 5, 'Negro'),
(20, 5, 'Blanco'),
(21, 5, 'Azul'),
(22, 5, 'Rojo'),
(23, 5, 'Amarillo');

-- Talla de Ropa (6)
INSERT IGNORE INTO store_attribute_values (attribute_value_id, attribute_id, description) VALUES
(24, 6, 'XS'),
(25, 6, 'S'),
(26, 6, 'M'),
(27, 6, 'L'),
(28, 6, 'XL'),
(29, 6, 'XXL');

-- =====================
-- CATEGORÍA → ATRIBUTOS
-- =====================
-- Raquetas (1):    Tamaño de Grip
-- Cuerdas  (2):    Grosor de Cuerda
-- Pelotas  (3):    Tamaño de Paquete
-- Zapatillas (4):  Talla de Zapatilla + Color
-- Ropa     (5):    Talla de Ropa + Color
INSERT IGNORE INTO store_category_attributes (category_id, attribute_id, required) VALUES
(1, 1, 'Y'),
(2, 2, 'Y'),
(3, 3, 'Y'),
(4, 4, 'Y'),
(4, 5, 'Y'),
(5, 6, 'Y'),
(5, 5, 'Y');

-- =====================
-- PRODUCTOS
-- =====================
-- seller_id = 1 (ajustar al store_profile_id real si es necesario)
INSERT IGNORE INTO store_products (product_id, seller_id, title, description, brand_id, line_id, category_id, status, created_at) VALUES
(1, 1, 'Wilson Pro Staff 97',       'Raqueta de control usada por Roger Federer',          1,  1,  1, 'ACTIVE', NOW()),
(2, 1, 'Babolat Pure Aero',         'Raqueta de topspin usada por Rafael Nadal',           2,  6,  1, 'ACTIVE', NOW()),
(3, 1, 'Babolat RPM Blast 200m',    'Cuerda de poliéster co-poly, rollo de 200m',          2, NULL, 2, 'ACTIVE', NOW()),
(4, 1, 'Wilson US Open Extra Duty', 'Pelotas de tenis para superficie dura, oficial US Open', 1, NULL, 3, 'ACTIVE', NOW()),
(5, 1, 'Nike Air Zoom Vapor Pro',   'Zapatilla liviana para superficie dura',              8, 27,  4, 'ACTIVE', NOW()),
(6, 1, 'Nike Dri-FIT Advantage Tee','Camiseta de entrenamiento con tecnología Dri-FIT',   8, 28,  5, 'ACTIVE', NOW());

-- =====================
-- VARIANTES
-- =====================
INSERT IGNORE INTO store_product_variants (variant_id, product_id, sku, price, compare_at_price, stock, created_at) VALUES
-- Wilson Pro Staff 97 — tamaños de grip
(1,  1, 'WILS-PS97-L1', 229.99, 259.99, 10, NOW()),
(2,  1, 'WILS-PS97-L2', 229.99, 259.99, 15, NOW()),
(3,  1, 'WILS-PS97-L3', 229.99, 259.99, 12, NOW()),
(4,  1, 'WILS-PS97-L4', 229.99, 259.99,  8, NOW()),

-- Babolat Pure Aero — tamaños de grip
(5,  2, 'BABL-PA-L1',   249.99, NULL,   10, NOW()),
(6,  2, 'BABL-PA-L2',   249.99, NULL,   14, NOW()),
(7,  2, 'BABL-PA-L3',   249.99, NULL,   11, NOW()),

-- Babolat RPM Blast — grosores de cuerda
(8,  3, 'BABL-RPM-115', 34.99,  NULL,   20, NOW()),
(9,  3, 'BABL-RPM-120', 34.99,  NULL,   25, NOW()),
(10, 3, 'BABL-RPM-125', 34.99,  NULL,   30, NOW()),
(11, 3, 'BABL-RPM-130', 34.99,  NULL,   18, NOW()),

-- Wilson US Open — tamaños de paquete
(12, 4, 'WILS-USO-3',   8.99,   NULL,   50, NOW()),
(13, 4, 'WILS-USO-4',   11.99,  NULL,   40, NOW()),

-- Nike Air Zoom Vapor Pro — talla + color (Negro, Blanco)
(14, 5, 'NIKE-AZV-40-NG', 139.99, 159.99, 6, NOW()),
(15, 5, 'NIKE-AZV-41-NG', 139.99, 159.99, 8, NOW()),
(16, 5, 'NIKE-AZV-42-NG', 139.99, 159.99, 9, NOW()),
(17, 5, 'NIKE-AZV-43-NG', 139.99, 159.99, 7, NOW()),
(18, 5, 'NIKE-AZV-40-BC', 139.99, 159.99, 5, NOW()),
(19, 5, 'NIKE-AZV-41-BC', 139.99, 159.99, 6, NOW()),
(20, 5, 'NIKE-AZV-42-BC', 139.99, 159.99, 8, NOW()),
(21, 5, 'NIKE-AZV-43-BC', 139.99, 159.99, 6, NOW()),

-- Nike Dri-FIT Tee — talla + color (Negro, Blanco, Azul)
(22, 6, 'NIKE-DFIT-S-NG',  39.99, NULL, 15, NOW()),
(23, 6, 'NIKE-DFIT-M-NG',  39.99, NULL, 20, NOW()),
(24, 6, 'NIKE-DFIT-L-NG',  39.99, NULL, 18, NOW()),
(25, 6, 'NIKE-DFIT-XL-NG', 39.99, NULL, 12, NOW()),
(26, 6, 'NIKE-DFIT-S-BC',  39.99, NULL, 14, NOW()),
(27, 6, 'NIKE-DFIT-M-BC',  39.99, NULL, 19, NOW()),
(28, 6, 'NIKE-DFIT-L-BC',  39.99, NULL, 16, NOW()),
(29, 6, 'NIKE-DFIT-M-AZ',  39.99, NULL, 11, NOW()),
(30, 6, 'NIKE-DFIT-L-AZ',  39.99, NULL, 10, NOW());

-- =====================================================================
-- store_product_variant_attribute_values
-- Relaciona cada variante con sus valores de atributo
-- =====================================================================
INSERT IGNORE INTO store_product_variant_attribute_values (variant_id, attribute_value_id) VALUES

-- Wilson Pro Staff 97 — un atributo: Tamaño de Grip
(1,  1),   -- L1
(2,  2),   -- L2
(3,  3),   -- L3
(4,  4),   -- L4

-- Babolat Pure Aero — un atributo: Tamaño de Grip
(5,  1),   -- L1
(6,  2),   -- L2
(7,  3),   -- L3

-- Babolat RPM Blast — un atributo: Grosor de Cuerda
(8,  5),   -- 1.15mm
(9,  6),   -- 1.20mm
(10, 7),   -- 1.25mm
(11, 8),   -- 1.30mm

-- Wilson US Open — un atributo: Tamaño de Paquete
(12, 9),   -- 3 Pelotas
(13, 10),  -- 4 Pelotas

-- Nike Air Zoom Vapor Pro — dos atributos: Talla de Zapatilla + Color
(14, 13), (14, 19),   -- 40 + Negro
(15, 14), (15, 19),   -- 41 + Negro
(16, 15), (16, 19),   -- 42 + Negro
(17, 16), (17, 19),   -- 43 + Negro
(18, 13), (18, 20),   -- 40 + Blanco
(19, 14), (19, 20),   -- 41 + Blanco
(20, 15), (20, 20),   -- 42 + Blanco
(21, 16), (21, 20),   -- 43 + Blanco

-- Nike Dri-FIT Tee — dos atributos: Talla de Ropa + Color
(22, 25), (22, 19),   -- S  + Negro
(23, 26), (23, 19),   -- M  + Negro
(24, 27), (24, 19),   -- L  + Negro
(25, 28), (25, 19),   -- XL + Negro
(26, 25), (26, 20),   -- S  + Blanco
(27, 26), (27, 20),   -- M  + Blanco
(28, 27), (28, 20),   -- L  + Blanco
(29, 26), (29, 21),   -- M  + Azul
(30, 27), (30, 21);   -- L  + Azul

-- =====================
-- MOVIMIENTOS DE INVENTARIO
-- Entradas iniciales de stock por variante
-- =====================
INSERT IGNORE INTO store_inventory_movements (variant_id, quantity, reason, created_at) VALUES
(1,  10, 'RESTOCK', NOW()), (2,  15, 'RESTOCK', NOW()), (3,  12, 'RESTOCK', NOW()), (4,   8, 'RESTOCK', NOW()),
(5,  10, 'RESTOCK', NOW()), (6,  14, 'RESTOCK', NOW()), (7,  11, 'RESTOCK', NOW()),
(8,  20, 'RESTOCK', NOW()), (9,  25, 'RESTOCK', NOW()), (10, 30, 'RESTOCK', NOW()), (11, 18, 'RESTOCK', NOW()),
(12, 50, 'RESTOCK', NOW()), (13, 40, 'RESTOCK', NOW()),
(14,  6, 'RESTOCK', NOW()), (15,  8, 'RESTOCK', NOW()), (16,  9, 'RESTOCK', NOW()), (17,  7, 'RESTOCK', NOW()),
(18,  5, 'RESTOCK', NOW()), (19,  6, 'RESTOCK', NOW()), (20,  8, 'RESTOCK', NOW()), (21,  6, 'RESTOCK', NOW()),
(22, 15, 'RESTOCK', NOW()), (23, 20, 'RESTOCK', NOW()), (24, 18, 'RESTOCK', NOW()), (25, 12, 'RESTOCK', NOW()),
(26, 14, 'RESTOCK', NOW()), (27, 19, 'RESTOCK', NOW()), (28, 16, 'RESTOCK', NOW()),
(29, 11, 'RESTOCK', NOW()), (30, 10, 'RESTOCK', NOW());
