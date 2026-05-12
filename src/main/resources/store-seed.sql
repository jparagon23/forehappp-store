-- =====================
-- ROLES
-- =====================
INSERT IGNORE INTO store_roles (role_name) VALUES
('CUSTOMER'),
('SELLER'),
('STORE_ADMIN');

-- =====================
-- CATEGORIES
-- =====================
INSERT IGNORE INTO store_categories (category_id, description) VALUES
(1,  'Raquetas'),
(2,  'Cuerdas'),
(3,  'Pelotas'),
(4,  'Zapatillas de Tenis'),
(5,  'Ropa'),
(6,  'Bolsos y Mochilas'),
(7,  'Grips y Overgrips'),
(8,  'Antivibradores'),
(9,  'Equipamiento de Entrenamiento'),
(10, 'Accesorios');

-- =====================
-- BRANDS
-- =====================
INSERT IGNORE INTO store_brands (brand_id, description) VALUES
(1,  'Wilson'),
(2,  'Babolat'),
(3,  'Head'),
(4,  'Yonex'),
(5,  'Prince'),
(6,  'Tecnifibre'),
(7,  'Dunlop'),
(8,  'Nike'),
(9,  'Adidas'),
(10, 'Asics'),
(11, 'New Balance'),
(12, 'K-Swiss');

-- =====================
-- LINES  (brand + category)
-- =====================

-- Wilson - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(1,  1, 1, 'Pro Staff'),
(2,  1, 1, 'Blade'),
(3,  1, 1, 'Clash'),
(4,  1, 1, 'Ultra'),
(5,  1, 1, 'Burn');

-- Babolat - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(6,  2, 1, 'Pure Aero'),
(7,  2, 1, 'Pure Drive'),
(8,  2, 1, 'Pure Strike'),
(9,  2, 1, 'Pure Control');

-- Head - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(10, 3, 1, 'Speed'),
(11, 3, 1, 'Radical'),
(12, 3, 1, 'Prestige'),
(13, 3, 1, 'Extreme'),
(14, 3, 1, 'Gravity');

-- Yonex - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(15, 4, 1, 'EZONE'),
(16, 4, 1, 'VCORE'),
(17, 4, 1, 'ASTREL');

-- Prince - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(18, 5, 1, 'Phantom'),
(19, 5, 1, 'Textreme'),
(20, 5, 1, 'ATS');

-- Tecnifibre - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(21, 6, 1, 'TFight'),
(22, 6, 1, 'TFlash'),
(23, 6, 1, 'T-Rebound');

-- Dunlop - Raquetas
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(24, 7, 1, 'CX'),
(25, 7, 1, 'SX'),
(26, 7, 1, 'FX');

-- Nike - Zapatillas de Tenis
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(27, 8, 4, 'Court');

-- Nike - Ropa
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(28, 8, 5, 'NikeCourt Dri-FIT');

-- Adidas - Zapatillas de Tenis
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(29, 9, 4, 'Adizero'),
(30, 9, 4, 'Barricade');

-- Adidas - Ropa
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(31, 9, 5, 'Adidas Tennis Apparel');

-- Asics - Zapatillas de Tenis
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(32, 10, 4, 'Gel-Resolution'),
(33, 10, 4, 'Gel-Challenger'),
(34, 10, 4, 'Solution Speed');

-- New Balance - Zapatillas de Tenis
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(35, 11, 4, 'Fresh Foam Lav'),
(36, 11, 4, '996'),
(37, 11, 4, '806');

-- K-Swiss - Zapatillas de Tenis
INSERT IGNORE INTO store_lines (line_id, brand_id, category_id, description) VALUES
(38, 12, 4, 'Hypercourt'),
(39, 12, 4, 'Ultrashot');
