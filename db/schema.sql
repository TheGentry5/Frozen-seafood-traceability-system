-- ============================================================
-- 冷冻海产品溯源系统 建库建表脚本
-- 依据：docs/开发实施文档.md §3.2
-- 说明：表间不建物理外键，靠逻辑关联 + 索引；表内唯一键兜底业务唯一性
-- ============================================================
CREATE DATABASE IF NOT EXISTS seafood_trace DEFAULT CHARACTER SET utf8mb4;
USE seafood_trace;

-- ============ 系统管理员 ============
CREATE TABLE admin (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(32) NOT NULL UNIQUE,
  password    VARCHAR(64) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 行政区域 ============
CREATE TABLE province (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  province_code VARCHAR(16) NOT NULL UNIQUE,
  province_name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE city (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_code     VARCHAR(16) NOT NULL UNIQUE,
  city_name     VARCHAR(64) NOT NULL,
  province_code VARCHAR(16) NOT NULL,
  KEY idx_city_province (province_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 节点企业信息表 ============
-- node_type: 1 养殖 2 加工 3 批发 4 零售 ;  status: 1 启用 2 停用
CREATE TABLE node_info (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_code     VARCHAR(32)  NOT NULL,
  password      VARCHAR(64)  NOT NULL,
  node_name     VARCHAR(64)  NOT NULL,
  node_type     TINYINT      NOT NULL,
  province_code VARCHAR(16),
  city_code     VARCHAR(16),
  contact       VARCHAR(32),
  phone         VARCHAR(32),
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_node_code (node_code),
  KEY idx_node_type (node_type),
  KEY idx_node_area (province_code, city_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 养殖企业产品批号 ============
-- status: 1 待发布 2 已发布 3 已下架
CREATE TABLE farm_batch (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_id         BIGINT NOT NULL,
  batch_code      VARCHAR(32)  NOT NULL,
  product_name    VARCHAR(64),
  inspection_cert VARCHAR(255),
  inspector       VARCHAR(32),
  status          TINYINT NOT NULL DEFAULT 1,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_farm_batch_code (batch_code),
  KEY idx_farm_node (node_id),
  KEY idx_farm_node_status (node_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 加工企业产品批号（冷冻海产品） ============
-- status: 1 新建 2 待确认 3 已确认 4 已下架
-- in_* 为"进场/上游"信息：采购自养殖企业的批号
CREATE TABLE proc_batch (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_id         BIGINT NOT NULL,
  batch_code      VARCHAR(32)  NOT NULL,
  product_name    VARCHAR(64),
  product_type    VARCHAR(64),
  inspection_cert VARCHAR(255),
  inspector       VARCHAR(32),
  in_node_id      BIGINT,
  in_area         VARCHAR(128),
  in_batch_id     BIGINT,
  in_product_name VARCHAR(64),
  status          TINYINT NOT NULL DEFAULT 1,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_proc_batch_code (batch_code),
  KEY idx_proc_node (node_id),
  KEY idx_proc_node_status (node_id, status),
  KEY idx_proc_in_node (in_node_id),
  KEY idx_proc_in_batch (in_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 批发商产品批号（上游=加工企业） ============
CREATE TABLE whol_batch (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_id         BIGINT NOT NULL,
  batch_code      VARCHAR(32)  NOT NULL,
  product_name    VARCHAR(64),
  product_type    VARCHAR(64),
  inspection_cert VARCHAR(255),
  inspector       VARCHAR(32),
  in_node_id      BIGINT,
  in_area         VARCHAR(128),
  in_batch_id     BIGINT,
  in_product_name VARCHAR(64),
  status          TINYINT NOT NULL DEFAULT 1,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_whol_batch_code (batch_code),
  KEY idx_whol_node (node_id),
  KEY idx_whol_node_status (node_id, status),
  KEY idx_whol_in_node (in_node_id),
  KEY idx_whol_in_batch (in_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 零售商产品批号（上游=批发商，增加溯源码） ============
CREATE TABLE reta_batch (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_id         BIGINT NOT NULL,
  batch_code      VARCHAR(32)  NOT NULL,
  product_name    VARCHAR(64),
  product_type    VARCHAR(64),
  inspection_cert VARCHAR(255),
  inspector       VARCHAR(32),
  in_node_id      BIGINT,
  in_area         VARCHAR(128),
  in_batch_id     BIGINT,
  in_product_name VARCHAR(64),
  trace_code      VARCHAR(64),
  status          TINYINT NOT NULL DEFAULT 1,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_reta_batch_code (batch_code),
  UNIQUE KEY uk_reta_trace_code (trace_code),
  KEY idx_reta_node (node_id),
  KEY idx_reta_node_status (node_id, status),
  KEY idx_reta_in_node (in_node_id),
  KEY idx_reta_in_batch (in_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 冷链温度填报记录（四环节共用） ============
-- batch_type: 1养殖 2加工 3批发 4零售
CREATE TABLE cold_chain_record (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_type  TINYINT       NOT NULL,
  batch_id    BIGINT        NOT NULL,
  node_id     BIGINT        NOT NULL,
  temperature DECIMAL(5,2)  NOT NULL,
  humidity    DECIMAL(5,2),
  record_time DATETIME      NOT NULL,
  remark      VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_cc_batch (batch_type, batch_id, record_time),
  KEY idx_cc_node (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
