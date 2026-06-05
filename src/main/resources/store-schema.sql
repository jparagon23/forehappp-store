CREATE TABLE IF NOT EXISTS store_confirmation_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255),
    created_at  DATETIME,
    expires_at  DATETIME,
    confirmed_at DATETIME,
    user_id     BIGINT NOT NULL,
    CONSTRAINT store_fk_sct_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS store_fcm_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(512) NOT NULL,
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    CONSTRAINT uk_store_fcm_token UNIQUE (token)
);

CREATE TABLE IF NOT EXISTS store_push_subscriptions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    endpoint        VARCHAR(2048) NOT NULL,
    p256dh          VARCHAR(512) NOT NULL,
    auth            VARCHAR(512) NOT NULL,
    expiration_time BIGINT,
    active          TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS store_profiles (
    store_profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    phone            VARCHAR(50),
    loyalty_points   INT NOT NULL DEFAULT 0,
    active           TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_store_profile_user UNIQUE (user_id),
    CONSTRAINT store_fk_sp_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS store_profile_addresses (
    address_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_profile_id BIGINT NOT NULL,
    alias            VARCHAR(100),
    street           VARCHAR(255) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    state            VARCHAR(100),
    country          VARCHAR(100) NOT NULL,
    zip_code         VARCHAR(20),
    is_default       TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT store_fk_spa_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_roles (
    role_name VARCHAR(20) PRIMARY KEY
);

INSERT IGNORE INTO store_roles (role_name) VALUES ('CUSTOMER'), ('SELLER'), ('STORE_ADMIN');

CREATE TABLE IF NOT EXISTS store_profile_roles (
    store_profile_id BIGINT      NOT NULL,
    role             VARCHAR(20) NOT NULL,
    CONSTRAINT store_fk_spr_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id),
    CONSTRAINT store_fk_spr_role    FOREIGN KEY (role)             REFERENCES store_roles(role_name)
);

-- =====================
-- Store Management
-- =====================
CREATE TABLE IF NOT EXISTS stores (
    store_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(100) NOT NULL,
    description TEXT,
    logo_s3_key VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME NOT NULL,
    CONSTRAINT uk_store_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS store_memberships (
    membership_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id         BIGINT NOT NULL,
    store_profile_id BIGINT NOT NULL,
    role             VARCHAR(20) NOT NULL,
    joined_at        DATETIME NOT NULL,
    active           TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_store_membership UNIQUE (store_id, store_profile_id),
    CONSTRAINT store_fk_sm_store   FOREIGN KEY (store_id)         REFERENCES stores(store_id),
    CONSTRAINT store_fk_sm_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_brands (
    brand_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS store_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS store_attributes (
    attribute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description  VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS store_lines (
    line_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(150) NOT NULL,
    CONSTRAINT store_fk_line_brand    FOREIGN KEY (brand_id)    REFERENCES store_brands(brand_id),
    CONSTRAINT store_fk_line_category FOREIGN KEY (category_id) REFERENCES store_categories(category_id)
);

CREATE TABLE IF NOT EXISTS store_attribute_values (
    attribute_value_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attribute_id       BIGINT NOT NULL,
    description        VARCHAR(255) NOT NULL,
    CONSTRAINT store_fk_av_attribute FOREIGN KEY (attribute_id) REFERENCES store_attributes(attribute_id)
);

CREATE TABLE IF NOT EXISTS store_category_attributes (
    category_attribute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id           BIGINT NOT NULL,
    attribute_id          BIGINT NOT NULL,
    required              VARCHAR(1) NOT NULL,
    CONSTRAINT store_uk_cat_attr    UNIQUE (category_id, attribute_id),
    CONSTRAINT store_fk_ca_category FOREIGN KEY (category_id)  REFERENCES store_categories(category_id),
    CONSTRAINT store_fk_ca_attr     FOREIGN KEY (attribute_id) REFERENCES store_attributes(attribute_id)
);

CREATE TABLE IF NOT EXISTS store_products (
    product_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id     BIGINT NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    brand_id     BIGINT NOT NULL,
    line_id      BIGINT,
    category_id  BIGINT NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME NOT NULL,
    CONSTRAINT store_fk_prod_store    FOREIGN KEY (store_id)    REFERENCES stores(store_id),
    CONSTRAINT store_fk_prod_brand    FOREIGN KEY (brand_id)    REFERENCES store_brands(brand_id),
    CONSTRAINT store_fk_prod_line     FOREIGN KEY (line_id)     REFERENCES store_lines(line_id),
    CONSTRAINT store_fk_prod_category FOREIGN KEY (category_id) REFERENCES store_categories(category_id)
);

CREATE TABLE IF NOT EXISTS store_product_images (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT NOT NULL,
    s3_key        VARCHAR(500) NOT NULL,
    url           VARCHAR(1000) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at    DATETIME NOT NULL,
    CONSTRAINT store_fk_pi_product FOREIGN KEY (product_id) REFERENCES store_products(product_id)
);

CREATE TABLE IF NOT EXISTS store_product_variants (
    variant_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    sku              VARCHAR(100) NULL,
    price            DECIMAL(14, 2) NOT NULL,
    compare_at_price DECIMAL(14, 2),
    stock            INT NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL,
    CONSTRAINT store_uk_variant_sku UNIQUE (sku),
    CONSTRAINT store_fk_pv_product  FOREIGN KEY (product_id) REFERENCES store_products(product_id)
);

CREATE TABLE IF NOT EXISTS store_product_variant_attribute_values (
    variant_id         BIGINT NOT NULL,
    attribute_value_id BIGINT NOT NULL,
    PRIMARY KEY (variant_id, attribute_value_id),
    CONSTRAINT store_fk_pvav_variant  FOREIGN KEY (variant_id)         REFERENCES store_product_variants(variant_id),
    CONSTRAINT store_fk_pvav_attr_val FOREIGN KEY (attribute_value_id) REFERENCES store_attribute_values(attribute_value_id)
);

CREATE TABLE IF NOT EXISTS store_inventory_movements (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    variant_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    reason     VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT store_fk_im_variant FOREIGN KEY (variant_id) REFERENCES store_product_variants(variant_id)
);

CREATE TABLE IF NOT EXISTS store_orders (
    order_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id         BIGINT NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total            DECIMAL(14,2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    shipping_city    VARCHAR(100) NOT NULL,
    shipping_country VARCHAR(100) NOT NULL,
    created_at       DATETIME NOT NULL,
    CONSTRAINT store_fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_payments (
    payment_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    method       VARCHAR(100) NOT NULL,
    status       VARCHAR(50) NOT NULL,
    amount       DECIMAL(15,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    reference    VARCHAR(255),
    CONSTRAINT store_fk_payment_order FOREIGN KEY (order_id) REFERENCES store_orders(order_id)
);

CREATE TABLE IF NOT EXISTS store_order_seller_groups (
    group_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    store_id        BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal        DECIMAL(14,2) NOT NULL,
    tracking_number VARCHAR(100),
    prepared_at     DATETIME,
    shipped_at      DATETIME,
    delivered_at    DATETIME,
    CONSTRAINT store_fk_osg_order FOREIGN KEY (order_id) REFERENCES store_orders(order_id),
    CONSTRAINT store_fk_osg_store FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN tracking_number VARCHAR(100)','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='tracking_number');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN prepared_at DATETIME','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='prepared_at');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN shipped_at DATETIME','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='shipped_at');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN delivered_at DATETIME','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='delivered_at');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN cancelled_at DATETIME','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='cancelled_at');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_order_seller_groups ADD COLUMN cancellation_reason VARCHAR(500)','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='cancellation_reason');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_orders ADD COLUMN payment_method VARCHAR(30) NOT NULL DEFAULT ''MERCADO_PAGO''','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_orders' AND COLUMN_NAME='payment_method');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE store_product_variants ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_product_variants' AND COLUMN_NAME='active');
PREPARE _stmt FROM @s;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

CREATE TABLE IF NOT EXISTS store_order_items (
    item_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id   BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    unit_price DECIMAL(14,2) NOT NULL,
    CONSTRAINT store_fk_oi_group   FOREIGN KEY (group_id)   REFERENCES store_order_seller_groups(group_id),
    CONSTRAINT store_fk_oi_variant FOREIGN KEY (variant_id) REFERENCES store_product_variants(variant_id)
);

CREATE TABLE IF NOT EXISTS store_carts (
    cart_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_profile_id BIGINT NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    creation_date    DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    CONSTRAINT store_fk_cart_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id      BIGINT NOT NULL,
    variant_id   BIGINT NOT NULL,
    quantity     INT NOT NULL,
    price_at_add DECIMAL(14, 2) NOT NULL,
    added_date   DATETIME NOT NULL,
    CONSTRAINT uk_cart_variant UNIQUE (cart_id, variant_id),
    CONSTRAINT store_fk_ci_cart      FOREIGN KEY (cart_id)    REFERENCES store_carts(cart_id),
    CONSTRAINT store_fk_ci_variant   FOREIGN KEY (variant_id) REFERENCES store_product_variants(variant_id)
);

CREATE TABLE IF NOT EXISTS store_wishlists (
    wishlist_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_profile_id BIGINT NOT NULL,
    created_at       DATETIME NOT NULL,
    CONSTRAINT uk_wishlist_profile UNIQUE (store_profile_id),
    CONSTRAINT store_fk_wl_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_wishlist_items (
    wishlist_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wishlist_id      BIGINT NOT NULL,
    product_id       BIGINT NOT NULL,
    added_at         DATETIME NOT NULL,
    CONSTRAINT uk_wishlist_product UNIQUE (wishlist_id, product_id),
    CONSTRAINT store_fk_wi_wishlist FOREIGN KEY (wishlist_id) REFERENCES store_wishlists(wishlist_id),
    CONSTRAINT store_fk_wi_product  FOREIGN KEY (product_id)  REFERENCES store_products(product_id)
);

CREATE TABLE IF NOT EXISTS store_product_reviews (
    review_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    store_profile_id BIGINT NOT NULL,
    rating           INT NOT NULL,
    title            VARCHAR(100),
    comment          VARCHAR(1000),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME,
    CONSTRAINT uk_review_product_profile UNIQUE (product_id, store_profile_id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT store_fk_review_product FOREIGN KEY (product_id)       REFERENCES store_products(product_id),
    CONSTRAINT store_fk_review_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_coupons (
    coupon_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id          BIGINT NOT NULL,
    code              VARCHAR(50) NOT NULL,
    description       VARCHAR(255),
    discount_type     VARCHAR(20) NOT NULL,
    discount_value    DECIMAL(14,2) NOT NULL,
    min_order_amount  DECIMAL(14,2),
    max_uses          INT,
    uses_count        INT NOT NULL DEFAULT 0,
    max_uses_per_user INT NOT NULL DEFAULT 1,
    valid_from        DATE NOT NULL,
    valid_until       DATE,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    created_at        DATETIME NOT NULL,
    CONSTRAINT uk_coupon_code UNIQUE (code),
    CONSTRAINT store_fk_coupon_store FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

CREATE TABLE IF NOT EXISTS store_coupon_redemptions (
    redemption_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id        BIGINT NOT NULL,
    store_profile_id BIGINT NOT NULL,
    order_id         BIGINT,
    discount_applied DECIMAL(14,2) NOT NULL,
    used_at          DATETIME NOT NULL,
    CONSTRAINT store_fk_cr_coupon  FOREIGN KEY (coupon_id)        REFERENCES store_coupons(coupon_id),
    CONSTRAINT store_fk_cr_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_return_requests (
    return_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id    BIGINT NOT NULL UNIQUE,
    buyer_id    BIGINT NOT NULL,
    return_type VARCHAR(30) NOT NULL,
    reason      VARCHAR(1000) NOT NULL,
    refund_amount DECIMAL(14,2),
    admin_notes VARCHAR(1000),
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME,
    CONSTRAINT store_fk_rr_group  FOREIGN KEY (group_id)  REFERENCES store_order_seller_groups(group_id),
    CONSTRAINT store_fk_rr_buyer  FOREIGN KEY (buyer_id)  REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_return_items (
    return_item_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id           BIGINT NOT NULL,
    order_item_id       BIGINT NOT NULL,
    quantity_to_return  INT NOT NULL,
    CONSTRAINT store_fk_ri_return     FOREIGN KEY (return_id)     REFERENCES store_return_requests(return_id),
    CONSTRAINT store_fk_ri_order_item FOREIGN KEY (order_item_id) REFERENCES store_order_items(item_id)
);

-- =====================
-- Migration: seller_id → store_id (existing DBs)
-- =====================

SET @s = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE store_products ADD COLUMN store_id BIGINT, ADD CONSTRAINT store_fk_prod_store FOREIGN KEY (store_id) REFERENCES stores(store_id)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_products' AND COLUMN_NAME='store_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_products DROP FOREIGN KEY store_fk_prod_seller',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_products' AND CONSTRAINT_NAME='store_fk_prod_seller');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_products DROP COLUMN seller_id',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_products' AND COLUMN_NAME='seller_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE store_order_seller_groups ADD COLUMN store_id BIGINT, ADD CONSTRAINT store_fk_osg_store FOREIGN KEY (store_id) REFERENCES stores(store_id)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='store_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_order_seller_groups DROP FOREIGN KEY store_fk_osg_seller',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND CONSTRAINT_NAME='store_fk_osg_seller');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_order_seller_groups DROP COLUMN seller_id',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_order_seller_groups' AND COLUMN_NAME='seller_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- Migrate store_coupons: seller_id → store_id
SET @s = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE store_coupons ADD COLUMN store_id BIGINT NOT NULL DEFAULT 0, ADD CONSTRAINT store_fk_coupon_store FOREIGN KEY (store_id) REFERENCES stores(store_id)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_coupons' AND COLUMN_NAME='store_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_coupons DROP FOREIGN KEY fk_coupon_seller',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_coupons' AND CONSTRAINT_NAME='fk_coupon_seller');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*)>0,
  'ALTER TABLE store_coupons DROP COLUMN seller_id',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='store_coupons' AND COLUMN_NAME='seller_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: Location Catalog (countries, states, cities)
-- =====================

CREATE TABLE IF NOT EXISTS countries (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    code   VARCHAR(3)   NOT NULL,
    active TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT uk_country_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS states (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    country_id BIGINT       NOT NULL,
    active     TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT store_fk_state_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

CREATE TABLE IF NOT EXISTS cities (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(150) NOT NULL,
    state_id BIGINT       NOT NULL,
    active   TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT store_fk_city_state FOREIGN KEY (state_id) REFERENCES states(id)
);

-- =====================
-- Migration: Shipping Zones
-- =====================

CREATE TABLE IF NOT EXISTS store_shipping_zones (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150)   NOT NULL,
    cost       DECIMAL(14, 2) NOT NULL,
    is_default TINYINT(1)     NOT NULL DEFAULT 0,
    active     TINYINT(1)     NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS store_shipping_zone_city_map (
    zone_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    PRIMARY KEY (zone_id, city_id),
    CONSTRAINT store_fk_szcm_zone FOREIGN KEY (zone_id) REFERENCES store_shipping_zones(id),
    CONSTRAINT store_fk_szcm_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- Add missing columns to location tables (table may have been created by Hibernate with incomplete schema)
SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE countries ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'countries' AND COLUMN_NAME = 'active');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE countries ADD COLUMN code VARCHAR(3) NOT NULL DEFAULT ''''',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'countries' AND COLUMN_NAME = 'code');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE states ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'states' AND COLUMN_NAME = 'active');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE cities ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cities' AND COLUMN_NAME = 'active');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- Drop old string-based shipping_zone_cities table if it exists
SET @s = (SELECT IF(COUNT(*) > 0,
  'DROP TABLE shipping_zone_cities',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipping_zone_cities');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_profile_addresses — replace city/state/country strings with city_id FK
-- =====================

-- Add city_id (nullable so existing rows without city data don't block the migration)
SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_profile_addresses ADD COLUMN city_id BIGINT, ADD CONSTRAINT store_fk_address_city FOREIGN KEY (city_id) REFERENCES cities(id)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_profile_addresses' AND COLUMN_NAME = 'city_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- Drop old free-text columns once city_id is in place
SET @s = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE store_profile_addresses DROP COLUMN city',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_profile_addresses' AND COLUMN_NAME = 'city');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE store_profile_addresses DROP COLUMN state',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_profile_addresses' AND COLUMN_NAME = 'state');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE store_profile_addresses DROP COLUMN country',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_profile_addresses' AND COLUMN_NAME = 'country');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_products — add free_shipping flag
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_products ADD COLUMN free_shipping TINYINT(1) NOT NULL DEFAULT 0',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_products' AND COLUMN_NAME = 'free_shipping');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: stores — add free_shipping_min_amount
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE stores ADD COLUMN free_shipping_min_amount DECIMAL(14,2)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'free_shipping_min_amount');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_order_seller_groups — add shipping_cost
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_order_seller_groups ADD COLUMN shipping_cost DECIMAL(14,2) NOT NULL DEFAULT 0',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_order_seller_groups' AND COLUMN_NAME = 'shipping_cost');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_orders — coupon fields
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN coupon_code VARCHAR(50)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'coupon_code');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN coupon_discount DECIMAL(14,2)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'coupon_discount');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_coupons — rename enum values to English
-- =====================

