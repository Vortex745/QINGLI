# 闲趣后端实现日志

## 项目概述
闲趣（xianqu）是一个基于位置的校园/社区二手交易和社交平台，支持"高校版"和"社区版"双模式切换。

---

## 技术栈
- **框架**: Spring Boot 3.4.1
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.7
- **认证**: JWT (jjwt 0.9.1)
- **开发工具**: Lombok, FastJSON2, Apache Commons Lang3
- **API**: 微信小程序登录接口

---

## 项目结构

```
xianqu_backend/
├── src/main/java/com/jin/xianqu_backend/
│   ├── common/              # 通用工具类
│   │   ├── BaseResponse.java    # 通用响应封装（旧版）
│   │   ├── ErrorCode.java       # 错误码枚举
│   │   ├── Result.java          # 统一返回对象（新版）
│   │   ├── ResultUtils.java     # 返回工具类
│   │   └── UserContext.java     # 用户上下文（ThreadLocal）
│   ├── config/              # 配置类
│   │   ├── MyBatisPlusConfig.java  # MyBatis Plus配置
│   │   ├── RestTemplateConfig.java # HTTP客户端配置
│   │   └── WebMvcConfig.java       # Web MVC配置（拦截器、CORS、静态资源）
│   ├── constant/            # 常量定义
│   │   ├── CommonConstant.java  # 通用常量
│   │   └── UserConstant.java    # 用户常量
│   ├── controller/          # 控制器层
│   │   ├── MainController.java  # 主控制器（健康检查）
│   │   ├── UserController.java  # 用户接口
│   │   ├── AreaController.java  # 区域接口
│   │   ├── GoodsController.java # 商品接口
│   │   ├── PostController.java  # 帖子接口
│   │   ├── FileController.java  # 文件上传接口
│   │   └── CommentController.java # 评论接口
│   ├── exception/           # 异常处理
│   │   ├── BusinessException.java      # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── interceptor/         # 拦截器
│   │   └── LoginInterceptor.java       # 登录拦截器（JWT验证）
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── AreaMapper.java
│   │   ├── GoodsMapper.java
│   │   ├── PostMapper.java
│   │   └── CommentMapper.java
│   ├── model/               # 数据模型
│   │   ├── dto/             # 数据传输对象
│   │   │   ├── LoginDTO.java
│   │   │   ├── UserUpdateDTO.java
│   │   │   ├── GoodsAddDTO.java
│   │   │   ├── GoodsUpdateDTO.java
│   │   │   ├── PostAddDTO.java
│   │   │   ├── PostUpdateDTO.java
│   │   │   └── CommentAddDTO.java
│   │   ├── entity/          # 数据库实体
│   │   │   ├── User.java
│   │   │   ├── Area.java
│   │   │   ├── Goods.java
│   │   │   ├── Post.java
│   │   │   └── Comment.java
│   │   └── vo/              # 视图对象
│   │       ├── LoginVO.java
│   │       ├── AreaMatchVO.java
│   │       └── CommentVO.java
│   ├── service/             # 业务逻辑层
│   │   ├── UserService.java
│   │   ├── AreaService.java
│   │   ├── GoodsService.java
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   └── impl/
│   │       ├── UserServiceImpl.java
│   │       ├── AreaServiceImpl.java
│   │       ├── GoodsServiceImpl.java
│   │       ├── PostServiceImpl.java
│   │       └── CommentServiceImpl.java
│   └── utils/               # 工具类
│       └── JwtUtils.java            # JWT工具类
└── src/main/resources/
    ├── application.yml      # 应用配置
    ├── mapper/              # MyBatis XML映射文件目录
    └── sql/                 # 数据库脚本
        └── V1__Init_Tables.sql  # 初始化表结构
```

---

## 已实现功能

### 一、标准化体系建设

#### 1. 统一返回格式
**文件**: `common/Result.java`

```java
{
  "code": 0,        // 状态码，0表示成功
  "msg": "ok",      // 提示信息
  "data": {...}     // 数据体
}
```

