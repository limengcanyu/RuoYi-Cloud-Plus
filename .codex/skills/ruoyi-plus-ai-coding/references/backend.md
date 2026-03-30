# 后端约定

## 优先参考的代码来源

- `ruoyi-modules/ruoyi-gen/src/main/resources/vm/java/*.vm`
- `ruoyi-modules/ruoyi-demo/...`
- `ruoyi-modules/ruoyi-system/...`
- `ruoyi-common/ruoyi-common-mybatis/...`

## 决策顺序

写代码时按下面顺序取样：

1. 当前业务模块下最近似实现。
2. 当前仓库公共能力模块中的统一约定。
3. generator 模板。
4. 通用 Spring / MyBatis-Plus 默认习惯。

如果规则冲突，优先相信当前仓库真实代码。

## 微服务总览

当前仓库的微服务相关真实约定不是 Feign 风格，而是：

- 服务注册与配置中心使用 Nacos。
- 对外入口使用 `ruoyi-gateway`。
- 服务间 RPC 默认使用 Dubbo。
- 跨服务契约放在 `ruoyi-api/*`。
- 跨服务写操作按需使用 Seata，不是所有写接口都默认上全局事务。

写代码前先判断任务属于下面哪类：

1. 纯服务内 CRUD 或模块增强。
2. 需要给其他服务复用能力，必须新增或修改 `ruoyi-api` 契约。
3. 只是网关入口、跨域、过滤、日志、鉴权相关，应该改 `ruoyi-gateway` 或配置。
4. 涉及跨服务写入一致性，才评估 `@GlobalTransactional`。

## 微服务模块边界

- `ruoyi-auth`：认证授权中心，本身也通过 `@DubboReference` 依赖 system/resource 等远程能力。
- `ruoyi-gateway`：统一入口，负责过滤器、异常处理、跨域、日志、语言解析等，不写业务 CRUD。
- `ruoyi-modules/ruoyi-system|resource|workflow|job|gen`：具体微服务业务实现。
- `ruoyi-api/ruoyi-api-system|resource|workflow`：服务间共享契约，放 `Remote*Service`、远程 BO/VO/model、mock/stub。
- `ruoyi-common/*`：跨服务都会复用的基础能力，不要在业务模块里重复造轮子。

## 应用入口与基础配置规则

- 每个微服务应用入口默认使用 `@EnableDubbo` + `@SpringBootApplication`。
- 每个服务的 `application.yml` 只保留本服务端口、服务名、激活环境、Nacos 导入入口。
- 配置导入继续沿用 `spring.config.import` + `optional:nacos:*` 结构。
- 业务服务通常额外导入 `datasource.yml`；像 `ruoyi-auth`、`ruoyi-gateway` 这类服务不一定导入数据源配置，先以当前模块现状为准。
- 不要因为新增一个接口就改动服务名、端口、Nacos group/namespace、配置加载方式。

## 跨服务契约规则

需要被其他服务调用的能力，按下面顺序落代码：

1. 在 `ruoyi-api` 下新增或扩展 `Remote*Service` 接口。
2. 如需跨服务传参或返回展示字段，在 `ruoyi-api` 下补充专用 `domain.bo`、`domain.vo` 或 `model`。
3. 在服务提供方模块的 `dubbo/` 包下新增 `@DubboService` 实现。
4. 在服务消费方用 `@DubboReference` 注入。

### 契约放置原则

- `Remote*Service` 放到能力归属服务对应的 `ruoyi-api-*` 模块。
- 远程 DTO 只放跨服务真正需要的字段，不直接暴露服务内 Entity。
- 不要把 controller 的 BO/VO 直接搬去做远程 DTO，除非该对象本身已经是跨服务语义。
- 远程接口命名延续当前仓库风格：`RemoteUserService`、`RemoteFileService`、`RemoteWorkflowService`。

### provider 实现规则

- provider 一般放在 `src/main/java/.../dubbo/RemoteXxxServiceImpl.java`。
- 类上保留 `@DubboService`，通常同时保留 Spring `@Service` 或使用构造注入风格与当前模块一致。
- provider 内优先复用本服务已有 `service`、`mapper`、`convert`、工具类，不重新实现一套业务。
- provider 返回远程 VO / model，不把本服务内部实体直接外泄。

