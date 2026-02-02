# 闲趣 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8081/api`
- **认证方式**: JWT Token (请求头 `Authorization`)
- **返回格式**: JSON

### 统一返回格式

```json
{
  "code": 0,        // 状态码，0表示成功，非0表示失败
  "msg": "ok",      // 提示信息
  "data": {...}     // 数据体
}
```

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限 |
| 500 | 系统错误 |

---

## 一、用户模块

### 1.1 微信登录

**接口**: `POST /user/login`

**请求参数**:
```json
{
  "code": "微信临时登录凭证"
}
```

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 1,
    "openid": "oXXXX...",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "nickname": "微信用户",
    "avatarUrl": "http://xxx.jpg",
    "role": "user",
    "currentAreaId": 1
  }
}
```

**说明**:
- 首次登录自动注册
- 返回的 `token` 需要在后续请求中携带
- `currentAreaId` 为用户当前所在区域 ID

---

### 1.2 更新用户信息

**接口**: `PUT /user/update`

**需要认证**: ✅

**请求参数**:
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

---

## 二、区域模块

### 2.1 区域匹配

**接口**: `GET /area/match`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| latitude | Double | ✅ | 纬度 |
| longitude | Double | ✅ | 经度 |
| userId | Long | ❌ | 用户ID（传入后自动更新用户区域） |

**返回数据**:
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

**说明**:
- 使用 Haversine 公式计算距离
- 默认匹配 5km 范围内最近的区域
- `type` 可选值: `school`(高校版) / `community`(社区版)

---

## 三、商品模块

### 3.1 商品列表

**接口**: `GET /goods/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |
| areaId | Long | ❌ | 区域ID（实现区域隔离） |
| keyword | String | ❌ | 搜索关键词（模糊匹配商品名称） |

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "二手自行车",
        "description": "9成新，价格可议",
        "price": 200.00,
        "imageUrl": "http://xxx.jpg",
        "status": 0,
        "sellerId": 10,
        "sellerName": "张三",
        "sellerAvatar": "http://yyy.jpg",
        "favorite": true,
        "createTime": "2026-01-11 16:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

**说明**:
- `status`: 0-上架中，1-交易中，2-已售出
- `favorite`: 当前用户是否已收藏（需登录）
- 自动关联卖家信息和收藏状态

---

### 3.2 我的发布商品

**接口**: `GET /goods/my`

**需要认证**: ✅

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |

**返回数据**: 同商品列表

**说明**:
- 查询当前登录用户发布的所有商品
- 包含所有状态的商品（上架中、交易中、已售出）

---

### 3.3 发布商品

**接口**: `POST /goods/add`

**需要认证**: ✅

**请求参数**:
```json
{
  "name": "二手自行车",
  "description": "9成新，价格可议",
  "price": 200.00,
  "imageUrl": "http://localhost:8081/api/profile/xxx.jpg"
}
```

**参数校验**:
- `name`: 必填，不能为空
- `description`: 必填，不能为空
- `price`: 必填，必须大于0

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": 123  // 商品ID
}
```

**说明**:
- 自动关联当前登录用户
- 自动关联用户当前所在区域
- 默认状态为上架中（status=0）

---

### 3.4 编辑商品

**接口**: `PUT /goods/update`

**需要认证**: ✅

**请求参数**:
```json
{
  "id": 123,
  "name": "二手自行车（已降价）",
  "description": "9成新，价格可议",
  "price": 180.00,
  "imageUrl": "http://localhost:8081/api/profile/xxx.jpg"
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

**说明**:
- 仅允许商品所有者编辑
- 权限校验失败返回 403

---

### 3.5 删除商品

**接口**: `DELETE /goods/{id}`

**需要认证**: ✅

**路径参数**:
- `id`: 商品ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true
}
```

**说明**:
- 逻辑删除，数据不会真正删除
- 仅允许商品所有者删除

---

### 3.6 商品详情

**接口**: `GET /goods/{id}`

**路径参数**:
- `id`: 商品ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 123,
    "name": "二手自行车",
    "description": "9成新，价格可议",
    "price": 200.00,
    "imageUrl": "http://localhost:8081/api/profile/xxx.jpg",
    "status": 0,
    "userId": 10,
    "areaId": 1,
    "createTime": "2026-01-11 16:00:00",
    "sellerNickname": "张三",
    "sellerAvatar": "http://localhost:8081/api/profile/avatar.jpg",
    "favorite": true
  }
}
```

