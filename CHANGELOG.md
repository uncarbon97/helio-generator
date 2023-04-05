## v1.7.3
### 前端模板变更
- `Api.ts`模板，`REST`访问路径随后端模板变更

### 后端模板变更
- `AdminInsertOrUpdateDTO.java`模板、`AdminListDTO.java`模板、`ServiceImpl.java`模板，制品中动词后置（如：`AdminInsertOrUpdateSysUserDTO`修改为`AdminSysUserInsertOrUpdateDTO`）
- `Mapper.java`模板，顶部`package`，由`dao`修正为`mapper`（实际使用不影响，IDEA会自动修正）
- `ServiceInterface.java`模板，`HelioBaseService`泛型内没有正确引入实体类对象
- `Controller.java`模板，类上注解路由修改，只保留到`/api/v1`
- `Controller.java`模板，方法上注解路由修改，默认不带`${moduleName}`模块名占位符，且`${className}`类名占位符由驼峰形式修改为连字符形式
- `getOneById(id, throwIfInvalidId)`抛出异常的错误信息文本改为明文
- `“后台管理-添加”`修改为`“后台管理-新增”`
- `“后台管理-新增/编辑/删除${comments}”`修改为`“后台管理-${comments}-新增/编辑/删除”`
- 数据库`date`类型对应`Java`类型由`LocalDateTime`修改为`LocalDate`

### 生成器变更
- 启动后在控制台增加`Web`访问地址打印，点击即可通过浏览器打开生成器网页


## v1.7.0
### 前端模板变更
- `index.vue`补充注释，格式优化；原先用于权限控制的`show`已失效，替换为`ifShow`，以修复权限不足却依然显示按钮的问题
- 新生成菜单的`parentId`设置成 0
- Controller`@RequestMapping`路由，在`/api/v1`后追加`/${moduleName}`，便于区分业务子模块
- 优化`data.ts`中的时间区间字段，直接使用解构赋值的语法，并只保留日期部分；对应`Api.ts`方法手动补全时间部分(起`00:00:00`，止`23:59:59`)
- 适配“生成分页列表查询条件”选择框
- `Api.ts`中编辑方法，URL 拼串改为模板字符串方式

### 后端模板变更
- 适配“生成分页列表查询条件”选择框
- 删除`model`中的`@ApiModel`注解，其可能导致不会正确渲染入参字段

### 生成器变更
- 增加“生成分页列表查询条件”选择框