**静态方法**:
- `Result.success(data)` - 成功返回
- `Result.error(msg)` - 失败返回
- `Result.error(code, msg)` - 自定义错误码返回

#### 2. 全局异常处理
**文件**: `exception/GlobalExceptionHandler.java`

- 捕获 `BusinessException`: 返回业务错误信息
- 捕获 `Exception`: 统一返回"系统繁忙"，避免暴露堆栈信息

#### 3. 跨域配置
**文件**: `config/CorsConfig.java`

- 允许所有域名访问（开发环境）
- 支持 Cookie 凭证传递
- 支持 GET/POST/PUT/DELETE/OPTIONS 方法

---

### 二、用户模块（微信登录）

#### 1. 数据库设计
**表名**: `sys_user`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| openid | VARCHAR(64) | 微信唯一标识 |
| nickname | VARCHAR(50) | 昵称 |
| avatar_url | VARCHAR(255) | 头像URL |
| role | VARCHAR(20) | 角色（user/admin/ban） |
| current_area_id | BIGINT | 当前所在区域ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2. 登录流程
**接口**: `POST /api/user/login`

**请求参数** (`LoginDTO`):
```json
{
  "code": "微信临时登录凭证"
}
```

**返回数据** (`LoginVO`):
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 1,
    "openid": "oXXXX...",
    "token": "uuid-token",
    "nickname": "微信用户",
    "avatarUrl": null
  }
}
```

**业务逻辑**:
1. 接收前端传来的微信临时凭证 `code`
2. 调用微信官方接口换取 `openid`
3. 根据 `openid` 查询数据库：
   - 新用户：自动注册（插入记录）
   - 老用户：直接获取信息
4. 生成 UUID Token 返回给前端
5. 前端后续请求需携带此 Token

**核心代码**: `service/impl/UserServiceImpl.java`

---

### 三、双模式定位匹配（项目亮点）

#### 1. 数据库设计
**表名**: `sys_area`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(100) | 区域名称 |
| type | VARCHAR(20) | 类型（school-高校，community-社区） |
| latitude | DECIMAL(10,6) | 纬度 |
| longitude | DECIMAL(10,6) | 经度 |

#### 2. 区域匹配接口
**接口**: `GET /api/area/match`

**请求参数**:
- `latitude`: 用户当前纬度
- `longitude`: 用户当前经度
- `userId` (可选): 用户ID，传入后会自动更新用户的 `current_area_id`

**返回数据** (`AreaMatchVO`):
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 1,
    "name": "XX大学",
    "type": "school",
    "distance": 123.45
  }
}
```

#### 3. 匹配算法
**核心逻辑** (`service/impl/AreaServiceImpl.java`):

1. **Haversine 公式**: 计算地球表面两点间的球面距离
2. **遍历所有区域**: 找到距离用户最近的区域
3. **半径判定**: 默认 5000 米范围内才算匹配成功
4. **自动更新**: 匹配成功后，将用户的 `current_area_id` 更新到数据库

**意义**:
- 高校版：用户在校园内，自动切换到高校模式
- 社区版：用户在社区内，自动切换到社区模式
- 跨区域：用户移动到新区域，自动切换对应内容

---

### 四、商品模块（区域隔离）

#### 1. 数据库设计
**表名**: `bus_goods`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 发布用户ID |
| area_id | BIGINT | 所属区域ID（关键字段） |
| name | VARCHAR(100) | 商品名称 |
| description | TEXT | 商品描述 |
| price | DECIMAL(10,2) | 价格 |
| image_url | VARCHAR(500) | 图片URL |
| status | INT | 状态（0-上架，1-下架） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2. 商品列表接口
**接口**: `GET /api/goods/list`

**请求参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `areaId`: 区域ID（必传，实现隔离）

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 3. 隔离逻辑
**核心代码** (`service/impl/GoodsServiceImpl.java`):

```java
// 强制过滤区域
queryWrapper.eq("area_id", areaId);
// 只查询上架商品
queryWrapper.eq("status", 0);
// 按时间倒序
queryWrapper.orderByDesc("create_time");
```

