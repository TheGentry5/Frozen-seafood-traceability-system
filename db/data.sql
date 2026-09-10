-- ============================================================
-- 冷冻海产品溯源系统 初始数据脚本
-- 依据：docs/开发实施文档.md §3.3
-- 密码统一为 123456（MD5 小写：e10adc3949ba59abbe56e057f20f883e）
-- 说明：行政区域（省/市）数据见 db/area.sql（GB/T 2260 真实编码），本脚本不再插入
-- ============================================================
USE seafood_trace;

-- 可重复执行：先清空旧种子数据并重置自增（顺序：先子后父）
TRUNCATE TABLE cold_chain_record;
TRUNCATE TABLE reta_batch;
TRUNCATE TABLE whol_batch;
TRUNCATE TABLE proc_batch;
TRUNCATE TABLE farm_batch;
TRUNCATE TABLE node_info;
TRUNCATE TABLE admin;

-- ---------- 系统管理员 ----------
INSERT INTO admin (username, password) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e');

-- ---------- 节点企业（四类各至少一家，用于联调） ----------
INSERT INTO node_info (node_code, password, node_name, node_type, province_code, city_code, contact, phone, status) VALUES
 ('FARM001','e10adc3949ba59abbe56e057f20f883e','东山深远海养殖有限公司',  1,'350000','350100','林海','13800000001',1),
 ('FARM002','e10adc3949ba59abbe56e057f20f883e','烟台海湾水产养殖合作社',  1,'370000','370600','王强','13800000002',1),
 ('PROC001','e10adc3949ba59abbe56e057f20f883e','舟山蓝海冷冻食品有限公司',2,'330000','330900','陈舟','13800000003',1),
 ('PROC002','e10adc3949ba59abbe56e057f20f883e','大连渔家冷冻加工厂',      2,'210000','210200','张渔','13800000004',1),
 ('WHOL001','e10adc3949ba59abbe56e057f20f883e','青岛海丰批发商行',        3,'370000','370200','李批','13800000005',1),
 ('RETA001','e10adc3949ba59abbe56e057f20f883e','大连海立方生鲜超市',      4,'210000','210200','赵超','13800000006',1);

-- ---------- 养殖企业一批"已发布"样例批号（供加工企业新建时级联选用） ----------
INSERT INTO farm_batch (node_id, batch_code, product_name, inspection_cert, inspector, status) VALUES
 (1,'FARM-B-20260901','南美白对虾','闽检字2026-0901','林海',2),
 (2,'FARM-B-20260902','三文鱼','鲁检字2026-0902','王强',2);

-- ---------- 冷链温度填报样例（养殖批号 1，供曲线展示） ----------
INSERT INTO cold_chain_record (batch_type, batch_id, node_id, temperature, humidity, record_time, remark) VALUES
 (1,1,1,-18.50,85.00,'2026-09-01 08:00:00','入库'),
 (1,1,1,-18.80,86.00,'2026-09-01 12:00:00',NULL),
 (1,1,1,-17.20,84.00,'2026-09-01 16:00:00','抽检'),
 (1,1,1,-19.10,87.00,'2026-09-02 08:00:00',NULL),
 (1,1,1,-18.30,85.00,'2026-09-02 12:00:00',NULL);
