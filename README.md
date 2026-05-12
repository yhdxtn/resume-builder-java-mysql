# ResumeForge 简历生成网站

一个基于 **Java 17 + Spring Boot + Thymeleaf + MySQL** 的在线简历生成系统。

## 功能

- 用户注册、登录、退出
- 登录后才能编辑简历
- 简历信息保存到 MySQL
- 10 个默认简历模板
- 每个简历模块都可以“勾选显示 / 取消显示”，例如证书、荣誉、项目、技能等
- 基本联系方式也可以选择是否显示，例如手机、邮箱、城市、GitHub / 个人主页
- 头像上传、预览、删除；头像会自动进入简历预览和 PDF
- 管理员后台新增、编辑、启用/停用模板
- 在线预览简历
- 一键下载 PDF
- 模板支持占位符替换，便于后期定期更新模板

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL 5.7+/8.0
- OpenHTMLToPDF

## 默认账号

管理员：

```text
用户名：admin
密码：admin123
```

普通用户：访问 `/register` 自行注册。

## 数据库初始化

先在 MySQL 中创建数据库：

```sql
CREATE DATABASE resume_builder DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

默认连接配置在 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resume_builder?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
app.upload.dir=uploads
```

也可以用环境变量覆盖：

Windows：

```bash
set DB_USER=root
set DB_PASS=root
set APP_UPLOAD_DIR=D:\resume-builder-uploads
```

Linux / macOS：

```bash
export DB_USER=root
export DB_PASS=root
export APP_UPLOAD_DIR=/opt/resume-builder/uploads
```

## 运行

```bash
mvn spring-boot:run
```

浏览器访问：

```text
http://localhost:8080
```

## 打包

```bash
mvn clean package -DskipTests
java -jar target/resume-builder-1.0.0.jar
```

## 模板占位符

后台模板 HTML 可使用以下新版占位符：

```text
{{accentColor}}
{{fullName}}
{{jobTitle}}
{{contactLine}}
{{contactDot}}
{{contactStack}}
{{avatarBlock}}
{{summarySection}}
{{educationSection}}
{{experienceSection}}
{{projectsSection}}
{{skillsSection}}
{{certificatesSection}}
{{awardsSection}}
```

其中：

- `{{avatarBlock}}` 会根据用户是否上传头像、是否勾选显示头像自动输出头像 HTML。
- `{{contactLine}}` 会自动拼接已勾选的联系方式，适合一行展示。
- `{{contactStack}}` 会用换行展示联系方式，适合侧栏模板。
- `{{summarySection}}` 等模块会根据用户是否勾选显示自动输出或隐藏。

系统仍兼容旧占位符：

```text
{{phone}}
{{email}}
{{location}}
{{website}}
{{summary}}
{{education}}
{{experience}}
{{projects}}
{{skills}}
{{certificates}}
{{awards}}
```

## 头像上传说明

头像默认保存到项目运行目录下的 `uploads/avatars/`。生产环境建议用 `APP_UPLOAD_DIR` 指定固定目录，避免重新部署后文件丢失。

## PDF 中文字体说明

项目不会打包字体文件。生成 PDF 时会自动尝试读取系统中文字体：

- Windows：微软雅黑 / 宋体
- macOS：PingFang
- Linux：Noto Sans CJK

如果 PDF 中文显示为方块，请安装中文字体，或在 `PdfService.java` 里增加你本机字体路径。

## 页面路径

- 首页：`/`
- 登录：`/login`
- 注册：`/register`
- 控制台：`/dashboard`
- 编辑简历：`/resume/edit`
- 预览简历：`/resume/preview`
- 下载 PDF：`/resume/download`
- 后台模板管理：`/admin/templates`