**说明**:
- 返回商品详细信息
- 自动关联卖家昵称和头像
- `favorite`: 当前登录用户是否已收藏该商品（未登录返回 false）
- 用于商品详情页展示

---

## 四、帖子模块

### 4.1 帖子列表

**接口**: `GET /post/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |
| areaId | Long | ❌ | 区域ID |
| type | Integer | ❌ | 帖子类型（1-失物招领，3-校园广场） |
| keyword | String | ❌ | 搜索关键词（模糊匹配内容） |

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 10,
        "areaId": 1,
        "type": 1,
        "content": "捡到一张校园卡",
        "imageUrls": "http://a.jpg,http://b.jpg",
        "createTime": "2026-01-11 16:00:00",
        "userNickname": "张三",
        "userAvatar": "http://xxx.jpg",
        "favorite": true
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

**说明**:
- `type`: 1-失物招领，3-校园广场
- `imageUrls`: 多张图片用逗号分隔
- 返回 `PostVO` 对象，包含发布者信息和收藏状态
- `favorite`: 当前用户是否已点赞/收藏（需登录）

---

### 4.2 我的发布帖子

**接口**: `GET /post/my`

**需要认证**: ✅

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |

**返回数据**: 同帖子列表

---

### 4.3 帖子详情

**接口**: `GET /post/{id}`

**路径参数**:
- `id`: 帖子ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 456,
    "userId": 10,
    "areaId": 1,
    "type": 1,
    "content": "捡到一张校园卡，失主请联系",
    "imageUrls": "http://a.jpg,http://b.jpg",
    "createTime": "2026-01-11 16:00:00",
    "userNickname": "张三",
    "userAvatar": "http://localhost:8081/api/profile/avatar.jpg",
    "favorite": false
  }
}
```

**说明**:
- 返回帖子详细信息
- 自动关联发布者昵称和头像
- `favorite`: 当前登录用户是否已点赞/收藏该帖子（未登录返回 false）
- 用于帖子详情页展示

---

### 4.4 发布帖子

**接口**: `POST /post/add`

**需要认证**: ✅

**请求参数**:
```json
{
  "content": "捡到一张校园卡，失主请联系",
  "imageUrls": "http://xxx.jpg,http://yyy.jpg",
  "type": 1
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

---

### 4.4 编辑帖子

**接口**: `PUT /post/update`

**需要认证**: ✅

**请求参数**:
```json
{
  "id": 456,
  "content": "捡到一张校园卡，已找到失主",
  "imageUrls": "http://xxx.jpg"
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

---

### 4.5 删除帖子

**接口**: `DELETE /post/{id}`

**需要认证**: ✅

**路径参数**:
- `id`: 帖子ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true
}
```

---

## 五、订单模块

### 5.1 下单

**接口**: `POST /order/add`

**需要认证**: ✅

**请求参数**:
```json
{
  "goodsId": 123
}
```

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": 789  // 订单ID
}
```

**说明**:
- 下单成功后，商品状态自动变为"交易中"
- 自动关联买家和卖家信息

---

### 5.2 订单列表

**接口**: `GET /order/list`

**需要认证**: ✅

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 789,
        "goodsId": 123,
        "goodsName": "二手自行车",
        "goodsPrice": 200.00,
        "goodsImageUrl": "http://xxx.jpg",
        "sellerId": 10,
        "sellerName": "张三",
        "buyerId": 20,
        "buyerName": "李四",
        "status": 0,
        "createTime": "2026-01-11 16:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

**说明**:
- `status`: 0-待确认，1-已完成，2-已取消
- 查询当前用户作为买家的所有订单

---

### 5.3 确认收货

**接口**: `PUT /order/finish/{id}`

**需要认证**: ✅