**效果**:
- 高校用户只能看到本校的二手商品
- 社区用户只能看到本社区的闲置物品
- 实现真正的"区域隔离"

---

### 五、动态/帖子模块（类型分类）

#### 1. 数据库设计
**表名**: `bus_post`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 发布用户ID |
| area_id | BIGINT | 所属区域ID |
| type | INT | 类型（1-失物招领，3-校园广场） |
| content | TEXT | 内容 |
| image_urls | VARCHAR(1000) | 图片URLs（逗号分隔） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2. 帖子列表接口
**接口**: `GET /api/post/list`

**请求参数**:
- `page`: 页码
- `size`: 每页数量
- `areaId`: 区域ID（可选）
- `type`: 帖子类型（可选，1-失物招领，3-校园广场）

**返回数据**: 同商品列表格式

#### 3. 业务逻辑
**核心代码** (`service/impl/PostServiceImpl.java`):

```java
// 区域过滤
if (areaId != null) {
    queryWrapper.eq("area_id", areaId);
}
// 类型过滤
if (type != null) {
    queryWrapper.eq("type", type);
}
```

**应用场景**:
- 失物招领（type=1）：仅高校版显示
- 校园广场（type=3）：高校/社区均可显示
- 前端根据区域类型动态调整 Tabbar 显示内容

---

### 六、JWT 认证与权限拦截

#### 1. JWT Token 生成
**文件**: `utils/JwtUtils.java`

**功能**:
- 使用 `jjwt` 库生成和解析 JWT Token
- Token 有效期：7天
- 密钥：`xianqu_secret_key_change_it_in_prod`（生产环境需更换）

**Token 内容**:
```json
{
  "userId": 1,
  "role": "user",
  "iat": 1736582400,
  "exp": 1737187200
}
```

#### 2. 登录拦截器
**文件**: `interceptor/LoginInterceptor.java`

**拦截逻辑**:
1. 从请求头 `Authorization` 获取 Token
2. 使用 `JwtUtils.parseToken()` 解析 Token
3. 验证成功：将 `userId` 存入 `UserContext`（ThreadLocal）
4. 验证失败：返回 401 状态码

**白名单**（不拦截）:
- `/user/login` - 登录接口
- `/health` - 健康检查
- `/upload/**` - 静态资源
- `/profile/**` - 上传文件访问路径

#### 3. UserContext（用户上下文）
**文件**: `common/UserContext.java`

**作用**:
- 使用 `ThreadLocal` 存储当前请求的登录用户 ID
- 业务代码可随时通过 `UserContext.getUserId()` 获取当前用户
- 请求结束后自动清理，避免内存泄漏

**使用示例**:
```java
Long userId = UserContext.getUserId();
User user = userMapper.selectById(userId);
```

---

### 七、文件上传（本地存储）

#### 1. 上传接口
**接口**: `POST /api/file/upload`

