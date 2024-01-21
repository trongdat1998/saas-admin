DROP TABLE IF EXISTS `tb_broker`;
CREATE TABLE `tb_broker` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'pk',
   `broker_id` bigint(20) NOT NULL COMMENT '券商id',
   `instance_id` bigint(20) NULL COMMENT '实例id',
   `name` varchar(100) DEFAULT NULL COMMENT '券商简称',
   `full_name` varchar(100) DEFAULT NULL COMMENT '券商全称',
   `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
   `phone` varchar(100) DEFAULT NULL COMMENT '联系电话',
   `host` varchar(100) DEFAULT NULL COMMENT '券商域名',
   `earnest_address` varchar(500) DEFAULT NULL COMMENT '保证金地址',
   `contact` varchar(100) DEFAULT NULL COMMENT '联系人',
   `basic_info` varchar(100) DEFAULT NULL COMMENT '基础信息',
   `saas_fee` bigint(20) DEFAULT NULL COMMENT 'saas费率',
   `is_bind` tinyint(4) DEFAULT NULL COMMENT '是否绑定身份',
   `enabled` tinyint(4) DEFAULT NULL COMMENT '是否启用',
   `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin comment '审核原因多语言信息表';

DROP TABLE IF EXISTS `tb_exchange_info`;
CREATE TABLE `tb_exchange_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '交易平台所使用的id',
  `exchange_name` varchar(255) DEFAULT NULL COMMENT '交易所简称',
  `saas_fee_rate` decimal(65,18) NOT NULL COMMENT 'Saas费率',
  `company` varchar(200) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '公司名称',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱地址',
  `contact_name` varchar(255) DEFAULT NULL COMMENT '联系人姓名',
  `contact_telephone` varchar(50) CHARACTER SET utf8mb4  DEFAULT NULL COMMENT '联系人电话',
  `pay_earnest` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否缴纳保证金',
  `remark` varchar(120) CHARACTER SET utf8mb4  NOT NULL COMMENT '备注信息',
  `status` tinyint(4) NOT NULL COMMENT '状态值 暂时没使用',
  `created_at` bigint(20) NOT NULL COMMENT '用户注册时间',
  `created_ip` varchar(20) DEFAULT NULL COMMENT '用户注册IP',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 1=删除 0=正常',
  `updated_at` bigint(20) DEFAULT NULL COMMENT '修改时间 ',
  `cluster_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=83940118410825729 DEFAULT CHARSET=utf8mb4  COMMENT='交易所信息表';

-- ----------------------------
-- Table structure for tb_exchange_instance
-- ----------------------------
DROP TABLE IF EXISTS `tb_exchange_instance`;
CREATE TABLE `tb_exchange_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_name` varchar(20) NOT NULL COMMENT '交易所名称',
  `exchange_id` bigint(20) NOT NULL COMMENT '交易所id',
  `cluster_name` varchar(20) NOT NULL COMMENT '集群名称',
  `instance_name` varchar(20) NOT NULL COMMENT '实例名称',
  `port` int(11) NOT NULL COMMENT '端口号',
  `created_at` bigint(20) NOT NULL COMMENT '用户注册时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 1=删除 0=正常',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='交易所实例表';

-- ----------------------------
-- Table structure for tb_exchange_op_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_exchange_op_record`;
CREATE TABLE `tb_exchange_op_record` (
  `id` bigint(20) NOT NULL,
  `saas_exchange_id` bigint(20) DEFAULT NULL,
  `exchange_id` bigint(20) DEFAULT NULL,
  `op_type` tinyint(4) DEFAULT NULL,
  `req_content` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `res_content` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `created_at` bigint(20) DEFAULT NULL,
  `op_saas_admin_id` bigint(20) DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


DROP TABLE IF EXISTS `tb_exchange_saas_fee_rate`;
CREATE TABLE `tb_exchange_saas_fee_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '交易所id',
  `fee_rate` decimal(65,18) NOT NULL COMMENT 'Saas费率',
  `action_time` date NOT NULL COMMENT '生效时间',
  `create_at` timestamp(3) NOT NULL COMMENT '创建时间',
  `update_at` timestamp(3) NOT NULL COMMENT '更新时间',
  `deleted`  tinyint(4) NOT NULL default '0' COMMENT '0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='交易所saas费率表';

DROP TABLE IF EXISTS `tb_broker_saas_fee_rate`;
CREATE TABLE `tb_broker_saas_fee_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `broker_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '券商id',
  `fee_rate` decimal(65,18) NOT NULL COMMENT 'Saas费率',
  `action_time` date NOT NULL COMMENT '生效时间',
  `create_at` timestamp(3) NOT NULL COMMENT '创建时间',
  `update_at` timestamp(3) NOT NULL COMMENT '更新时间',
  `deleted`  tinyint(4) NOT NULL default '0' COMMENT '0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='券商saas费率表';

#v0.6-sqlcode

CREATE TABLE `tb_exchange_symbol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` bigint(20) NOT NULL COMMENT '币对所属交易所ID',
  `symbol_id` varchar(255) DEFAULT NULL,
  `symbol_name` varchar(255) NOT NULL,
  `base_token_id` varchar(255) NOT NULL,
  `quote_token_id` varchar(255) NOT NULL,
  `symbol_alias` varchar(255) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL  COMMENT '显示状态：0-不在交易所中显示，1-显示在交易所中',
  `created_at` timestamp(3) NOT NULL,
  `updated_at` timestamp(3) NOT NULL,
  `allow_trade` tinyint(4) DEFAULT NULL,
  `published` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='设置交易所可显示的币对';

CREATE TABLE `tb_exchange_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` bigint(20) DEFAULT NULL,
  `token_id` varchar(255) NOT NULL COMMENT '不用数字',
  `token_full_name` varchar(255) NOT NULL COMMENT 'full name',
  `min_precision` tinyint(4) NOT NULL COMMENT '最小精度',
  `token_detail` varchar(255) DEFAULT NULL COMMENT 'token 详情',
  `description` varchar(1024) DEFAULT NULL COMMENT 'token 描述',
  `status` tinyint(4) DEFAULT NULL COMMENT '显示状态：0-不在交易所中显示，1-显示在交易所中',
  `created_at` timestamp(3) NOT NULL,
  `updated_at` timestamp(3) NOT NULL,
  `icon` varchar(255) DEFAULT NULL COMMENT 'token icon',
  `fee_token_id` varchar(0) DEFAULT NULL COMMENT '手续费TokenId',
  `fee_token_name` varchar(255) DEFAULT NULL COMMENT '手续费TokenName',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_id_idx` (`token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设置交易所可显示的token';