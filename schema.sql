-- Create table for oms_order
CREATE TABLE oms_order (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_sn VARCHAR(255) NOT NULL,
    coupon_id BIGINT,
    create_time TIMESTAMP NOT NULL,
    member_username VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    pay_amount DECIMAL(19, 2) NOT NULL,
    freight_amount DECIMAL(19, 2),
    promotion_amount DECIMAL(19, 2),
    integration_amount DECIMAL(19, 2),
    coupon_amount DECIMAL(19, 2),
    discount_amount DECIMAL(19, 2),
    pay_type INTEGER,
    source_type INTEGER,
    status INTEGER,
    delivery_company VARCHAR(255),
    delivery_sn VARCHAR(255),
    auto_confirm_day INTEGER,
    integration INTEGER,
    growth INTEGER,
    bill_type INTEGER,
    bill_header VARCHAR(255),
    bill_content VARCHAR(255),
    bill_receiver_phone VARCHAR(255),
    bill_receiver_email VARCHAR(255),
    receiver_name VARCHAR(255),
    receiver_phone VARCHAR(255),
    receiver_post_code VARCHAR(255),
    receiver_province VARCHAR(255),
    receiver_city VARCHAR(255),
    receiver_region VARCHAR(255),
    receiver_detail_address VARCHAR(255),
    note TEXT,
    confirm_status INTEGER,
    delete_status INTEGER,
    use_integration INTEGER,
    payment_time TIMESTAMP,
    delivery_time TIMESTAMP,
    receive_time TIMESTAMP,
    comment_time TIMESTAMP,
    modify_time TIMESTAMP
);

-- Create table for oms_order_item
CREATE TABLE oms_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_sn VARCHAR(255) NOT NULL,
    spu_id BIGINT,
    spu_name VARCHAR(255),
    spu_pic VARCHAR(255),
    spu_brand VARCHAR(255),
    category_id BIGINT,
    sku_id BIGINT,
    sku_name VARCHAR(255),
    sku_pic VARCHAR(255),
    sku_price DECIMAL(19, 2),
    sku_quantity INTEGER,
    sku_attrs_vals TEXT,
    promotion_amount DECIMAL(19, 2),
    coupon_amount DECIMAL(19, 2),
    integration_amount DECIMAL(19, 2),
    real_amount DECIMAL(19, 2),
    gift_integration INTEGER,
    gift_growth INTEGER,
    FOREIGN KEY (order_id) REFERENCES oms_order(id)
);

-- Create table for oms_order_operate_history
CREATE TABLE oms_order_operate_history (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    operate_man VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    order_status INTEGER,
    note TEXT,
    FOREIGN KEY (order_id) REFERENCES oms_order(id)
);

-- Create table for oms_order_return_apply
CREATE TABLE oms_order_return_apply (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT,
    order_sn VARCHAR(255),
    create_time TIMESTAMP NOT NULL,
    member_username VARCHAR(255),
    return_amount DECIMAL(19, 2),
    return_name VARCHAR(255),
    return_phone VARCHAR(255),
    status INTEGER,
    handle_time TIMESTAMP,
    sku_img VARCHAR(255),
    sku_name VARCHAR(255),
    sku_brand VARCHAR(255),
    sku_attrs_vals TEXT,
    sku_count INTEGER,
    sku_price DECIMAL(19, 2),
    sku_real_price DECIMAL(19, 2),
    reason TEXT,
    description TEXT,
    desc_pics TEXT,
    handle_note TEXT,
    handle_man VARCHAR(255),
    receive_man VARCHAR(255),
    receive_time TIMESTAMP,
    receive_note TEXT,
    receive_phone VARCHAR(255),
    company_address TEXT,
    FOREIGN KEY (order_id) REFERENCES oms_order(id)
);

-- Create table for oms_order_return_reason
CREATE TABLE oms_order_return_reason (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sort INTEGER,
    status INTEGER,
    create_time TIMESTAMP NOT NULL
);

-- Create table for oms_order_setting
CREATE TABLE oms_order_setting (
    id BIGINT PRIMARY KEY,
    flash_order_overtime INTEGER,
    normal_order_overtime INTEGER,
    confirm_overtime INTEGER,
    finish_overtime INTEGER,
    comment_overtime INTEGER,
    member_level INTEGER
);

-- Create table for oms_payment_info
CREATE TABLE oms_payment_info (
    id BIGINT PRIMARY KEY,
    order_sn VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL,
    alipay_trade_no VARCHAR(255),
    total_amount DECIMAL(19, 2) NOT NULL,
    subject VARCHAR(255),
    payment_status VARCHAR(50),
    create_time TIMESTAMP NOT NULL,
    confirm_time TIMESTAMP,
    callback_content TEXT,
    callback_time TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES oms_order(id)
);

-- Create table for oms_refund_info
CREATE TABLE oms_refund_info (
    id BIGINT PRIMARY KEY,
    order_return_id BIGINT NOT NULL,
    refund DECIMAL(19, 2) NOT NULL,
    refund_sn VARCHAR(255),
    refund_status INTEGER,
    refund_channel INTEGER,
    refund_content TEXT,
    FOREIGN KEY (order_return_id) REFERENCES oms_order_return_apply(id)
);
-- =============================================ware============================================
CREATE TABLE wms_ware_order_task_detail (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT,
    sku_name VARCHAR(255),
    sku_num INTEGER,
    task_id BIGINT,
    ware_id BIGINT,
    lock_status INTEGER,
    FOREIGN KEY (task_id) REFERENCES wms_ware_order_task(id),
    FOREIGN KEY (ware_id) REFERENCES wms_ware_info(id)
);
CREATE TABLE wms_ware_sku (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT,
    ware_id BIGINT,
    stock INTEGER,
    sku_name VARCHAR(255),
    stock_locked INTEGER,
    FOREIGN KEY (ware_id) REFERENCES wms_ware_info(id)
);
CREATE TABLE wms_ware_info (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255),
    area_code VARCHAR(100)
);
CREATE TABLE wms_purchase_detail (
    id BIGINT PRIMARY KEY,
    purchase_id BIGINT,
    sku_id BIGINT,
    sku_num INTEGER,
    sku_price DECIMAL(19, 2),
    ware_id BIGINT,
    status INTEGER,
    FOREIGN KEY (purchase_id) REFERENCES wms_purchase(id),
    FOREIGN KEY (ware_id) REFERENCES wms_ware_info(id)
);
CREATE TABLE wms_purchase (
    id BIGINT PRIMARY KEY,
    assignee_id BIGINT,
    assignee_name VARCHAR(255),
    phone VARCHAR(50),
    priority INTEGER,
    status INTEGER,
    ware_id BIGINT,
    amount DECIMAL(19, 2),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    FOREIGN KEY (ware_id) REFERENCES wms_ware_info(id)
);
CREATE TABLE wms_ware_order_task (
    id BIGINT PRIMARY KEY,
    order_id BIGINT,
    order_sn VARCHAR(255),
    consignee VARCHAR(255),
    consignee_tel VARCHAR(50),
    delivery_address VARCHAR(255),
    order_comment TEXT,
    payment_way INTEGER,
    task_status INTEGER,
    order_body TEXT,
    tracking_no VARCHAR(255),
    create_time TIMESTAMP,
    ware_id BIGINT,
    task_comment TEXT,
    FOREIGN KEY (ware_id) REFERENCES wms_ware_info(id)
);