### consumer 调用规则

- 跨服务调用使用 `@DubboReference`，不要新写服务间 HTTP/Feign 调用。
- 只有真正跨服务时才新增 `@DubboReference`；同服务内调用继续直接注入本地 service。
- controller 中允许少量直接 `@DubboReference`，但仅限当前模块已有这种写法的场景；新逻辑优先看附近代码风格保持一致。
- 消费方如果附近已有 `stub = "true"`、`mock = "true"` 等容错写法，继续沿用。

### mock / stub 规则

- 可选能力或允许降级的远程服务，优先复用 `ruoyi-api` 中现有 `Mock` / `Stub` 风格。
- `Mock` 适合返回空对象、空列表、空字符串等兜底值。
- `Stub` 适合包一层 try/catch 做调用保护和告警日志。
- 不要在业务代码里散落重复的远程降级逻辑，优先收敛到 API 契约侧。

## 分层结构

标准 CRUD 代码应优先遵循下面这套结构：

- `domain/Entity.java`
- `domain/bo/EntityBo.java`
- `domain/vo/EntityVo.java`
- `mapper/EntityMapper.java`
- `service/IEntityService.java`
- `service/impl/EntityServiceImpl.java`
- `controller/EntityController.java`

## Entity 规则

- 除非所在模块明显另有约定，否则实体类继承 `org.dromara.common.mybatis.core.domain.BaseEntity`。
- 使用 Lombok `@Data` 和 `@EqualsAndHashCode(callSuper = true)`。
- 使用 `@TableName("table_name")`。
- 主键使用 `@TableId`。
- 存在 `delFlag` 时保留 `@TableLogic`，存在乐观锁字段时保留 `@Version`。
- 如果附近实体已经使用 `@OrderBy` 等额外注解，应继续保持。

## BO 规则

- 实现 `Serializable`。
- 添加 `@AutoMapper(target = Entity.class, reverseConvertGenerate = false)`。
- 请求专用字段、查询专用字段放在 BO 中，包括 `params`。
- 在生成器或附近代码已有分组校验时，继续使用：`AddGroup`、`EditGroup`、`QueryGroup`。
- `@Xss`、`@Email`、`@Size`、`@NotBlank`、`@NotNull` 要按真实业务语义添加，不要一股脑全套上。
- 查询存在日期范围或扩展条件时，保留 `params = new HashMap<>()`。

## VO 规则

- 实现 `Serializable`。
- 添加 `@AutoMapper(target = Entity.class)`。
- 生成器风格的导出对象通常带 `@ExcelIgnoreUnannotated`。
- `@ExcelProperty`、`@ExcelDictFormat`、`ExcelDictConvert`、`@ExcelRequired`、`@ExcelNotation`、`@DateTimeFormat` 只在导入导出场景下使用。
- 如果附近代码会把 ID 翻译成展示字段，沿用 `@Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")` 这类写法。
- 展示型派生字段放在 VO，不放在 Entity。

## Mapper 规则

- 默认形式是 `interface XxxMapper extends BaseMapperPlus<Xxx, XxxVo>`。
- 不要为简单的 entity 转 vo 手写重复代码，优先依赖 `BaseMapperPlus`。
- 模块已经使用 `@DataPermission` 时，在重写方法和自定义查询上继续保留。
- 复杂模块里 mapper 可能同时继承 `MPJBaseMapper<Entity>` 并使用 `JoinWrappers.lambda(...)`，遇到这种风格要延续，不要换一种写法。
- 只有在 `selectVoList/selectVoPage` 不够用时，才补 XML 或自定义 mapper 方法。

### Mapper 建议结构

标准 mapper 一般按这个顺序组织：

1. 接口声明
2. 默认查询方法
3. 自定义分页或列表方法
4. 特殊数据权限重写
5. 辅助构造方法

### 什么时候需要 XML

- 复杂联表 SQL 无法仅靠 wrapper 清晰表达时。
- 需要手写查询列和结果映射时。
- 项目当前模块已经大量使用 XML 时。