UPDATE store_coupons SET discount_type = 'PERCENTAGE'   WHERE discount_type = 'PORCENTAJE';
UPDATE store_coupons SET discount_type = 'FIXED_AMOUNT'  WHERE discount_type = 'MONTO_FIJO';
UPDATE store_coupons SET status = 'ACTIVE'   WHERE status = 'ACTIVA';
UPDATE store_coupons SET status = 'INACTIVE' WHERE status = 'INACTIVA';
UPDATE store_coupons SET status = 'EXPIRED'  WHERE status = 'VENCIDA';

ALTER TABLE store_coupons MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- =====================
-- Migration: store_orders — buyer contact snapshot
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN buyer_phone VARCHAR(50)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'buyer_phone');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN buyer_email VARCHAR(150)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'buyer_email');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_orders — guest checkout support
-- =====================

-- Make buyer_id nullable to allow guest orders
SET @s = (SELECT IF(IS_NULLABLE = 'NO',
  'ALTER TABLE store_orders MODIFY COLUMN buyer_id BIGINT NULL',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'buyer_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- Guest contact fields (name/lastname stored inline since there is no StoreProfile)
SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN guest_name VARCHAR(100)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'guest_name');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN guest_lastname VARCHAR(100)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'guest_lastname');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- Extended shipping address fields (complement and reference were missing)
SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN shipping_department VARCHAR(100)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'shipping_department');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN shipping_complement VARCHAR(255)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'shipping_complement');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN shipping_reference VARCHAR(255)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'shipping_reference');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN shipping_city_id BIGINT',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'shipping_city_id');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

-- =====================
-- Migration: store_orders — MercadoPago surcharge
-- =====================

SET @s = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE store_orders ADD COLUMN mercado_pago_surcharge DECIMAL(14,2)',
  'SELECT 1')
  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_orders' AND COLUMN_NAME = 'mercado_pago_surcharge');
PREPARE _stmt FROM @s; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;