**请求参数**:
- `file`: MultipartFile 类型的文件

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": "http://localhost:8081/api/profile/uuid-filename.jpg"
}
```

#### 2. 存储逻辑
**文件**: `controller/FileController.java`

1. **文件重命名**: 使用 UUID 防止文件名冲突
2. **存储位置**: 项目根目录 `/upload/` 文件夹
3. **静态资源映射**: 通过 `/api/profile/**` 访问上传的文件

#### 3. 静态资源配置
**文件**: `config/WebMvcConfig.java`

```java
registry.addResourceHandler("/profile/**")
        .addResourceLocations("file:" + uploadPath);
```

**访问示例**:
- 上传文件保存为：`E:\testJava\xianqu_taro\xianqu_backend\upload\abc123.jpg`
- 访问地址：`http://localhost:8081/api/profile/abc123.jpg`

**生产环境建议**:
- 使用阿里云 OSS 或腾讯云 COS
- 提高可靠性和访问速度
- 减轻服务器存储压力

---

### 八、发布功能（商品/帖子）

#### 1. 商品发布
**接口**: `POST /api/goods/add`

**请求参数** (`GoodsAddDTO`):
```json
{
  "name": "二手自行车",
  "description": "9成新，价格可议",
  "price": 200.00,
  "imageUrl": "http://localhost:8081/api/profile/xxx.jpg"
}
```

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": 123  // 商品ID
}
```

**业务逻辑** (`service/impl/GoodsServiceImpl.java`):
1. 从 `UserContext` 获取当前登录用户 ID
2. 查询用户的 `current_area_id`（当前所在区域）
3. 自动填充字段：
   - `userId`: 当前用户
   - `areaId`: 用户当前区域
   - `status`: 0（上架）
   - `createTime`: 当前时间
4. 保存到数据库

**关键点**:
- 用户必须先完成定位（调用 `/api/area/match`）
- 商品自动关联到用户当前区域
- 实现区域隔离：用户只能在自己所在区域发布

#### 2. 帖子发布
**接口**: `POST /api/post/add`

**请求参数** (`PostAddDTO`):
```json
{
  "content": "捡到一张校园卡，失主请联系",
  "imageUrls": "http://xxx.jpg,http://yyy.jpg",
  "type": 1  // 1-失物招领，3-校园广场
}
```

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": 456  // 帖子ID
}
```

**业务逻辑**: 同商品发布,自动关联 `userId` 和 `areaId`

---

### 九、内容管理（编辑与删除）

#### 1. 商品编辑
**接口**: `PUT /api/goods/update`

**请求参数** (`GoodsUpdateDTO`):
```json
{
  "id": 123,
  "name": "二手自行车（已降价）",
  "description": "9成新，价格可议",
  "price": 180.00,
  "imageUrl": "http://localhost:8081/api/profile/xxx.jpg"
}
```

**权限校验**:
```java
if (!oldGoods.getUserId().equals(userId)) {
    throw new BusinessException(403, "无权修改");
}
```

#### 2. 商品删除
**接口**: `DELETE /api/goods/{id}`

**逻辑删除**:
- 使用 MyBatis Plus 的 `@TableLogic` 注解
- 删除时自动将 `is_delete` 字段设置为 1
- 查询时自动过滤已删除数据

**权限校验**: 仅允许商品所有者删除

#### 3. 帖子编辑与删除
**接口**:
- `PUT /api/post/update`
- `DELETE /api/post/{id}`

**逻辑**: 与商品管理相同，包含权限校验和逻辑删除

---

### 十、用户资料管理

#### 1. 用户信息更新
**接口**: `PUT /api/user/update`

**请求参数** (`UserUpdateDTO`):
```json
{
  "nickname": "张三",
  "avatarUrl": "http://localhost:8081/api/profile/avatar.jpg",
  "phone": "13800138000"
}
```

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true
}
```

#### 2. 头像上传流程
1. **前端上传图片**: 调用 `POST /api/file/upload` 获取图片 URL
2. **更新用户信息**: 将获取的 URL 作为 `avatarUrl` 调用更新接口
3. **数据库更新**: 更新 `sys_user` 表的 `avatar_url` 字段

---

### 十一、社交互动（评论系统）

#### 1. 数据库设计
**表名**: `bus_comment`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 评论者ID |
| target_id | BIGINT | 目标ID（商品/帖子） |
| type | INT | 1-商品，2-帖子 |
| content | TEXT | 评论内容 |
| is_delete | INT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |

#### 2. 发布评论
**接口**: `POST /api/comment/add`

**请求参数** (`CommentAddDTO`):
```json
{
  "targetId": 123,
  "type": 1,
  "content": "这个商品还在吗？"
}
```

**业务逻辑**:
- 从 `UserContext` 获取当前登录用户 ID
- 自动填充 `userId` 和 `createTime`
- 保存到数据库

#### 3. 评论列表
**接口**: `GET /api/comment/list`

**请求参数**:
- `targetId`: 目标ID（商品/帖子）
- `type`: 类型（1-商品，2-帖子）
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）

**返回数据** (`CommentVO`):
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 10,
        "nickname": "张三",
        "avatarUrl": "http://xxx.jpg",
        "content": "这个商品还在吗？",
        "createTime": "2026-01-11 16:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

**特性**:
- 自动关联用户信息（昵称、头像）
- 按时间倒序排列
- 支持分页查询

---

### 十二、数据库优化

#### 1. 索引优化
**文件**: `src/main/resources/sql/V1__Init_Tables.sql`

**已添加索引**:
```sql
-- 商品表索引
KEY `idx_area_id` (`area_id`),
KEY `idx_user_id` (`user_id`),
KEY `idx_create_time` (`create_time`)

-- 帖子表索引
KEY `idx_area_id` (`area_id`),
KEY `idx_user_id` (`user_id`),
KEY `idx_create_time` (`create_time`)

-- 评论表索引
KEY `idx_target_type` (`target_id`, `type`)
```

**优化效果**:
- 提高区域过滤查询效率（`area_id`）
- 加速用户内容查询（`user_id`）
- 优化时间排序性能（`create_time`）

#### 2. 逻辑删除配置
**application.yml**:
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDelete
      logic-delete-value: 1
      logic-not-delete-value: 0
```

**实体类配置**:
```java
@TableLogic
private Integer isDelete;
```

**效果**:
- 删除操作自动转换为 UPDATE 语句
- 查询自动过滤已删除数据
- 保留历史数据，便于审计和恢复

---

## 配置文件

### application.yml

```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  application:
    name: xianqu-backend
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/xianqu_pdb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      logic-delete-field: isDelete
      logic-delete-value: 1
      logic-not-delete-value: 0

logging:
  level:
    com.jin.xianqu_backend: debug

# 微信小程序配置（需补充）
wx:
  open:
    app_id: wx_appid_placeholder
    app_secret: wx_secret_placeholder
```

---

## API 接口汇总

| 接口路径 | 方法 | 功能 | 参数 | 需要认证 |
|---------|------|------|---------|---------|
| `/api/health` | GET | 健康检查 | 无 | ❌ |
| `/api/user/login` | POST | 微信登录 | code | ❌ |
| `/api/user/update` | PUT | 更新用户信息 | nickname, avatarUrl, phone | ✅ |
| `/api/area/match` | GET | 区域匹配 | latitude, longitude, userId | ❌ |
| `/api/file/upload` | POST | 文件上传 | file | ✅ |
| `/api/goods/list` | GET | 商品列表 | page, size, areaId | ❌ |
| `/api/goods/add` | POST | 发布商品 | name, description, price, imageUrl | ✅ |
| `/api/goods/update` | PUT | 编辑商品 | id, name, description, price, imageUrl | ✅ |
| `/api/goods/{id}` | DELETE | 删除商品 | id | ✅ |
| `/api/post/list` | GET | 帖子列表 | page, size, areaId, type | ❌ |
| `/api/post/add` | POST | 发布帖子 | content, imageUrls, type | ✅ |
| `/api/post/update` | PUT | 编辑帖子 | id, content, imageUrls | ✅ |
| `/api/post/{id}` | DELETE | 删除帖子 | id | ✅ |
| `/api/comment/add` | POST | 发布评论 | targetId, type, content | ✅ |
| `/api/comment/list` | GET | 评论列表 | targetId, type, page, size | ❌ |

---

## 核心特性

### 1. 区域隔离机制
- 所有业务数据（商品、帖子）都关联 `area_id`
- 查询时强制过滤，确保用户只看到当前区域内容
- 用户切换位置时，自动更新 `current_area_id`

### 2. 双模式切换
- **高校版**: type=school，显示失物招领、校园广场
- **社区版**: type=community，显示社区闲置、邻里互助
- 前端根据 `AreaMatchVO.type` 动态调整 UI

### 3. 自动定位匹配
- 使用 Haversine 公式精确计算距离
- 5km 半径内自动匹配最近区域
- 支持多区域管理（多个高校/社区）

---

## 待完成事项

### 后端
- [ ] 补充微信小程序 AppID 和 AppSecret
- [x] 实现 Token 验证拦截器（JWT）
- [x] 添加文件上传接口（本地存储）
- [x] 完善用户信息更新接口
- [x] 添加商品发布接口
- [x] 添加帖子发布接口
- [x] 添加商品编辑/删除接口
- [x] 添加帖子编辑/删除接口
- [x] 实现评论功能
- [ ] 实现收藏/点赞功能
- [ ] 添加订单管理模块
- [ ] 升级文件上传到 OSS/COS

### 数据库
- [x] 创建所有数据表（sys_user, sys_area, bus_goods, bus_post, bus_comment）
- [x] 添加索引优化（area_id, user_id, create_time）
- [ ] 准备测试数据

### 前端联调
- [x] 封装 Taro 请求工具（request.ts）
- [x] 实现微信登录流程
- [x] 首页定位并调用区域匹配接口
- [x] 实现商品列表页面（区域隔离）
- [x] 根据区域类型动态显示标签（高校版/社区版）
- [x] 实现商品收藏功能（爱心图标）
- [x] 实现下单功能（"想要"按钮）
- [x] 实现双模式 UI 金刚区切换
- [x] 实现商品发布页面
- [x] 实现帖子发布页面
- [x] 实现帖子列表页面
- [x] 首页金刚区动态路由
- [ ] 根据区域类型动态渲染 Tabbar
- [ ] 实现商品详情页
- [ ] 实现帖子详情页

### 交易与社交
- [x] 实现订单模块（下单、订单列表）
- [x] 实现收藏/点赞模块（Toggle 切换）
- [x] 商品状态同步（下单后变为交易中）
- [x] 实现订单状态管理（确认收货、取消订单）
- [x] 实现我的收藏页面
- [ ] 实现我的发布页面
- [ ] 实现评论详情展示

---

## 更新日志

### 2026-01-11 傍晚
- ✅ 实现商品编辑接口（权限校验）
- ✅ 实现商品删除接口（逻辑删除）
- ✅ 实现帖子编辑接口（权限校验）
- ✅ 实现帖子删除接口（逻辑删除）
- ✅ 实现用户信息更新接口
- ✅ 实现评论系统（发布、列表、用户信息关联）
- ✅ 创建数据库初始化脚本（V1__Init_Tables.sql）
- ✅ 添加数据库索引优化
- ✅ 配置 MyBatis Plus 逻辑删除

### 2026-01-11 晚上（前端联调）
- ✅ 封装全局请求工具（request.ts）
- ✅ 实现请求拦截（自动添加 Authorization Token）
- ✅ 实现响应拦截（统一处理 code 和 401 跳转）
- ✅ 实现微信一键登录（mine 页面）
- ✅ 实现 Token 本地缓存（登录状态持久化）
- ✅ 实现区域匹配初始化（app.ts 启动时定位）
- ✅ 实现首页商品列表（区域隔离展示）
- ✅ 实现动态区域标签（高校版/社区版切换）
- ✅ 修复拦截器白名单（添加 /area/match 等公开接口）
- ✅ 修复页面跳转路径错误
- ✅ 修复 userId=null 参数问题

### 2026-01-11 深夜（交易闭环与社交增强）
**后端功能**:
- ✅ 实现订单模块（Order 实体、Mapper、Service、Controller）
- ✅ 实现下单接口（POST /api/order/add）
- ✅ 实现订单列表接口（GET /api/order/list）
- ✅ 实现商品状态同步（下单后自动更新为"交易中"）
- ✅ 实现收藏/点赞模块（Favorite 实体、Mapper、Service、Controller）
- ✅ 实现收藏切换接口（POST /api/favorite/toggle）
- ✅ 实现商品列表关联查询（GoodsVO 包含收藏状态和卖家信息）
- ✅ 引入 Knife4j 接口文档（添加依赖和配置）
- ✅ 更新拦截器白名单（添加 Swagger 相关路径）

**前端功能**:
- ✅ 实现商品收藏功能（爱心图标动态切换）
- ✅ 实现下单功能（"想要"按钮 + 确认弹窗）
- ✅ 实现双模式 UI 联动（高校版/社区版金刚区动态切换）
- ✅ 商品卡片增强（显示卖家信息、收藏状态、交易状态）
- ✅ 优化商品列表刷新逻辑（登录状态变化时自动刷新）

### 2026-01-11 深夜 II（发布流程与交易闭环）
**前端完善**:
- ✅ 实现商品发布页面（支持图片上传、价格验证）
- ✅ 实现帖子发布页面（支持多图并发上传、类型选择）
- ✅ 实现我的订单页面（展示订单状态、确认收货、取消订单）
- ✅ 实现我的收藏页面（展示收藏列表、一键取消）
- ✅ 实现帖子列表页面（根据区域动态切换标题：校园广场/邻里互助）
- ✅ 首页金刚区实现动态跳转路由

**后端增强**:
- ✅ 订单状态流转（实现 finish 和 cancel 接口）
- ✅ 订单取消逻辑（自动回滚商品状态为上架中）
- ✅ 订单完成逻辑（更新商品状态为已售出）
- ✅ 我的收藏列表接口（实现分页查询 GoodsVO）
- ✅ 开放文件上传接口权限（WebMvcConfig 配置）


### 2026-01-11 傍晚 II（搜索、分页与代码质量提升）
**后端优化**:
- ✅ 修复后端编译错误（Lombok 注解处理器配置）
- ✅ 配置 maven-compiler-plugin 支持 Lombok
- ✅ 修复 Jakarta EE 迁移问题（javax.validation → jakarta.validation）
- ✅ 实现商品搜索功能（keyword 模糊查询）
- ✅ 实现帖子搜索功能（keyword 模糊查询）
- ✅ 实现"我的发布"接口（listMyGoods、listMyPosts）
- ✅ 实现 AOP 日志切面（LogAspect，记录所有 Controller 请求）
- ✅ 添加 JSR303 参数校验（GoodsAddDTO 添加 @NotBlank、@NotNull、@Min）
- ✅ 添加 spring-boot-starter-validation 依赖
- ✅ 添加 spring-boot-starter-aop 依赖
- ✅ 导出 BASE_URL 常量供前端文件上传使用

**前端优化**:
- ✅ 实现首页搜索框（支持商品关键词搜索）
- ✅ 实现下拉刷新功能（usePullDownRefresh）
- ✅ 实现触底加载更多（useReachBottom，无限滚动）
- ✅ 实现"我的发布"页面（Tab 切换查看商品/帖子）
- ✅ 实现发布内容删除功能（商品、帖子）
- ✅ 修复 tabBar 配置错误（移除不存在的 publish 页面）
- ✅ 修复前端编译错误（移除多余的 markdown 代码围栏）
- ✅ 优化分页逻辑（page 状态管理、hasMore 判断）

**代码质量**:
- ✅ 后端成功编译通过（mvn clean compile）
- ✅ 前端成功编译通过（npm run dev:weapp）
- ✅ 统一导入规范（移除未使用的导入）
- ✅ 修复语法错误（import 位置错误）

### 2026-01-11 晚上
**商品详情页实现**:
- ✅ 后端实现 `GET /api/goods/{id}` 接口（返回商品详情、卖家信息、收藏状态）
- ✅ 前端实现商品详情页（`pages/goods-detail/index`）
- ✅ 集成图片轮播、价格展示、卖家信息、评论列表
- ✅ 实现收藏切换和"我想要"下单功能
- ✅ 从商品列表页跳转到详情页

**动态 Tabbar 实现**:
- ✅ 启用自定义 Tabbar（`app.config.ts` 设置 `custom: true`）
- ✅ 创建自定义 Tabbar 组件（`src/custom-tab-bar/index.tsx`）
- ✅ 实现中间 Tab 文字动态切换（校园广场 ↔ 邻里互助）
- ✅ 基于用户区域类型（`AreaMatchVO.type`）自动更新 Tabbar 文字
- ✅ 在首页、我的、帖子列表页面添加 Tabbar 状态同步逻辑

**帖子详情页实现**:
- ✅ 创建 `PostVO` 类（包含用户信息和收藏状态）
- ✅ 添加 Swagger `@Schema` 注解到 `PostVO`（提升 API 文档质量）
- ✅ 重构 `PostService` 返回 `Page<PostVO>`（替代原 `Page<Post>`）
- ✅ 实现 `GET /api/post/{id}` 接口（返回帖子详情、发布者信息、收藏状态）
- ✅ 前端实现帖子详情页（`pages/post-detail/index`）
- ✅ 集成帖子内容展示、多图预览、评论列表、发表评论功能
- ✅ 根据帖子类型动态设置导航栏标题（失物招领/校园广场）
- ✅ 从帖子列表页跳转到详情页

**工程质量优化**:
- ✅ 统一 VO 模式（GoodsVO、PostVO）提升 API 响应质量
- ✅ 添加 Swagger 注解到新增 DTO/VO 类
- ✅ 修复前端导入错误（`useDidShow` 未导入）
- ✅ 清理未使用的导入（`Post` entity in `PostController`）
- ✅ 优化分页逻辑（区域切换时自动重置页码和列表）

### 2026-01-11 下午
- ✅ 实现 JWT Token 生成和解析（JwtUtils）
- ✅ 实现登录拦截器（LoginInterceptor）
- ✅ 实现 UserContext（ThreadLocal 用户上下文）
- ✅ 实现文件上传接口（本地存储）
- ✅ 配置静态资源映射
- ✅ 实现商品发布接口（自动关联用户和区域）
- ✅ 实现帖子发布接口（自动关联用户和区域）
- ✅ 更新 WebMvcConfig（整合拦截器和 CORS）
- ✅ 添加 jjwt 依赖到 pom.xml

### 2026-01-11 上午
- ✅ 初始化项目结构
- ✅ 创建 application.yml 配置文件
- ✅ 实现统一返回格式 Result
- ✅ 实现全局异常处理
- ✅ 实现用户模块（微信登录）
- ✅ 实现区域匹配模块（Haversine 算法）
- ✅ 实现商品模块（区域隔离查询）
- ✅ 实现帖子模块（类型分类查询）
- ✅ 配置 CORS 跨域
- ✅ 配置 MyBatis Plus 分页插件

---

## 技术亮点

1. **位置智能匹配**: 基于 Haversine 公式的精确地理位置计算
2. **动态模式切换**: 根据用户位置自动切换高校版/社区版
3. **数据隔离**: 通过 area_id 实现多租户数据隔离
4. **JWT 认证体系**: 完整的 Token 生成、验证、拦截机制
5. **ThreadLocal 上下文**: 优雅的用户信息传递方案
6. **权限校验机制**: Service 层严格校验用户权限，防止越权操作
7. **逻辑删除**: 使用 MyBatis Plus @TableLogic 实现软删除，保留历史数据
8. **数据库索引优化**: 针对高频查询字段添加索引，提升性能
9. **评论系统**: 支持商品和帖子评论，自动关联用户信息
10. **标准化开发**: 统一返回格式、全局异常处理、分层架构
11. **微信生态集成**: 无缝对接微信小程序登录体系
12. **自动关联机制**: 发布内容自动关联用户和区域，实现区域隔离
13. **交易闭环**: 订单模块实现商品状态同步，防止一物多卖
14. **社交增强**: Toggle 式收藏系统，支持商品和帖子多态收藏
15. **VO 模式优化**: 商品列表自动关联卖家信息和收藏状态
16. **接口文档**: 集成 Knife4j，提供可视化 API 文档

---

## 联系方式
- 项目路径: `e:\testJava\xianqu_taro\xianqu_backend`
- 数据库: MySQL 8.0 (xianqu_pdb)
- 端口: 8081
- 上下文路径: /api
