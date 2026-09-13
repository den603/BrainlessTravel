-- ==================================================================================
-- 迁移脚本：图片地址由「绝对 URL」改为「相对 object key」
--
-- 【解决什么问题】
-- 早期版本 MinioUtil.upload() 返回的是绝对 URL（如 http://192.168.0.6:9000/travel/scenic/tt.jpg），
-- 前端原样存入数据库。一旦开发电脑换 WiFi／DHCP 重新分配 IP，数据库里所有历史图片
-- 地址立即全部失效，必须手工批量改库才能恢复，非常脆弱。
--
-- 【改造后】
--   数据库只保存相对路径，例如：scenic/tt.jpg
--   完整地址由前端 uniapp-front/api/config.js 的 MINIO_BASE 在渲染时拼接：
--     resolveImage('scenic/tt.jpg') → http://<主机>:9000/travel/scenic/tt.jpg
--   这样换网络只需改前端一个常量，历史数据永不受影响 —— 彻底根治。
--
-- 【执行前提】
--   1. 前端已升级到使用 resolveImage()（本次改造已包含，无需额外操作）
--   2. 后端 MinioUtil 已改为返回相对路径（本次改造已包含）
--   3. 建议先备份：mysqldump -uroot -p travel scenic scenic_play > backup.sql
--
-- 【执行方式】
--   mysql -uroot -p travel < migration_v2_relative_image_path.sql
--
-- 【幂等性】
--   带 WHERE ... LIKE 'http%://%/travel/%' 条件，已是相对路径的行不会被重复处理，
--   可安全重复执行。第三方外链（如 banner/游玩项目里的 https://xxx.com/a.jpg）不匹配条件，
--   会被完整保留，不受影响。
-- ==================================================================================

-- ---------- 执行前自查：看看有哪些行会被改动 ----------
SELECT 'scenic.img' AS 字段, id, img AS 当前值
FROM scenic
WHERE img LIKE 'http://%/travel/%' OR img LIKE 'https://%/travel/%'
UNION ALL
SELECT 'scenic_play.url' AS 字段, id, url AS 当前值
FROM scenic_play
WHERE url LIKE 'http://%/travel/%' OR url LIKE 'https://%/travel/%';

-- ---------- 正式迁移：取 /travel/ 之后的部分作为 object key ----------
UPDATE scenic
SET img = SUBSTRING_INDEX(img, '/travel/', -1)
WHERE img LIKE 'http://%/travel/%' OR img LIKE 'https://%/travel/%';

UPDATE scenic_play
SET url = SUBSTRING_INDEX(url, '/travel/', -1)
WHERE url LIKE 'http://%/travel/%' OR url LIKE 'https://%/travel/%';

-- ---------- 迁移后校验 ----------
-- 结果应全为相对路径（scenic/xxx.jpg、scenic_detail/xxx.jpg），且第三方外链保持不变
SELECT id, title, img FROM scenic;
SELECT id, url FROM scenic_play;

-- 应返回 0：确认已无指向 MinIO 的绝对地址残留
SELECT COUNT(*) AS 残留绝对地址数
FROM scenic
WHERE img LIKE 'http://%/travel/%' OR img LIKE 'https://%/travel/%';

-- ==================================================================================
-- 【回滚方法】（万一需要退回绝对 URL，把下面 host 换成当时的地址）
-- UPDATE scenic      SET img = CONCAT('http://10.90.64.155:9000/travel/', img)
--   WHERE img NOT LIKE 'http://%' AND img NOT LIKE 'https://%';
-- UPDATE scenic_play SET url = CONCAT('http://10.90.64.155:9000/travel/', url)
--   WHERE url NOT LIKE 'http://%' AND url NOT LIKE 'https://%';
-- ==================================================================================
