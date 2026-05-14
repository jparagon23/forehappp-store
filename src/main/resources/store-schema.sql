CREATE TABLE IF NOT EXISTS store_confirmation_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255),
    created_at  DATETIME,
    expires_at  DATETIME,
    confirmed_at DATETIME,
    user_id     BIGINT NOT NULL,
    CONSTRAINT fk_sct_user FOREIGN KEY (user_id) REFERENCES users(user_id)
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
    CONSTRAINT fk_sp_user FOREIGN KEY (user_id) REFERENCES users(user_id)
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
    CONSTRAINT fk_spa_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_roles (
    role_name VARCHAR(20) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS store_profile_roles (
    store_profile_id BIGINT      NOT NULL,
    role             VARCHAR(20) NOT NULL,
    CONSTRAINT fk_spr_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id),
    CONSTRAINT fk_spr_role    FOREIGN KEY (role)             REFERENCES store_roles(role_name)
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
    seller_id    BIGINT NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    brand_id     BIGINT NOT NULL,
    line_id      BIGINT,
    category_id  BIGINT NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME NOT NULL,
    CONSTRAINT store_fk_prod_seller   FOREIGN KEY (seller_id)   REFERENCES store_profiles(store_profile_id),
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
    sku              VARCHAR(100) NOT NULL,
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

CREATE TABLE IF NOT EXISTS store_order_seller_groups (
    group_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    seller_id       BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal        DECIMAL(14,2) NOT NULL,
    tracking_number VARCHAR(100),
    prepared_at     DATETIME,
    shipped_at      DATETIME,
    delivered_at    DATETIME,
    CONSTRAINT store_fk_osg_order  FOREIGN KEY (order_id)  REFERENCES store_orders(order_id),
    CONSTRAINT store_fk_osg_seller FOREIGN KEY (seller_id) REFERENCES store_profiles(store_profile_id)
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
    CONSTRAINT fk_cart_profile FOREIGN KEY (store_profile_id) REFERENCES store_profiles(store_profile_id)
);

CREATE TABLE IF NOT EXISTS store_cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id      BIGINT NOT NULL,
    variant_id   BIGINT NOT NULL,
    quantity     INT NOT NULL,
    price_at_add DECIMAL(14, 2) NOT NULL,
    added_date   DATETIME NOT NULL,
    CONSTRAINT uk_cart_variant UNIQUE (cart_id, variant_id),
    CONSTRAINT fk_ci_cart      FOREIGN KEY (cart_id)    REFERENCES store_carts(cart_id),
    CONSTRAINT fk_ci_variant   FOREIGN KEY (variant_id) REFERENCES store_product_variants(variant_id)
);
