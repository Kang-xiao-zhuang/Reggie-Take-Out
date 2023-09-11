# 瑞吉外卖

<p align="center">
   <a target="_blank" >
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/JDK-1.8+-green.svg"/>
      <img src="https://img.shields.io/badge/SpringBoot-2.7.6.RELEASE-red"/>
      <img src="https://img.shields.io/badge/Vue-2.5.17-red"/>
      <img src="https://img.shields.io/badge/MySQL-5.7-green"/>
      <br/>
      <img src="https://img.shields.io/badge/Mybatis--Plus-3.4.2-red"/>
      <img src="https://img.shields.io/badge/druid-1.2.23-green"/>
      <img src="https://img.shields.io/badge/fastjson-1.2.76-green"/>
      <img src="https://img.shields.io/badge/knife4j-3.0.2-green"/>
   </a>
</p>

## 后台登录开发

**服务端结果返回结果类**

```java
@Data
public class R<T> implements Serializable {

    //编码：1成功，0和其它数字为失败
    private Integer code;

    //错误信息
    private String msg;

    //数据
    private T data;

    //动态数据
    private Map<String,Object> map = new HashMap<>();

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
```

### 员工登录

```java
/**
     * 员工登录
     *
     * @param employee Employee
     * @param request  HttpServletRequest
     * @return Employee
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute(EMPLOYEE, emp.getId());
        return R.success(emp);

    }
```

### 员工退出

```java
/**
     * 员工退出
     *
     * @param request HttpServletRequest
     * @return String
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清楚Session中保存的当前登录员工的id
        request.getSession().removeAttribute(EMPLOYEE);
        return R.success("退出成功");
    }
```

## 员工操作

### 新增员工

```java
/**
     * 新增员工
     *
     * @param employee Employee
     * @return String
     */
    @PostMapping
    public R<String> add(@RequestBody Employee employee, HttpServletRequest request) {
        log.info("新增员工，员工信息：{}", employee);
        // 设置初始密码123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获取当前用户id
        Long empId = (Long) request.getSession().getAttribute(EMPLOYEE);

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }
```

### 员工信息分页查询

```java
/**
     * 员工信息分页查询
     *
     * @param page     int
     * @param pageSize int
     * @param name     String
     * @return Page
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
```

### 根据id修改员工信息

```java
/**
     * 根据id修改员工信息
     *
     * @param employee Employee
     * @param request  HttpServletRequest
     * @return String
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request) {
        log.info(employee.toString());

        Long empId = (Long) request.getSession().getAttribute(EMPLOYEE);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }
```

### 根据id查询员工信息

```java
/**
     * 根据id查询员工信息
     *
     * @param id Long
     * @return Employee
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);

        return employee == null ? R.error("没有查询到对应员工信息") : R.success(employee);
    }
```

测试

