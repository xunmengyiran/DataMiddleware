# Notice1
maven依赖Oracle的Jar包不能自动加载到maven库，需要手动执行导入命令：
1.将Oracle_lib目录下的ojdbc6.jar拷贝到IntelliJ IDEA的maven插件目录下(C:\Program Files\JetBrains\IntelliJ IDEA 2017.2\plugins\maven\lib\maven3\bin)
2.在该目录下执行命令行：mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3.0 -Dpackaging=jar -Dfile=ojdbc6.jar
# Notice2
工程可以打包成war包，步骤如下：
1.修改pom文件，将package打包方式从jar方式修改为war方式，放开pom.xml中的注释：spring-boot-starter-tomcat配置
2.在IntelliJ中的maven插件中执行clean和package命令，即可在target目录下生成war包
# Notice3
在环境中运行war包命令如下：
nohup java -jar DataMiddleware-0.0.1-SNAPSHOT.war &
# Notice4
MySQL调整max_allowed_packet的大小:
环境中的MySQL传输报文默认限制不满足要求，需要修改其配置文件中的max_allowed_packet = 500M
参考：https://blog.csdn.net/qq_27292113/article/details/74784309