如果 `BaseMapperPlus + wrapper` 已足够，优先不要补 XML。

## Service 规则

- 类声明通常是 `@RequiredArgsConstructor`、`@Service`，按需补 `@Slf4j`。
- mapper 注入字段命名为 `private final XxxMapper baseMapper;`。
- 读操作通常返回 `Vo`、`List<Vo>` 或 `PageResult<Vo>`。
- BO 转实体用 `MapstructUtils.convert(bo, Entity.class)`。
- 查询条件优先用 `LambdaQueryWrapper` 和 `Wrappers.lambdaQuery()`。
- 在 wrapper 条件里直接写 `StringUtils.isNotBlank(...)` 和 null 判断。
- 分页查询优先采用：
  `Page<Vo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);`
  `return PageResult.build(result.getRecords(), result.getTotal());`
- 生成器风格模块保留 `validEntityBeforeSave(...)` 这种扩展点。
- 多表写操作使用 `@Transactional(rollbackFor = Exception.class)`。
- 明确的业务失败，尤其是权限、数据完整性、删除校验，使用 `ServiceException`。
- 不要绕过模块现有的数据权限、角色校验、删除前校验。
- 如果写操作只发生在单服务内，优先本地 `@Transactional`，不要无故提升成分布式事务。
- 如果像 `SysProfileController` 这类场景同时写本服务数据并调用远程文件服务，再参考附近代码决定是否使用 `@GlobalTransactional`。

### Service 建议结构

标准 service impl 一般按下面顺序组织：

1. 查询单条
2. 分页查询
3. 列表查询
4. 构建查询条件
5. 新增
6. 修改
7. 保存前校验
8. 删除前校验与删除
9. 其他扩展业务方法

### 查询逻辑建议

- 单表查询优先使用 `LambdaQueryWrapper`。
- 条件判断直接放在 wrapper 上，不要额外写大量 if 套壳。
- 日期范围统一从 `bo.getParams()` 取 begin/end。
- 复杂联表查询优先查同模块是否已有 MPJ 风格可复用。

### 写入逻辑建议

- BO 转实体统一走 `MapstructUtils.convert`。
- 批量关系维护时优先拆成私有方法，例如角色、岗位、用户关联。
- 修改前优先保留已有防误删、防越权、防并发覆盖逻辑。

## Controller 规则

- 继承 `BaseController`。
- 类上通常带 `@Validated`、`@RestController`、`@RequiredArgsConstructor`、`@RequestMapping`。
- 返回值使用 `R<T>` 或 `R<Void>`。
- 标准 CRUD 接口通常是：`GET /list`、`POST /export`、`GET /{id}`、`POST`、`PUT`、`DELETE /{ids}`。
- `@SaCheckPermission` 权限格式遵循 `${module}:${business}:${action}`。
- 写操作、导入导出接口通常加 `@Log(title = "...", businessType = BusinessType.X)`。
- 附近接口已有防重时，写接口继续使用 `@RepeatSubmit`。
- 适合分组校验时，使用 `@Validated(AddGroup.class)` 和 `@Validated(EditGroup.class)`。
- 特殊接口直接复用模块内现成做法，例如导入导出、`@ApiEncrypt`、multipart 上传、数据权限检查、写入前唯一性校验。
- controller 默认是对前端开放的 HTTP 接口，不要把跨服务契约直接建在 controller 上。
- 新增给其他服务复用的能力时，优先补 `Remote*Service`，不是补一个“内部专用 controller”。

### Controller 建议结构

标准 controller 一般按下面顺序组织：

1. 列表
2. 导出
3. 详情
4. 新增
5. 修改
6. 删除
7. 特殊接口

### Controller 边界

- controller 负责接参、校验、权限、日志、返回值转换。
- 重业务逻辑尽量放 service，不要在 controller 里堆长逻辑。
- 但前置权限检查、唯一性提示、显式业务失败提示可以留在 controller，前提是同模块已有这种习惯。

## 查询与工具规则