**路径参数**:
- `id`: 订单ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true
}
```

**说明**:
- 确认收货后，订单状态变为"已完成"
- 商品状态变为"已售出"

---

### 5.4 取消订单

**接口**: `PUT /order/cancel/{id}`

**需要认证**: ✅

**路径参数**:
- `id`: 订单ID

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true
}
```

**说明**:
- 取消订单后，订单状态变为"已取消"
- 商品状态回滚为"上架中"

---

## 六、收藏模块

### 6.1 收藏切换

**接口**: `POST /favorite/toggle`

**需要认证**: ✅

**请求参数**:
```json
{
  "targetId": 123,
  "type": 1
}
```

**参数说明**:
- `targetId`: 目标ID（商品/帖子）
- `type`: 1-商品，2-帖子

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": true  // true-已收藏，false-已取消
}
```

**说明**:
- Toggle 模式：已收藏则取消，未收藏则添加
- 返回当前收藏状态

---

### 6.2 我的收藏列表

**接口**: `GET /favorite/list`

**需要认证**: ✅

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "二手自行车",
        "description": "9成新",
        "price": 200.00,
        "imageUrl": "http://xxx.jpg",
        "status": 0,
        "sellerId": 10,
        "sellerName": "张三",
        "sellerAvatar": "http://yyy.jpg",
        "favorite": true,
        "createTime": "2026-01-11 16:00:00"
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1
  }
}
```

**说明**:
- 仅返回商品类型的收藏
- 自动关联商品详情和卖家信息

---

## 七、评论模块

### 7.1 发布评论

**接口**: `POST /comment/add`

**需要认证**: ✅

**请求参数**:
```json
{
  "targetId": 123,
  "type": 1,
  "content": "这个商品还在吗？"
}
```

**参数说明**:
- `targetId`: 目标ID（商品/帖子）
- `type`: 1-商品，2-帖子

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": 999  // 评论ID
}
```

---

### 7.2 评论列表

**接口**: `GET /comment/list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | ✅ | 目标ID |
| type | Integer | ✅ | 类型（1-商品，2-帖子） |
| page | Integer | ❌ | 页码，默认1 |
| size | Integer | ❌ | 每页数量，默认10 |

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 999,
        "userId": 20,
        "nickname": "李四",
        "avatarUrl": "http://zzz.jpg",
        "content": "这个商品还在吗？",
        "createTime": "2026-01-11 17:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

**说明**:
- 自动关联评论者信息
- 按时间倒序排列

---

## 八、文件上传

### 8.1 上传文件

**接口**: `POST /file/upload`

**需要认证**: ✅

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

**说明**:
- 文件保存在服务器 `/upload/` 目录
- 文件名使用 UUID 防止冲突
- 返回完整的访问 URL

---

## 九、健康检查

### 9.1 健康检查

**接口**: `GET /health`

**返回数据**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": "ok"
}
```

**说明**:
- 用于检查服务是否正常运行
- 不需要认证

---

## 附录

### A. 认证说明

所有需要认证的接口，请求头必须携带 Token：

```
Authorization: eyJhbGciOiJIUzI1NiJ9...
```

Token 获取方式：
1. 调用 `/user/login` 接口登录
2. 从返回数据中获取 `token` 字段
3. 后续请求携带此 Token

Token 有效期：7天

### B. 分页说明

所有列表接口均支持分页，返回格式统一为：

```json
{
  "records": [],    // 数据列表
  "total": 100,     // 总记录数
  "size": 10,       // 每页数量
  "current": 1,     // 当前页码
  "pages": 10       // 总页数
}
```

### C. 区域隔离说明

商品和帖子均实现区域隔离：
- 发布时自动关联用户当前区域
- 查询时可通过 `areaId` 过滤
- 确保用户只看到当前区域的内容

### D. 搜索功能说明

商品和帖子列表均支持关键词搜索：
- 商品：模糊匹配 `name` 字段
- 帖子：模糊匹配 `content` 字段
- 支持与其他过滤条件组合使用

---

**文档版本**: v1.0  
**更新时间**: 2026-01-11  
**维护者**: 闲趣开发团队
