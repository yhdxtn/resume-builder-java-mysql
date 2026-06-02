<img width="1733" height="981" alt="image" src="https://github.com/user-attachments/assets/2272a372-35dc-4505-bf66-d77491f8cb05" /># 云简匠 CareerCraft 简历生成网站

一个基于 **Java 17 + Spring Boot + Thymeleaf + MySQL** 的在线简历生成系统。

## 功能

- 用户注册、登录、退出
- 登录后才能编辑简历
- 简历信息保存到 MySQL
- 19 个默认简历模板，包含参考图片风格新增的 9 个版式
- 首页、控制台、编辑页和后台模板页均显示真实模板缩略预览
- 首页和控制台模板缩略图可点击，点击后直接进入对应模板的编辑页
- 编辑页采用“左侧填写信息 + 右侧实时预览”的工作台布局，输入内容会自动刷新预览
- 每个简历模块都可以“勾选显示 / 取消显示”，例如证书、荣誉、项目、技能等
- 基本联系方式也可以选择是否显示，例如手机、邮箱、城市、GitHub / 个人主页
- 头像上传、预览、删除；新选择头像会立刻进入右侧实时预览，保存后进入正式预览和 PDF
- 已加入云简匠 CareerCraft 小 Logo，网页顶部、浏览器标签页和 PWA 图标都会显示
- 管理员后台新增、编辑、启用/停用模板
- 在线预览简历
- 一键下载 PDF
- 模板支持占位符替换，便于后期定期更新模板

## 页面展示
### 主页
<img width="1733" height="981" alt="image" src="https://github.com/user-attachments/assets/8ff9f44f-1e5c-48d6-92d1-7159fcf1f3f3" />
### 管理员后台
<img width="1733" height="981" alt="image" src="https://github.com/user-attachments/assets/da2bb4a2-74a5-4285-90d5-9cacec4497e7" />
### 模板
<img width="1733" height="981" alt="image" src="https://github.com/user-attachments/assets/22c7a400-b072-436c-956e-14204180b0c0" />

## 默认模板名称

内置模板名称全部改为原创命名：

流光双栏模板、白屿极简模板、玄夜代码模板、靛蓝侧栏模板、青柠校招模板、松石事务模板、星链项目模板、琥珀数据模板、玫瑰卡片模板、墨线正式模板、星岸蓝线模板、夜幕弧光模板、海屿标题模板、青锋时间轴模板、紫境双栏模板、暖阳侧栏模板、云灰通栏模板、松青清雅模板、藏蓝名片模板。

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
- 指定模板编辑：`/resume/edit?templateId=模板ID`
- 预览简历：`/resume/preview`
- 下载 PDF：`/resume/download`
- 后台模板管理：`/admin/templates`


## 模板缩略预览与实时编辑说明

v6 已加入模板点击使用和编辑页实时预览功能。首页、控制台模板卡片可以直接点击进入编辑页；编辑页左侧显示当前简历效果，右侧填写基本信息、头像、经历、证书等内容，输入时会自动刷新预览。

v5 已加入模板缩略预览功能。系统会用一份示例简历内容自动渲染每个模板的真实 HTML 样式，并以缩略图方式展示在首页、控制台、编辑页和后台模板管理页。选择模板时不再只显示文字和颜色，而是可以直接看到版式效果。

## 安卓手机端适配说明

v3 已加入移动端响应式适配，安卓手机浏览器可直接访问同一个地址使用。

- 所有页面增加 `viewport`，手机端不会默认缩放成电脑网页。
- 首页、登录、注册、控制台、简历编辑、后台模板管理均做了单列布局适配。
- 输入框字号调整为 16px，减少 Android Chrome 自动放大输入框的问题。
- 导航栏在小屏幕下支持横向滑动，按钮和输入区域加大，方便触屏点击。
- 简历预览页在手机端使用自适应 iframe，高度按屏幕计算。
- 简历模板预览 HTML 增加 `@media screen`，手机预览时双栏会自动变为单栏；PDF 导出仍保持 A4 版式。
- 头像上传支持安卓相册选择，部分浏览器会同时提供拍照入口。
- 增加 Web App Manifest 和图标，Android Chrome 可通过“添加到主屏幕”以类 App 方式打开。

手机访问方式示例：

1. 电脑和安卓手机连接同一个 Wi-Fi。
2. 在电脑命令行查看局域网 IP，例如 Windows 使用 `ipconfig`。
3. 启动项目后，在手机浏览器访问：`http://电脑局域网IP:8080`。
4. 如果访问不了，检查 Windows 防火墙是否放行 8080 端口。

## v10 新增功能

- 新增【个人主页】：用户可以保存姓名、默认求职岗位、电话、邮箱、城市、个人主页/GitHub、默认头像。
- 填写简历时会优先带入个人主页中的默认信息；新建简历时也会自动使用默认头像。
- 注册用户默认获得 100 积分。
- 管理员后台新增【用户管理】：可查看用户列表、启用/禁用用户、调整用户角色、修改积分、快捷加减积分。
- 默认头像上传支持前端裁切，裁成正方形后再保存，适合证件照/职业头像。
- 控制台显示当前用户积分，并提供个人主页入口。

管理员入口：登录 `admin / admin123` 后，进入控制台，点击“用户管理”。

## v12 更新说明

本版本新增：

1. 后台用户与积分管理页面改为 100% 页面缩放下可完整显示的紧凑横向列表。
2. 首页右侧示意图增加文字说明、步骤标签和功能标签，避免大面积空白。
3. 网站底部增加基本信息栏，展示系统名称、技术栈和核心功能。
4. 控制台新增“草稿箱”和“我的简历”模块：
   - 保存草稿：保存当前编辑到一半的内容，下次可继续编辑。
   - 保存到我的简历：生成一条历史简历记录，可预览、回看和下载 PDF。
5. 新增 `/resume/my` 简历中心页面，集中展示草稿与历史简历。

数据库仍然使用 `spring.jpa.hibernate.ddl-auto=update` 自动新增 `resume_records` 表。


## v13 修复说明
- 编辑页已改为更紧凑的已选模板提示。
- 右侧实时预览放大，A4 页面显示更清楚。
- 增加底部悬浮操作条：保存草稿、保存到我的简历、保存并下载 PDF。
- 修复历史简历下载时 recordId 异常或空 HTML 导致的 Whitelabel 500 问题，并增加 PDF 生成兜底。