- 分页统一使用 `PageQuery` 和 `PageResult`，不要无故引入新的分页 DTO。
- 优先使用项目工具类：`MapstructUtils`、`StringUtils`、`StreamUtils`、`ValidatorUtils`、`SpringUtils`、`RedisUtils`。
- 数组转列表按附近代码习惯使用 `List.of(ids)` 或 `Arrays.asList(ids)`。
- 日期范围查询通常从 `bo.getParams()` 中读取 `beginTime`、`endTime` 或 `beginFieldName`、`endFieldName`。

## 前后端联动规则

- 新增后端接口时，路径和权限前缀尽量保持 generator 约定，方便前端目录和 API 命名同步。
- 新增日期范围查询时，记得保留 `bo.params` 结构，避免前端 `addDateRange` 无法对接。
- 导出接口通常保持 `POST /export` 风格，便于前端直接复用现有下载逻辑。
- 批量删除接口通常使用 `DELETE /{ids}`，便于前端直接传数组或逗号串。
- 前端访问路径保持面向 gateway 的稳定 HTTP 路由，不把 Dubbo 远程接口暴露给前端设计。

## Gateway 规则

- `ruoyi-gateway` 主要承载入口、过滤器、异常处理、跨域、日志、国际化等横切能力。
- 不在 gateway 中新增业务 service、mapper、领域对象 CRUD。
- 如果问题只与请求头、跨域、过滤链、日志、安全入口有关，优先检查 gateway。
- 如果问题属于业务数据查询、写入、校验，优先改对应业务服务，不要误改 gateway。

## Seata 与事务边界规则

- 本地单服务写操作优先 `@Transactional(rollbackFor = Exception.class)`。
- 只有一个业务动作明确跨越多个微服务资源写入时，才考虑 `@GlobalTransactional(rollbackFor = Exception.class)`。
- 不要把纯查询接口、纯远程读取接口或可容忍最终一致的场景都包进全局事务。
- 是否需要 Seata，以当前模块已有实现和业务一致性要求为准，不做想当然扩张。

## 生成器优先模式

从零新增 CRUD 时，优先对齐生成器默认方法集合：

- `queryById`
- `queryPageList`
- `queryList`
- `insertByBo`
- `updateByBo`
- `deleteWithValidByIds`

然后再叠加模块内已有增强，例如：

- 唯一性校验
- 数据权限注解
- MPJ 联表查询
- 缓存注解
- Excel 导入导出监听器
- 关联表维护逻辑

## 什么时候优先看 generator

- 新增一个标准单表 CRUD 时。
- 只有表结构和基本接口需求，没有现成业务模块可参考时。
- 需要快速补齐整套骨架代码时。

## 什么时候优先看现有模块

- 目标模块已经有类似业务。
- 涉及数据权限、联表、缓存、角色岗位关系、导入导出、工作流扩展时。
- 任务是“修改已有模块”而不是“新建模块”时。

## 避免事项

- 不要在 controller 里直接暴露 entity 代替 BO/VO。
- 不要给新的管理接口漏掉权限注解。
- 没有明确必要时，不要从 `BaseMapperPlus` 风格退回手工映射。
- 前端查询页用了日期范围时，不要删掉后端 `params` 相关处理。
- 不要把 `ruoyi-system` 这类复杂逻辑强行简化成生成器式单表 CRUD。
- 不要把当前仓库默认的 Dubbo 远程协作改写成 Feign 风格。
- 不要把 `ruoyi-api` 变成业务实现模块；它只承载契约、远程 DTO、mock/stub。
- 不要为跨服务复用去直接依赖对方 controller 路由。
- 不要在 gateway 里堆业务逻辑。
- 不要无故扩大全局事务边界。

## 交付前自检

交付前至少检查这些点：

- CRUD 主链路是否完整。
- BO / VO / Entity 职责是否清晰。
- 分页、查询、删除校验是否与前端对得上。
- 权限、日志、防重、事务是否遗漏。
- 是否只是 generator 裸产物，如果是，需要继续补齐同模块已有增强。
- 如果改了跨服务能力，`ruoyi-api`、provider、consumer 三处是否同步。
- 如果改了微服务入口或配置，是否仍保持当前 Nacos 导入结构。
- 如果改了事务，是否能说明为什么需要本地事务或全局事务。
