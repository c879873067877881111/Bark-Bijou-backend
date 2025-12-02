# Bark Bijou 電商 API 服務

> 基於 Spring Boot 3.5.7 + PostgreSQL + Redis 的現代化電商 API 系統

## 技術架構

### 核心技術棧
- **後端框架**: Spring Boot 3.5.7 (Java 21)
- **數據持久化**: MyBatis 3.0.3 + PostgreSQL 15
- **緩存**: Redis 7
- **安全認證**: Spring Security + JWT (雙 Token 架構)
- **API 文檔**: OpenAPI 3 + Swagger UI
- **日誌系統**: Logback + JSON 格式 (ELK 友好)
- **容器化**: Docker Compose

### 系統特性
- ✅ **RESTful API 設計** - 遵循 REST 架構風格
- ✅ **JWT 雙 Token 認證** - Access Token + Refresh Token 自動刷新
- ✅ **完整的電商功能** - 商品、分類、品牌、購物車、訂單管理
- ✅ **多環境支持** - 開發/測試/生產環境分離
- ✅ **結構化日誌** - ELK Stack 友好的日誌格式
- ✅ **CORS 支持** - 前端跨域請求配置
- ✅ **API 文檔自動生成** - 完整的 Swagger 註解

## 快速開始

### 環境要求
- Java 21+
- Docker & Docker Compose
- Maven 3.6+

### 1. 克隆項目
```bash
git clone <repository-url>
cd api-server
```

### 2. 啟動基礎服務
```bash
# 啟動 PostgreSQL + Redis
docker-compose up -d
```

### 3. 啟動應用
```bash
# 開發模式
./mvnw spring-boot:run

# 或指定配置文件
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. 驗證部署
- **API 基礎路徑**: http://localhost:8080/api
- **API 文檔**: http://localhost:8080/swagger-ui.html
- **健康檢查**: http://localhost:8080/actuator/health

## API 概覽

### 認證管理 `/api/auth`
```http
POST   /api/auth/register    # 用戶註冊
POST   /api/auth/login       # 用戶登入
GET    /api/auth/me          # 獲取當前用戶
POST   /api/auth/refresh     # 刷新 Token
POST   /api/auth/logout      # 用戶登出
```

### 商品管理 `/api/products`
```http
GET    /api/products              # 商品列表 (支持分頁/搜索)
GET    /api/products/{id}         # 商品詳情
POST   /api/products              # 創建商品 [需認證]
PUT    /api/products/{id}         # 更新商品 [需認證]
PATCH  /api/products/{id}/stock   # 更新庫存 [需認證]
```

### 購物車管理 `/api/cart` [需認證]
```http
GET    /api/cart               # 獲取購物車
POST   /api/cart/items         # 添加商品
PUT    /api/cart/items/{id}    # 更新數量
DELETE /api/cart/items/{id}    # 移除商品
```

### 訂單管理 `/api/orders` [需認證]
```http
GET    /api/orders             # 訂單列表
POST   /api/orders             # 創建訂單
GET    /api/orders/{id}        # 訂單詳情
PUT    /api/orders/{id}/cancel # 取消訂單
```

## 數據庫設計

### 核心實體關係
```
member (用戶) ──┬── cart_items (購物車)
              ├── orders (訂單)
              └── member_coupons (優惠券)

product (商品) ──┬── brand (品牌)
               ├── category (分類)
               └── product_images (圖片)

orders (訂單) ── order_items (訂單項目)
```

### 資料庫連接
```properties
# PostgreSQL (默認)
spring.datasource.url=jdbc:postgresql://localhost:5432/bark_bijou
spring.datasource.username=user
spring.datasource.password=password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## 安全認證

### JWT Token 流程
1. **登入**: 返回 Access Token (15分鐘) + Refresh Token (7天)
2. **API 請求**: Header 中攜帶 `Authorization: Bearer <access_token>`
3. **自動刷新**: Access Token 過期時使用 Refresh Token 獲取新 Token

### 前端集成示例
```javascript
// 登入
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ usernameOrEmail: 'user', password: 'pass' })
});

// 攜帶 Token 的請求
const apiResponse = await fetch('/api/cart', {
  headers: { 'Authorization': `Bearer ${accessToken}` }
});
```

## 開發指南

### 項目結構
```
src/main/java/com/smallnine/apiserver/
├── controller/     # REST API 控制器
├── service/        # 業務邏輯服務
├── dao/           # 數據訪問層
├── entity/        # 實體模型
├── dto/           # 數據傳輸對象
├── config/        # 配置類
├── filter/        # 過濾器
├── interceptor/   # 攔截器
└── utils/         # 工具類
```

### 編碼規範
1. **日誌格式**: 使用 ELK 友好的結構化格式 (參考 `LOGGING_STANDARDS.md`)
2. **異常處理**: 統一使用 `GlobalExceptionHandler` 處理
3. **API 設計**: RESTful 風格，統一使用 `ApiResponse<T>` 包裝
4. **事務管理**: Service 層使用 `@Transactional` 註解
5. **組件註解**: 統一使用 `@Component`，控制器使用 `@RestController`

### 測試
```bash
# 運行所有測試
./mvnw test

# 運行特定測試
./mvnw test -Dtest=ECommerceIntegrationTest
```

## 部署指南

### Docker 部署
```bash
# 構建鏡像
docker build -t bark-bijou-api .

# 運行容器
docker run -d \
  -p 8080:8080 \
  --link postgres:postgres \
  --link redis:redis \
  bark-bijou-api
```

### 生產環境配置
```properties
# application-prod.properties
spring.profiles.active=prod
logging.level.root=WARN
logging.level.com.smallnine.apiserver=INFO

# 數據庫連接池
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
```

## 監控與日誌

### 日誌配置
- **開發環境**: 控制台彩色輸出 + 文件記錄
- **生產環境**: JSON 格式輸出，適配 ELK Stack
- **日誌位置**: `logs/api-server.log`

### 健康檢查
```bash
# 應用健康狀態
curl http://localhost:8080/actuator/health

# 應用信息
curl http://localhost:8080/actuator/info
```

## 故障排除

### 常見問題
1. **數據庫連接失敗**: 確認 PostgreSQL 服務已啟動，檢查連接參數
2. **Redis 連接異常**: 確認 Redis 服務狀態，檢查端口配置
3. **JWT Token 過期**: 使用 Refresh Token 刷新 Access Token
4. **CORS 錯誤**: 檢查前端域名是否在允許列表中

### 日誌查詢
```bash
# 查看應用日誌
tail -f logs/api-server.log

# 過濾特定操作
grep "action=login" logs/api-server.log

# 查看錯誤日誌
grep "ERROR" logs/api-server.log
```

## 相關文檔
- [JWT 刷新機制前端整合](./REFRESH_TOKEN.md)
- [ELK 友好日誌規範](./LOGGING_STANDARDS.md)
- [API 詳細文檔](http://localhost:8080/swagger-ui.html)

## 聯絡信息
- **開發團隊**: SmallNine Team
- **項目維護**: [GitHub Issues](./issues)
- **技術支持**: 請參考 API 文檔和日誌進行故障排除