![在这里插入图片描述](https://img-blog.csdnimg.cn/7d915ba9c9ef4171926990e2b8f9f9aa.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/caaa6bb6fc7845c1a898288e9cc57a4e.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/323c20d276814b048db472c5d8b11d14.png)

### 前端拦截器开发

```java
/**
 * description: LoginCheckFilter
 * date: 2022/12/7 14:37
 * author: Zhuang
 * version: 1.0
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String EMPLOYEE = "employee";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html
        log.info("拦截到请求：{}", requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login",// 移动端登录
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1、判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute(EMPLOYEE) != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute(EMPLOYEE));

            Long empId = (Long) request.getSession().getAttribute(EMPLOYEE);
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }


    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls       String[]
     * @param requestURI String
     * @return boolean
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/48bdc0e3035b45c194532df6a28d7585.png)

### 配置消息转换器

```java
/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

```java
@Slf4j
@Configuration
@EnableSwagger2
@EnableKnife4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 设置静态资源映射
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静态资源映射...");

        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展mvc框架的消息转换器
     *
     * @param converters HttpMessageConverter
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter);
    }
}
```

### 全局异常处理

```java
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     *
     * @return String
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }
}
```

## 套餐分类操作

### 新增分类

```java
/**
     * 新增分类
     *
     * @param category Category
     * @return String
     */
    @PostMapping
    public R<String> add(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/f0dbcadb49304178b5893b73076bc86f.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/6170a0c6a54941649b6b4c1b30f6829e.png)

### 分页查询

```java
/**
     * 分页查询
     *
     * @param page     int
     * @param pageSize int
     * @return Page
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        //分页查询
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/6fbd35b65228472490145689bfd2674e.png)

### 根据id删除分类



**删除之前要做校验**

```java
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param ids Long
     */
    @Override
    public void remove(Long ids) {
        //添加查询条件，根据分类id进行查询菜品数据
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //如果已经关联，抛出一个业务异常
        if (count1 > 0) {
            throw new CustomException("当前分类下关联了菜品，不能删除");//已经关联菜品，抛出一个业务异常
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");//已经关联套餐，抛出一个业务异常
        }

        //正常删除分类
        super.removeById(ids);
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/b414c324434749f8b4e90704faf0db72.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/baafbb6c8f7940519fbbaa137ad88833.png)

###  根据id修改分类信息

```java
/**
     * 根据id修改分类信息
     *
     * @param category Category
     * @return String
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);

        categoryService.updateById(category);

        return R.success("修改分类信息成功");
    }
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/18bd553dc94447c697603da86c14d04e.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/14903217944f4cda87f1b09b000fcc85.png)

### 根据条件查询分类数据

```java
/**
     * 根据条件查询分类数据
     *
     * @param category Category
     * @return List<Category>
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }
```

## 文件上传下载

```java
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
	@Value("${reggie.path}")
    private String basePath;
}
```

### 文件上传

```java
/**
     * 文件上传
     *
     * @param file MultipartFile
     * @return String
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        assert originalFilename != null;
        // 后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        // dfsdfdfd.jpg
        String fileName = UUID.randomUUID() + suffix;

        // 创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if (!dir.exists()) {
            //目录不存在，需要创建
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }
```

### 文件下载

```java
/**
     * 文件下载
     *
     * @param name     String
     * @param response HttpServletResponse
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
```

## 菜品操作

### 新增菜品

```java
public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
```

封装前端传数据的DTO

```java
@EqualsAndHashCode(callSuper = true)
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
```
**新增菜品，同时保存对应的口味数据**

```java
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto DishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //菜品id
        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }
}    
```

**根据id查询菜品信息和对应的口味信息**

```java
    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id Long
     * @return DishDto
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        // 对象拷贝
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }
```

**更新菜品信息，同时更新对应的口味信息**

```java
/**
     * 更新菜品信息，同时更新对应的口味信息
     *
     * @param dishDto DishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
```

**新增菜品**

```java
	/**
     * 新增菜品
     *
     * @param dishDto DishDto
     * @return String
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        String key = DISH_ + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
```

**菜品信息分页查询**

```java
	/**
     * 菜品信息分页查询
     *
     * @param page     int
     * @param pageSize int
     * @param name     String
     * @return Page
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map(item -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
```

**根据id查询菜品信息和对应的口味信息**

```java
    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id Long
     * @return DishDto
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }
```

### **修改菜品**

```java
	/**
     * 修改菜品
     *
     * @param dishDto DishDto
     * @return String
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        String key = DISH_ + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }
```

### **删除菜品**

```java
    /**
 * 删除菜品
 *
 * @param ids List<Long>
 * @return String
 */
@DeleteMapping
public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(Dish::getId, ids);
        dishService.remove(wrapper);

        return R.success("套餐数据删除成功");
        }
