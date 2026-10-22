-- 智能客服模块数据库
-- 数据库名与课程参考项目保持一致：itheima_ai
-- 执行方式：
--   mysql -uroot -p --socket=/tmp/mysql.sock < db/itheima_ai.sql

CREATE DATABASE IF NOT EXISTS itheima_ai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE itheima_ai;

-- =========================================================
-- 校区表
-- =========================================================
CREATE TABLE IF NOT EXISTS school (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    address VARCHAR(255) NOT NULL DEFAULT '' COMMENT '校区地址',
    phone VARCHAR(30) NOT NULL DEFAULT '' COMMENT '咨询电话',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_school_name (name),
    KEY idx_school_city (city)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '校区';

-- =========================================================
-- 课程表
-- =========================================================
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    type VARCHAR(50) NOT NULL COMMENT '课程方向：Java/Python/前端/测试/设计/AI 等',
    education VARCHAR(50) NOT NULL DEFAULT '不限' COMMENT '学历要求',
    duration VARCHAR(50) NOT NULL DEFAULT '' COMMENT '学习周期',
    price DECIMAL(10, 2) DEFAULT NULL COMMENT '课程价格',
    description VARCHAR(500) NOT NULL DEFAULT '' COMMENT '课程简介',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_name (name),
    KEY idx_course_type (type),
    KEY idx_course_education (education)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '课程';

-- =========================================================
-- 课程预约表
-- =========================================================
CREATE TABLE IF NOT EXISTS course_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL COMMENT '预约人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    school_id BIGINT NOT NULL COMMENT '校区 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/CANCELLED',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_reservation_course (course_id),
    KEY idx_reservation_school (school_id),
    KEY idx_reservation_phone (phone),
    CONSTRAINT fk_reservation_course
        FOREIGN KEY (course_id) REFERENCES course (id),
    CONSTRAINT fk_reservation_school
        FOREIGN KEY (school_id) REFERENCES school (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '课程预约';

-- =========================================================
-- 示例校区
-- =========================================================
INSERT IGNORE INTO school (name, city, address, phone) VALUES
('北京校区', '北京', '北京市海淀区（具体地址以校区通知为准）', '010-00000000'),
('上海校区', '上海', '上海市浦东新区（具体地址以校区通知为准）', '021-00000000'),
('广州校区', '广州', '广州市天河区（具体地址以校区通知为准）', '020-00000000'),
('深圳校区', '深圳', '深圳市南山区（具体地址以校区通知为准）', '0755-00000000'),
('杭州校区', '杭州', '杭州市西湖区（具体地址以校区通知为准）', '0571-00000000'),
('成都校区', '成都', '成都市高新区（具体地址以校区通知为准）', '028-00000000');

-- =========================================================
-- 示例课程
-- =========================================================
INSERT IGNORE INTO course (name, type, education, duration, price, description) VALUES
('JavaEE 就业班', 'Java', '大专及以上', '6 个月', 20980.00, 'Java 基础、Web、框架、微服务与项目实战。'),
('Python 大数据', 'Python', '本科及以上', '5 个月', 21980.00, 'Python、数据分析、Hadoop、Spark 与大数据项目。'),
('Web 前端开发', '前端', '大专及以上', '5 个月', 18980.00, 'HTML/CSS/JavaScript、Vue、React 与工程化。'),
('软件测试', '测试', '大专及以上', '4 个月', 16980.00, '功能测试、接口测试、自动化测试与性能测试。'),
('UI/UX 设计', '设计', '不限', '4 个月', 15980.00, '视觉设计、交互设计、Figma 与作品集。'),
('AI 大模型应用开发', 'AI', '本科及以上', '6 个月', 22980.00, '大模型基础、Prompt、RAG、Agent 与 Spring AI 实战。');

-- =========================================================
-- 验证
-- =========================================================
SELECT 'school' AS table_name, COUNT(*) AS total FROM school
UNION ALL
SELECT 'course', COUNT(*) FROM course
UNION ALL
SELECT 'course_reservation', COUNT(*) FROM course_reservation;
