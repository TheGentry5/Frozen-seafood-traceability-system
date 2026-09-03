-- ============================================================
-- 冷冻海产品溯源系统 初始数据脚本
-- 依据：docs/开发实施文档.md §3.3
-- 密码统一为 123456（MD5 小写：e10adc3949ba59abbe56e057f20f883e）
-- ============================================================
USE seafood_trace;

-- ---------- 省 / 市（沿海省份样例） ----------
INSERT INTO province (province_code, province_name) VALUES
 ('LN','辽宁省'), ('SD','山东省'), ('JS','江苏省'), ('ZJ','浙江省'), ('FJ','福建省'), ('GD','广东省');

INSERT INTO city (city_code, city_name, province_code) VALUES
 ('LN01','大连市','LN'), ('LN02','丹东市','LN'),
 ('SD01','青岛市','SD'), ('SD02','烟台市','SD'), ('SD03','威海市','SD'),
 ('JS01','连云港市','JS'), ('JS02','南通市','JS'),
 ('ZJ01','舟山市','ZJ'), ('ZJ02','宁波市','ZJ'), ('ZJ03','台州市','ZJ'),
 ('FJ01','福州市','FJ'), ('FJ02','厦门市','FJ'), ('FJ03','莆田市','FJ'),
 ('GD01','湛江市','GD'), ('GD02','汕头市','GD'), ('GD03','广州市','GD');

-- ---------- 系统管理员 ----------
INSERT INTO admin (username, password) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e');

-- ---------- 节点企业（四类各至少一家，用于联调） ----------
INSERT INTO node_info (node_code, password, node_name, node_type, province_code, city_code, contact, phone, status) VALUES
 ('FARM001','e10adc3949ba59abbe56e057f20f883e','东山深远海养殖有限公司',  1,'FJ','FJ01','林海','13800000001',1),
 ('FARM002','e10adc3949ba59abbe56e057f20f883e','烟台海湾水产养殖合作社',  1,'SD','SD02','王强','13800000002',1),
 ('PROC001','e10adc3949ba59abbe56e057f20f883e','舟山蓝海冷冻食品有限公司',2,'ZJ','ZJ01','陈舟','13800000003',1),
 ('PROC002','e10adc3949ba59abbe56e057f20f883e','大连渔家冷冻加工厂',      2,'LN','LN01','张渔','13800000004',1),
 ('WHOL001','e10adc3949ba59abbe56e057f20f883e','青岛海丰批发商行',        3,'SD','SD01','李批','13800000005',1),
 ('RETA001','e10adc3949ba59abbe56e057f20f883e','大连海立方生鲜超市',      4,'LN','LN01','赵超','13800000006',1);

-- ---------- 养殖企业一批"已发布"样例批号（供加工企业新建时级联选用） ----------
INSERT INTO farm_batch (node_id, batch_code, product_name, inspection_cert, inspector, status) VALUES
 (1,'FARM-B-20260901','南美白对虾','闽检字2026-0901','林海',2),
 (2,'FARM-B-20260902','三文鱼','鲁检字2026-0902','王强',2);
