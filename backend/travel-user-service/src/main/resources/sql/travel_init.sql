-- MySQL dump 10.13  Distrib 5.7.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: travel
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `banner` (
  `id` varchar(32) NOT NULL,
  `image` varchar(255) DEFAULT NULL COMMENT '轮播图图片地址',
  `title` varchar(100) DEFAULT NULL COMMENT '轮播图标题',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_title` (`title`),
  KEY `idx_is_delete` (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页轮播图';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_history`
--

DROP TABLE IF EXISTS `chat_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 user 表',
  `role` varchar(20) NOT NULL COMMENT '消息角色：user/assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `session_id` varchar(64) DEFAULT 'default' COMMENT '会话ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session` (`session_id`,`create_time`),
  CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI自由聊天历史记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_name` varchar(255) NOT NULL COMMENT '景点名称',
  `difficulty` varchar(20) NOT NULL COMMENT '难度：简单/中等/困难/噩梦',
  `content` text NOT NULL COMMENT '题目内容',
  `options` json NOT NULL COMMENT '选项数组（JSON格式）',
  `correct_answer` varchar(255) NOT NULL COMMENT '正确答案',
  `analysis` text COMMENT '题目解析（可选）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_name` (`scenic_name`),
  KEY `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='旅游景点题库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_fallback`
--

DROP TABLE IF EXISTS `question_fallback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_fallback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_name` varchar(255) NOT NULL COMMENT '景点名称',
  `difficulty` varchar(20) NOT NULL COMMENT '难度',
  `question_count` int NOT NULL COMMENT '题数',
  `option_count` int NOT NULL COMMENT '选项数',
  `questions` json NOT NULL COMMENT '题目数组（JSON格式，同AI返回结构）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_difficulty` (`scenic_name`,`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库兜底表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scenic`
--

DROP TABLE IF EXISTS `scenic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scenic` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT NULL COMMENT '景点标题',
  `img` varchar(255) DEFAULT NULL COMMENT '图片路径',
  `tag` json DEFAULT NULL COMMENT '标签数组',
  `is_dot` varchar(20) DEFAULT NULL COMMENT '推荐标记文字（如：推荐/热门）',
  `dot` tinyint(1) DEFAULT '0' COMMENT '是否显示红点',
  `introduce` text COMMENT '景点介绍',
  `times` varchar(100) DEFAULT NULL COMMENT '开放时间',
  `is_play` tinyint(1) DEFAULT '0' COMMENT '是否有游玩项目',
  `address` json DEFAULT NULL COMMENT '经纬度数组',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_title` (`title`),
  KEY `idx_is_dot` (`is_dot`),
  KEY `idx_is_delete` (`is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='景点信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scenic_play`
--

DROP TABLE IF EXISTS `scenic_play`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scenic_play` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '游玩项目ID',
  `scenic_id` int NOT NULL COMMENT '关联景区ID',
  `title` varchar(100) NOT NULL COMMENT '项目名称',
  `url` varchar(255) DEFAULT NULL COMMENT '项目图片',
  `tag` varchar(50) DEFAULT NULL COMMENT '项目标签',
  `description` varchar(255) DEFAULT NULL COMMENT '项目描述/地址',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态 1启用 0禁用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='景区游玩推荐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_travel_plan`
--

DROP TABLE IF EXISTS `t_travel_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_travel_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联 user 表的 id）',
  `departure` varchar(50) DEFAULT NULL COMMENT '出发地',
  `destination` varchar(50) NOT NULL COMMENT '目的地',
  `travel_date` date DEFAULT NULL COMMENT '出行日期',
  `days` int NOT NULL COMMENT '旅行天数',
  `people_count` int DEFAULT '1' COMMENT '同行人数',
  `people_type` varchar(20) DEFAULT NULL COMMENT '同行类型：情侣/家庭/朋友/独自',
  `budget_min` int DEFAULT NULL COMMENT '最低预算（元）',
  `budget_max` int DEFAULT NULL COMMENT '最高预算（元）',
  `preferences` json DEFAULT NULL COMMENT '兴趣偏好标签（JSON数组）',
  `plan_content` json NOT NULL COMMENT 'AI生成的完整计划内容（结构化JSON）',
  `total_budget` decimal(10,2) DEFAULT NULL COMMENT 'AI估算的总花费',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，加速按用户查询'
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='旅行计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_group_booking`
--

DROP TABLE IF EXISTS `tb_group_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_group_booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint NOT NULL COMMENT '旅游团ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `group_id` (`group_id`),
  CONSTRAINT `tb_group_booking_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `tb_travel_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老年团报名表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_group_booking_traveler`
--

DROP TABLE IF EXISTS `tb_group_booking_traveler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_group_booking_traveler` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `booking_id` bigint NOT NULL COMMENT '报名表ID',
  `traveler_id` bigint NOT NULL COMMENT '出行人ID',
  PRIMARY KEY (`id`),
  KEY `booking_id` (`booking_id`),
  KEY `traveler_id` (`traveler_id`),
  CONSTRAINT `tb_group_booking_traveler_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `tb_group_booking` (`id`),
  CONSTRAINT `tb_group_booking_traveler_ibfk_2` FOREIGN KEY (`traveler_id`) REFERENCES `tb_traveler` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老年团报名出行人关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_guide_booking`
--

DROP TABLE IF EXISTS `tb_guide_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_guide_booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `guide_id` bigint NOT NULL COMMENT '导游ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `guide_id` (`guide_id`),
  CONSTRAINT `tb_guide_booking_ibfk_1` FOREIGN KEY (`guide_id`) REFERENCES `tb_private_guide` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人导游预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_guide_booking_traveler`
--

DROP TABLE IF EXISTS `tb_guide_booking_traveler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_guide_booking_traveler` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `booking_id` bigint NOT NULL COMMENT '预约表ID',
  `traveler_id` bigint NOT NULL COMMENT '出行人ID',
  PRIMARY KEY (`id`),
  KEY `booking_id` (`booking_id`),
  KEY `traveler_id` (`traveler_id`),
  CONSTRAINT `tb_guide_booking_traveler_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `tb_guide_booking` (`id`),
  CONSTRAINT `tb_guide_booking_traveler_ibfk_2` FOREIGN KEY (`traveler_id`) REFERENCES `tb_traveler` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人导游预约出行人关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_private_guide`
--

DROP TABLE IF EXISTS `tb_private_guide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_private_guide` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导游姓名',
  `avatar` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `region` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '熟悉地区',
  `languages` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务语言（逗号分隔）',
  `rating` decimal(2,1) DEFAULT NULL COMMENT '评分',
  `intro` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '介绍',
  `experience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '从业经验',
  `price` decimal(10,2) NOT NULL COMMENT '日薪',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `schedule` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '可预约日期（逗号分隔）',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人导游信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_travel_group`
--

DROP TABLE IF EXISTS `tb_travel_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_travel_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '旅游团名称',
  `start_time` date NOT NULL COMMENT '开始时间',
  `end_time` date NOT NULL COMMENT '结束时间',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `group_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旅行社名称',
  `meeting_point` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '集合地点',
  `guide_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导游姓名',
  `guide_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导游电话',
  `guide_experience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导游经验',
  `group_info` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社团信息',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老年旅游团信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_traveler`
--

DROP TABLE IF EXISTS `tb_traveler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tb_traveler` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出行人姓名',
  `age` int NOT NULL COMMENT '年龄',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `id_card` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID（小程序用户）',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删 1-已删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出行人信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creator_id` bigint NOT NULL COMMENT '创建人用户ID',
  `destination` varchar(100) NOT NULL COMMENT '目的地',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `total_people` int NOT NULL COMMENT '计划总人数',
  `current_people` int NOT NULL DEFAULT '1' COMMENT '当前人数',
  `description` varchar(500) DEFAULT NULL COMMENT '组队描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-招募中，2-已满员，3-已结束，4-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_destination` (`destination`),
  KEY `idx_start_date` (`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team_member`
--

DROP TABLE IF EXISTS `team_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_id` bigint NOT NULL COMMENT '组队ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` tinyint NOT NULL DEFAULT '2' COMMENT '角色：1-队长，2-队员',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组队成员关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) NOT NULL COMMENT '微信openid',
  `nick_name` varchar(50) DEFAULT '' COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT '' COMMENT '用户头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`) COMMENT 'openid唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_answer_detail`
--

DROP TABLE IF EXISTS `user_answer_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_answer_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '答题记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` varchar(255) NOT NULL COMMENT '用户答案',
  `is_correct` tinyint NOT NULL COMMENT '是否正确：0=错误，1=正确',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户答题详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_answer_record`
--

DROP TABLE IF EXISTS `user_answer_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_answer_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `scenic_name` varchar(255) NOT NULL COMMENT '测评景点',
  `total_questions` int NOT NULL COMMENT '总题数',
  `score` int NOT NULL COMMENT '得分（满分100）',
  `evaluate_name` varchar(20) NOT NULL COMMENT '评价名称（≤8字）',
  `evaluate_desc` text NOT NULL COMMENT '评价描述（50-200字）',
  `answer_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scenic_name` (`scenic_name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户答题记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-11  9:48:53


-- ==================================================================================
-- 【微服务改造新增】RAG 知识库文档表
-- 说明：
--   1. 使用 CREATE TABLE IF NOT EXISTS，可安全重复执行，不会影响上方的原有业务表；
--   2. is_delete 逻辑删除字段与 MyBatis-Plus 全局 logic-delete-field 配置一致；
--   3. user_id = 0 表示系统预置文档（景点一键同步产生），任何用户都不可删除；
--   4. file_path 存的是本地绝对路径，位于 D:\dev-resources\rag-docs\（不走 MinIO）。
-- ==================================================================================
CREATE TABLE IF NOT EXISTS `rag_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `user_id` BIGINT NOT NULL COMMENT '上传用户ID（系统预置为0）',
  `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `file_type` VARCHAR(20) NOT NULL COMMENT 'PDF/DOCX/TXT/XLSX/SCENIC',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '本地存储路径(D盘)',
  `chunk_count` INT DEFAULT 0 COMMENT '切分片段数量',
  `source_type` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/SYSTEM/SCENIC',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/COMPLETED/FAILED',
  `error_msg` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG知识库文档表';
