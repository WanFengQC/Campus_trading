# Campus Trading

基于 Spring Boot 3 的校园二手交易平台（B/S 单体架构）。

## 技术栈

- 后端：Spring Boot 3、Spring MVC、MyBatis-Plus、MySQL 8、Spring Security、Thymeleaf、Knife4j
- 前端页面：Thymeleaf 模板 + Bootstrap 5
- 文件：本地磁盘存储（头像/商品图片）

## 目录结构

- `backend/`：核心应用代码（接口、页面、业务模块）
- `docs/`：架构与开发说明
- `frontend/`：历史 Vue 原型（不作为当前主运行入口）

## 本地启动

1. 安装 JDK 17、Maven 3.9+、MySQL 8。
2. 创建数据库并执行：`backend/src/main/resources/db/schema.sql`。
3. 复制 `backend/src/main/resources/application-local.example.yml` 为 `application-local.yml` 并修改数据库账号密码。
4. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

5. 浏览器访问：
   - 首页：[http://127.0.0.1:8080/](http://127.0.0.1:8080/)
   - 登录页：[http://127.0.0.1:8080/login](http://127.0.0.1:8080/login)
   - 注册页：[http://127.0.0.1:8080/register](http://127.0.0.1:8080/register)
   - 个人中心：[http://127.0.0.1:8080/user/center](http://127.0.0.1:8080/user/center)

## 当前已完成

- 学号/工号注册与登录
- 个人中心资料维护（昵称）
- 头像上传与本地静态访问
- 商品模块第一版（发布、列表、详情、编辑、下架、筛选）
- Bootstrap 响应式页面骨架