```

## 项目部署启动

部署reids和mysql，更改`application.yml`的配置文件。

启动项目里的jar包即可！

### 1、在瑞吉外卖项目中，后端API设计和开发需要考虑哪些问题？

在瑞吉外卖项目中，后端API的设计和开发需要考虑以下几个问题：

1. 功能需求：首先需要明确外卖项目的功能需求，包括用户注册、登录、浏览菜单、下单、支付、订单管理等功能。这些需求将指导后端API的设计和实现。

2. 数据模型设计：根据功能需求，设计合适的数据模型来存储用户信息、菜单信息、订单信息等数据。考虑到外卖项目通常需要处理大量订单，对数据模型的设计要充分考虑性能和扩展性。

3. API设计：确定API的端点（endpoints）和操作（operations）。每个端点对应一个特定的功能，例如获取菜单信息、创建订单等。定义清晰的API接口，并采用一致的命名和参数约定，以便于前端开发人员的使用和理解。

4. 认证和授权：外卖项目通常需要身份认证和授权机制，以确保只有经过验证的用户可以访问敏感数据和执行特定操作。设计适当的认证和授权机制，例如使用JSON Web Token（JWT）进行用户认证，并为不同的用户角色分配不同的权限。

5. 安全性考虑：在API设计和开发中，要考虑数据的安全性。使用加密协议（例如HTTPS）传输敏感数据，防止信息泄露。对用户输入进行有效的验证和过滤，以防止常见的安全漏洞，如SQL注入和跨站脚本攻击（XSS）。

6. 性能和可扩展性：外卖项目可能会面临大量的并发请求，因此后端API需要具备高性能和可扩展性。优化数据库查询、使用缓存技术、进行水平扩展等方法可以提高API的性能和可伸缩性。

7. 错误处理和日志记录：良好的错误处理和日志记录是后端API设计的重要组成部分。定义清晰的错误码和错误信息，并提供有意义的错误响应，方便前端开发人员进行故障排除和错误处理。同时，记录关键操作和异常情况的日志，有助于监控和故障排查。

8. 文档和测试：编写详细的API文档，描述每个端点的使用方法、参数和返回结果等信息。同时，进行充分的单元测试和集成测试，确保API的功能和性能符合预期，并及早发现和修复潜在的问题。

综上所述，后端API设计和开发需要考虑功能需求、数据模型设计、API设计、认证和授权、安全性、性能和可扩展性、错误处理和日志记录，以及文档和测试等方面。此外，还有一些其他值得考虑的问题：

9. 输入验证和数据处理：在处理用户输入时，进行有效的验证和数据处理是至关重要的。确保输入的数据符合预期的格式和范围，以防止潜在的错误和安全漏洞。处理异常情况和边界条件，并提供适当的错误消息或反馈，使系统更加健壮和用户友好。

10. 性能优化：针对常见的性能瓶颈进行优化是后端API设计的重要任务。可以通过合理的数据库索引设计、查询优化、缓存策略、异步任务处理等方式来提高系统的响应速度和吞吐量。

11. 扩展性和可维护性：考虑项目的未来发展和需求变化，设计具有良好扩展性和可维护性的后端API架构。使用模块化和分层设计原则，将功能模块解耦，使得项目可以更容易地添加新功能或进行修改和维护。

12. 监控和日志：建立适当的监控系统，用于实时监测API的性能指标、错误率和异常情况。记录和分析日志信息，包括请求日志、错误日志和系统事件日志，以便及时发现和解决潜在的问题，并进行系统性能分析和故障排查。

13. API版本控制：当外卖项目不断迭代和更新时，可能会引入对API接口的更改。为了保持与旧版本的兼容性，并确保客户端的平稳过渡，考虑采用API版本控制机制，例如在URL中包含版本号或使用HTTP请求头中的版本信息。

14. 第三方集成：外卖项目可能需要与其他系统或服务进行集成，例如支付网关、短信通知服务、地图服务等。在后端API设计中，要考虑如何与这些第三方服务进行交互和集成，确保数据的安全性和一致性。

15. 扩展API文档：除了基本的API文档外，考虑为开发人员提供更丰富的文档和资源，包括示例代码、SDK（软件开发工具包）、API授权机制等，以便于他们更轻松地使用和集成后端API。

总体而言，后端API设计和开发需要综合考虑功能需求、数据模型设计、API设计、安全性、性能优化、错误处理、文档和测试等方面，以构建高效、安全、可扩展和易于维护的外卖项目后端系统。

### 2、对于瑞吉外卖项目的数据模型设计，可以考虑以下几个核心实体和它们之间的关系：

1. 用户(User)：表示注册和使用外卖服务的用户信息。用户实体可以包含属性如用户ID、用户名、密码（哈希加密后存储）、电子邮件、电话号码等。此外，还可以考虑用户地址(Address)作为用户的关联实体，包含属性如街道、城市、州/省份、邮政编码等。

2. 菜品(Item)：表示瑞吉外卖提供的菜品信息。菜品实体可以包含属性如菜品ID、名称、描述、价格、图片URL等。

3. 菜单(Menu)：表示瑞吉外卖的菜单，包含不同种类的菜品。菜单实体可以包含属性如菜单ID、名称、描述等。菜单与菜品之间可以建立关联关系，以表示一个菜单包含多个菜品。

4. 订单(Order)：表示用户下的订单信息。订单实体可以包含属性如订单ID、用户ID、下单时间、总价、状态等。订单与菜品之间是多对多的关系，一个订单可以包含多个菜品，而一个菜品也可以出现在多个订单中。因此，需要引入一个关联表（OrderItem）来存储订单与菜品之间的关系。

5. 支付(Payment)：表示用户的支付信息。支付实体可以包含属性如支付ID、订单ID、支付时间、支付方式、支付金额等。支付与订单之间是一对一的关系，每个订单只对应一个支付信息。

通过以上的数据模型设计，可以实现用户的注册和登录功能，浏览菜单和下单功能，以及订单管理和支付功能。在实际的开发过程中，可以根据具体需求和业务场景进行进一步的调整和扩展。此外，还可以考虑添加其他实体和关系，如配送员(Delivery Person)、评论(Review)等，以满足更多的业务需求。

### 3、在瑞吉外卖开发过程中遇到的最大挑战是什么以及如何解决的，以下回答作为参考：

在瑞吉外卖的开发过程中，遇到的最大挑战是处理高并发请求和保证系统的性能和可扩展性。当用户量增加并且同时有多个用户在下单和浏览菜单时，后端系统需要能够快速响应并处理大量的请求，同时保持良好的用户体验。

为了解决这个挑战，我们采取了以下几个措施：

1. 基础架构优化：对系统的基础架构进行优化，包括选择高性能的服务器和数据库，合理分配和配置服务器资源，以提高系统的整体性能。

2. 缓存机制：引入缓存机制来减轻数据库的负载。对于频繁读取的数据，例如菜单信息，可以将其缓存在内存中，减少对数据库的查询次数，提高响应速度。

3. 异步任务处理：使用异步任务队列，将一些耗时的操作，如订单的支付和配送任务，放入任务队列中进行处理，以避免阻塞主线程并提高并发处理能力。

4. 水平扩展：根据实际需求，采用水平扩展的方式增加服务器数量，通过负载均衡来分担请求的压力。这可以通过使用容器化技术和自动化部署工具来实现，以快速扩展和管理系统的实例。

5. 性能测试和优化：进行系统的性能测试，识别瓶颈和性能瓶颈，并进行相应的优化。可以使用压力测试工具模拟大量并发请求，并监控系统的响应时间、资源利用率和吞吐量，以发现并解决性能瓶颈问题。

6. 监控和日志：建立监控系统来实时监测系统的性能指标、错误率和异常情况。通过日志记录关键操作和异常情况，可以及时发现问题并进行故障排查。

通过采取以上措施，我们能够成功地应对高并发请求的挑战，并保证瑞吉外卖系统的性能和可扩展性。当然，解决这个挑战也需要团队的合作和不断的迭代优化，以适应不断增长的用户需求和业务发展。

### 4、在瑞吉外卖项目中进行性能测试和优化，可以按照以下步骤进行：

1. 设定性能测试目标：明确性能测试的目标，例如确定系统需要支持的并发用户数、响应时间的要求等。这些目标将作为评估系统性能的标准。

2. 创建测试场景：根据实际使用情况和预期的负载模式，创建符合实际场景的性能测试场景。考虑用户的行为、请求类型、并发用户数等因素，并设计相应的测试用例。

3. 选择性能测试工具：选择适合的性能测试工具，常见的工具包括Apache JMeter、LoadRunner、Gatling等。根据项目需求和团队的熟悉程度选择合适的工具。

4. 配置测试环境：建立一个与生产环境相似的测试环境，包括服务器、网络设置和数据库等。确保测试环境与实际生产环境尽可能一致，以获得准确的性能测试结果。

5. 执行性能测试：使用选定的性能测试工具配置和运行性能测试。模拟实际负载情况，逐步增加并发用户数和请求量，记录系统的响应时间、吞吐量和错误率等指标。

6. 分析性能测试结果：对性能测试结果进行详细分析，识别潜在的性能瓶颈和问题。关注响应时间较长的接口、高负载下的系统资源使用情况、数据库查询性能等方面。

7. 优化性能瓶颈：根据性能测试结果，采取相应的优化措施来解决性能瓶颈。可能的优化策略包括数据库查询优化、缓存的使用、代码逻辑优化、并发处理的优化等。

8. 重复测试和迭代优化：对优化后的系统进行再次性能测试，验证优化效果，并持续监测和调整系统的性能。进行迭代优化，根据需求和实际情况进行改进和调整。

9. 监控和日志记录：建立监控系统，实时监测系统的性能指标、错误率和异常情况。记录关键操作和异常情况的日志，以便及时发现问题并进行故障排查。

通过以上步骤，可以评估系统的性能，发现和解决潜在的性能瓶颈，并持续优化瑞吉外卖项目的性能。重要的是持续关注系统性能，以满足用户需求并提供良好的用户体验。

### 5、要实现前后端分离，在瑞吉外卖项目中，可以采用以下步骤：

1. 定义前后端职责：明确前端和后端各自的职责和任务。前端负责用户界面设计和交互逻辑，后端负责处理业务逻辑、数据存储和与前端的数据交互。

2. 设计API接口：定义前后端之间的API接口，规定数据的格式、请求方法和参数等。可以使用RESTful API设计原则，使前后端之间的通信更加规范和易于理解。

3. 前端开发：前端开发团队根据API接口的定义，使用适当的前端框架（如React、Angular、Vue.js等）进行界面设计和开发。前端团队与后端团队密切合作，确保前端界面能够正确地与后端API进行交互。

4. 后端开发：后端开发团队根据前端的需求和API接口的定义，负责业务逻辑的实现和数据存储。后端开发团队可以选择合适的后端技术框架（如Django、Spring Boot、Express.js等），实现API接口的具体逻辑。

5. 接口对接和调试：前后端开发完成后，进行接口对接和调试。前端开发团队使用模拟数据或者假数据来模拟后端的响应，确保前后端的接口能够正确地进行数据交换和通信。

6. 独立部署：前后端开发完成后，可以将前端代码和后端代码分别部署到不同的服务器或者服务端。前端代码可以部署到Web服务器或者CDN上，后端代码可以部署到应用服务器或者云平台上。

7. 跨域处理：由于前后端分离时前端和后端运行在不同的域上，可能会涉及跨域请求的问题。需要在后端进行跨域处理，允许前端的跨域请求。

8. 安全性考虑：在前后端分离的架构中，需要特别注意数据的安全性。采取适当的安全措施，如使用HTTPS协议进行数据传输、使用身份验证和授权机制保护API接口等，以确保系统的安全性。

通过以上步骤，瑞吉外卖项目可以成功实现前后端分离，提高开发效率和系统的可扩展性。前后端团队可以并行开发，各自专注于自己的领域，提供更好的用户体验和灵活的系统架构。

### 6、如果数据库每天的数据增量为5万条，需要使用三年，以下是一些实施方案来确保数据库的正常运行：

1. 容量规划：评估数据库的容量需求，并确保数据库服务器的存储空间能够满足未来三年的数据增长。根据数据增长速度和存储需求，考虑增加硬盘容量或扩展存储解决方案。

2. 数据库分区：根据数据增长的趋势和查询模式，考虑将数据库进行分区。可以按照时间范围、地理位置或其他逻辑规则划分数据，以提高查询性能和管理效率。分区可以基于数据库本身的分区功能或应用层逻辑实现。

3. 索引优化：设计和优化数据库的索引以支持快速查询和数据检索。评估查询模式和频率，并相应地创建索引以提高查询性能。定期审查和优化索引，确保索引的有效性和适应性。

4. 查询性能调优：监控数据库查询的性能，并进行必要的调优。分析慢查询，识别瓶颈，优化查询语句和查询计划，以提高查询效率和响应时间。

5. 定期维护和优化：执行定期维护任务，例如数据库备份、日志清理和数据库统计信息更新。这样可以确保数据库的稳定性和性能，并减少潜在问题的风险。

6. 负载均衡和集群部署：考虑在需要时引入数据库负载均衡和集群部署。通过将负载分布到多个数据库节点上，可以提高系统的可扩展性和容错性。

7. 监控和警报：建立数据库性能监控系统，实时监测关键指标如存储空间利用率、CPU 和内存使用率、查询响应时间等。设置合适的阈值并配置警报，以及时发现潜在的问题并进行调整。

8. 定期评估和规划：定期评估数据库的性能和容量需求，并进行规划。根据业务增长和数据趋势，进行必要的扩展和升级，以满足未来三年的需求。

这些实施方案将有助于确保数据库的正常运行，并满足长期数据增长的需求。重要的是持续监控和优化数据库，并根据实际情况进行调整和改进。

### 7、在Java项目开发过程中，以下情况可能导致OOM（内存溢出）错误：

1. 内存泄漏：当对象不再使用时，没有及时释放对应的内存资源，导致内存不断积累，最终耗尽可用内存空间。

2. 频繁创建大量对象：如果应用程序频繁创建大量的对象，而没有及时释放这些对象，会导致内存使用量不断增加，最终导致内存溢出。

3. 静态集合类引起的内存泄漏：如果使用静态集合类（如静态List、Map等）来保存大量对象，并且没有适时地清理或移除这些对象，会导致内存泄漏。

4. 递归调用导致的栈溢出：当递归调用的深度过大时，会导致栈空间耗尽，进而导致栈溢出错误。

解决OOM错误的方法如下：

1. 内存泄漏排查：使用内存分析工具（如VisualVM、Eclipse Memory Analyzer等）来检测和分析内存泄漏问题。通过查看对象的引用链和内存占用情况，找出引起内存泄漏的根本原因，并进行相应的修复。

2. 优化对象的创建和销毁：减少不必要的对象创建和使用，及时释放不再使用的对象。使用对象池或缓存来管理对象的创建和重用，减少对象频繁创建和销毁带来的内存开销。

3. 增加堆内存：通过增加JVM的堆内存限制，提高可用内存空间。可以通过调整JVM的启动参数中的-Xms（初始堆大小）和-Xmx（最大堆大小）来设置堆内存大小。

4. 使用合适的集合和数据结构：根据实际需求选择合适的集合和数据结构，避免使用静态集合类保存大量对象。及时清理和移除不再需要的对象，防止内存泄漏。

5. 优化递归算法：检查和优化递归算法，确保递归调用的深度合理且不会导致栈溢出。可以考虑使用迭代或尾递归等替代递归的方式来避免栈溢出问题。

6. 监控和调优：使用性能监控工具对应用程序进行实时监控，查看内存使用情况和垃圾回收行为。根据监控结果进行相应的优化和调整，如调整垃圾回收器参数、调整内存分配策略等。