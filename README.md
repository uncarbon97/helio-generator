# helio-generator

## 项目说明
基于[renren-generator](https://gitee.com/renrenio/renren-generator) 改造适配的代码生成器，可一键生成单体or微服务版的前、后端代码，减少无谓的重复劳动

## 如何使用

1. 从GitHub或Gitee克隆项目源码，到自己的电脑上
2. 找到`resources/application.yml`，修改里面的数据库`连接地址、账号、密码`等
3. 找到`resources/generator.properties`，修改里面的`包名、模块名、作者`等
4. 找到`src/main/java/io/renren/GeneratorApplication.java`，运行项目
5. 浏览器访问http://127.0.0.1:6688 ，就能看到代码生成器页面了

## License
[GPL-3.0](./LICENSE)

## 演示效果图
![](.readme_static/helio-generator-1.JPG)