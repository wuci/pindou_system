-- 店铺规则表
CREATE TABLE `design_store_rules` (
  `id` VARCHAR(36) NOT NULL COMMENT '规则ID（UUID）',
  `category` VARCHAR(50) NOT NULL COMMENT '规则分类：packages-套餐规则, services-增值服务, safety-安全须知, other-其他规定',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '规则标题',
  `content` TEXT DEFAULT NULL COMMENT '规则内容（HTML格式，支持富文本）',
  `rule_type` VARCHAR(50) DEFAULT NULL COMMENT '规则类型：table-表格数据, list-列表, warning-警告框, special-特色服务',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT DEFAULT NULL COMMENT '更新时间（毫秒时间戳）',
  `created_by` VARCHAR(36) DEFAULT NULL COMMENT '创建人ID',
  `updated_by` VARCHAR(36) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_enabled` (`is_enabled`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺规则表';

-- 插入初始数据
INSERT INTO `design_store_rules` (`id`, `category`, `title`, `content`, `rule_type`, `sort_order`, `is_enabled`, `created_at`, `created_by`) VALUES
-- 套餐规则 - 基础套餐表格
('1', 'packages', '基础套餐', '[
  {"name":"轻体验包","price":"¥39","content":"小号模板1个 + 拼豆200颗 + 基础工具1套 + 普通熨烫1次","suitable":"初次体验者","duration":"30-45分钟"},
  {"name":"标准畅玩包","price":"¥68","content":"中号模板1个 + 拼豆500颗 + 基础工具1套 + 普通熨烫1次 + 钥匙扣配件1个","suitable":"单人/情侣","duration":"45-90分钟"},
  {"name":"家庭时光包","price":"¥128","content":"大号模板2个 + 拼豆800颗 + 基础工具2套 + 普通熨烫2次 + 配件任选2个","suitable":"亲子/2-3人","duration":"60-120分钟"},
  {"name":"创作大师包","price":"¥188","content":"大号模板任选2个 + 拼豆1500颗 + 工具全套 + 普通熨烫2次 + 配件任选3个 + 相框1个","suitable":"深度爱好者","duration":"90-180分钟"}
]', 'table', 1, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 套餐规则 - 计费说明
('2', 'packages', '计费说明', '<ul>
<li><strong>计时加时：</strong>超出套餐规定时间，按 ¥15/30分钟 收取（不足30分钟按30分钟计）</li>
<li><strong>材料补购：</strong>拼豆补充包 ¥10/200颗（限同色系），额外模板租赁 ¥5/个</li>
<li><strong>特殊配件：</strong>胸针、磁铁、挂绳等 ¥3-8/个</li>
<li><strong>二次熨烫：</strong>首次熨烫由店员免费协助；如需重烫或修改，收取 ¥5/次</li>
</ul>', 'list', 2, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 套餐规则 - 优惠与会员
('3', 'packages', '优惠与会员', '<ul>
<li>首次到店：关注小红书/大众点评，享首单9折</li>
<li>会员储值：充¥300送¥30，充¥500送¥80</li>
<li>积分规则：消费1元积1分，100分可抵扣¥5</li>
<li>团购优惠：3人及以上同行，每人立减¥10（不可叠加）</li>
</ul>', 'list', 3, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 增值服务 - 免费服务
('4', 'services', '免费服务', '<ul>
<li><strong>免费熨烫协助</strong> - 每位顾客享有1次由店员操作的专业熨烫</li>
<li><strong>图案参考图册</strong> - 店内提供200+图案模板供免费参考</li>
<li><strong>作品拍照服务</strong> - 店员可协助拍摄成品照，提供简易背景板</li>
<li><strong>问题咨询</strong> - 拼豆技巧、配色建议等免费指导</li>
<li><strong>Wi-Fi & 充电</strong> - 免费使用，扫码即可连接</li>
<li><strong>茶水自助</strong> - 柠檬水/薄荷水免费自取</li>
</ul>', 'list', 1, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 增值服务 - 付费服务表格
('5', 'services', '付费升级服务', '[
  {"name":"设计师定制","price":"¥20-50","description":"根据顾客需求，设计师提供独家图案设计"},
  {"name":"作品装裱","price":"¥25起","description":"含相框/立体展示盒，可制作成摆件或挂饰"},
  {"name":"成品邮寄","price":"¥8（同城）","description":"作品完成后可代寄，需填写邮寄委托单"},
  {"name":"拼豆材料包","price":"¥29起","description":"含图纸+拼豆+模板，可带回家继续制作"},
  {"name":"企业团建定制","price":"面议","description":"含场地布置、专属物料、伴手礼等（需提前3天预约）"},
  {"name":"生日派对包场","price":"¥588起","description":"含2小时包场 + 8人标准包 + 主题装饰 + 生日小礼品"}
]', 'table', 2, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 增值服务 - 温柔帮烫
('6', 'services', '温柔帮烫', '<strong>✨ "温柔帮烫"服务：</strong>担心自己烫不好？店员可全程代烫，你只需完成拼豆部分。代烫作品不保证100%完美，但我们会尽最大努力。如需完美品，建议自行练习或选择"设计师定制"服务。', 'special', 3, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 增值服务 - 时光胶囊
('7', 'services', '时光胶囊', '<strong>🎁 "时光胶囊"寄存：</strong>作品完成后可寄存店内，一年后凭存根领取，我们将为你附上当日日期戳和手写祝福卡（寄存费 ¥10/件/年）。', 'special', 4, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 安全须知 - 警告框
('8', 'safety', '安全警告', '<p>本店使用电熨斗进行拼豆定型，熨斗底板工作温度可达150℃-220℃，存在烫伤、触电、火灾等风险。</p>', 'warning', 1, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 安全须知 - 高温烫伤
('9', 'safety', '高温烫伤风险', '<ul>
<li><strong>风险告知：</strong>电熨斗工作时温度极高；刚熨烫完成的拼豆表面温度可达80℃-100℃</li>
<li><strong>安全要求：</strong>熨烫操作必须由成年顾客或监护人完成；儿童需在监护人全程看护下</li>
<li><strong>免责条款：</strong>如因顾客未遵守安全要求导致意外事故，由顾客自行承担全部责任</li>
</ul>', 'list', 2, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 安全须知 - 小零件风险
('10', 'safety', '小零件窒息风险', '<ul>
<li><strong>风险告知：</strong>拼豆颗粒直径约5mm，属于小零件，儿童误吞可能导致窒息</li>
<li><strong>安全要求：</strong>3岁以下儿童禁止入内；3-6岁儿童必须在监护人全程陪同下进行</li>
<li><strong>免责条款：</strong>如因监护人看护不当导致意外，本店不承担任何法律责任</li>
</ul>', 'list', 3, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 安全须知 - 呼吸道健康
('11', 'safety', '材质与呼吸道健康', '<ul>
<li><strong>风险告知：</strong>熨烫过程中可能释放微量挥发性气体，部分人群可能敏感</li>
<li><strong>建议：</strong>孕妇、哺乳期女性建议避免参与熨烫环节；哮喘患者请谨慎参与</li>
</ul>', 'list', 4, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 其他规定 - 预约与占座
('12', 'other', '预约与占座', '<ul>
<li>周末及节假日建议提前1天预约，预约保留15分钟，超时自动取消</li>
<li>工作日可直接到店，如遇客满需排队等候</li>
<li>包场/团建活动需提前3天预约，并支付50%定金</li>
</ul>', 'list', 1, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 其他规定 - 退改规则
('13', 'other', '退改规则', '<ul>
<li>未到店消费可随时退款（线上订单原路返回）</li>
<li>已开始拼豆制作但未完成，可按剩余材料折算退款</li>
<li>完成熨烫后视为服务完成，不接受退款</li>
</ul>', 'list', 2, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 其他规定 - 禁止事项
('14', 'other', '禁止事项', '<ul>
<li>🚫 禁止在店内吸烟、饮酒</li>
<li>🚫 禁止携带宠物（导盲犬除外）</li>
<li>🚫 禁止外带餐食（婴儿食品除外）</li>
<li>🚫 禁止制作涉及侵权、暴力、色情等违法违规图案</li>
<li>🚫 禁止将拼豆带出店外（购买的成品/材料包除外）</li>
</ul>', 'list', 3, 1, UNIX_TIMESTAMP() * 1000, NULL),

-- 其他规定 - 财物保管
('15', 'other', '财物保管', '<ul>
<li>请妥善保管个人贵重物品</li>
<li>本店提供自助储物柜（扫码使用），贵重物品请务必入柜</li>
<li>放置在公共区域的物品如有丢失，本店不承担赔偿责任</li>
</ul>', 'list', 4, 1, UNIX_TIMESTAMP() * 1000, NULL);
