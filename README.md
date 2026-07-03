# Alarm Log

异常日志监控和告警发送组件。它可以从日志事件或 `@Alarm` 标注的方法/类中识别指定异常，并通过邮件、钉钉群机器人或企业微信发送告警。

- 支持 Java 8+、Spring Boot、Spring MVC
- 支持 Logback、Log4j 1.x、Log4j2

## 快速开始

Spring Boot 项目推荐直接使用 starter。

```xml
<dependency>
    <groupId>io.github.future0923</groupId>
    <artifactId>alarm-log-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

配置需要告警的异常和至少一个通知通道：

```yaml
spring:
  alarm-log:
    exception:
      include:
        classes:
          - java.lang.Exception
        extend: true
      exclude:
        classes:
          - java.io.FileNotFoundException
        extend: false
    warn:
      dingtalk:
        enabled: true
        token: your-dingtalk-token
        secret: your-dingtalk-secret
```

在日志配置中加入 `AlarmLog` Appender。以 Logback 为例：

```xml
<configuration>
    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="AlarmLog" class="io.github.future0923.alarm.log.core.enhance.logback.AlarmLogLogbackAsyncAppender">
        <appender-ref ref="Console"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="Console"/>
        <appender-ref ref="AlarmLog" level="ERROR"/>
    </root>
</configuration>
```

之后代码中正常打印异常日志即可触发告警：

```java
log.error("create order failed", exception);
```

## 告警触发规则

一个异常先经过排除规则，再经过包含规则。命中 `exclude` 时不告警；未命中 `exclude` 且满足以下任一条件时会触发告警：

1. 异常类在 `exception.include.classes` 中配置。
2. `exception.include.extend=true`，且异常是 `exception.include.classes` 中任一类的子类。
3. 异常继承 `AlarmLogException` 或 `AlarmLogRuntimeException`。
4. 异常实现 `AlarmLogDoWarnException` 接口。
5. 方法或类标注了 `@Alarm`，且抛出的异常命中注解配置。

### 包含、排除和继承匹配

```yaml
spring:
  alarm-log:
    exception:
      include:
        classes:
          - java.lang.Exception
        extend: true
      exclude:
        classes:
          - java.io.FileNotFoundException
        extend: false
```

`include.extend=false` 时只匹配 `classes` 中配置的异常本身；改为 `true` 后会匹配子类。`exclude.extend` 独立控制排除规则是否匹配子类。排除规则优先级最高，也可以排除标记异常。

### 使用标记异常

```java
public class BizException extends AlarmLogRuntimeException {
    public BizException(String message) {
        super(message);
    }
}
```

如果已有异常类无法继承项目提供的异常基类，可以实现 `AlarmLogDoWarnException`。该接口当前仍需要异常类同时是 `Throwable` 子类。

## Spring Boot 配置

### 基础配置

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `spring.alarm-log.exception.include.classes` | `List<String>` | 空 | 需要触发告警的异常类全限定名 |
| `spring.alarm-log.exception.include.extend` | `Boolean` | `false` | 包含规则是否按继承关系匹配异常 |
| `spring.alarm-log.exception.exclude.classes` | `List<String>` | 空 | 需要排除告警的异常类全限定名 |
| `spring.alarm-log.exception.exclude.extend` | `Boolean` | `false` | 排除规则是否按继承关系匹配异常 |
| `spring.alarm-log.max-retry-times` | `Integer` | `3` | 告警发送失败后的最大重试次数 |
| `spring.alarm-log.retry-sleep-millis` | `Integer` | `1000` | 重试等待基准时间，实际等待会按次数递增 |
| `spring.alarm-log.print-stack-trace` | `Boolean` | `false` | 告警内容是否包含堆栈 |
| `spring.alarm-log.simple-warn-info` | `Boolean` | `false` | 是否使用简化告警内容 |

### 钉钉群机器人

```yaml
spring:
  alarm-log:
    warn:
      dingtalk:
        enabled: true
        token: your-token
        secret: your-secret
```

### 企业微信应用消息

```yaml
spring:
  alarm-log:
    warn:
      workweixin:
        enabled: true
        to: WeiLai,user2
        application-id: 1000002
        corpid: your-corpid
        corpsecret: your-corpsecret
```

### 邮件

```yaml
spring:
  alarm-log:
    warn:
      mail:
        enabled: true
        smtp-host: smtp.example.com
        smtp-port: 465
        to: user1@example.com,user2@example.com
        from: alarm@example.com
        username: alarm@example.com
        password: your-password
        ssl: true
        debug: false
```

多个通知通道可以同时开启。

## 日志框架配置

`includeException`、`includeExceptionExtend`、`excludeException` 和 `excludeExceptionExtend` 既可以写在 Spring 全局配置中，也可以写在 Appender 上。Appender 上的配置会写入全局 `AlarmLogContext`，因此同一进程内会影响所有后续告警判断。

多数 Spring Boot 项目只需要在 `application.yml` 中配置异常匹配，Appender 中保留最小配置即可。

### Logback

```xml
<appender name="AlarmLog" class="io.github.future0923.alarm.log.core.enhance.logback.AlarmLogLogbackAsyncAppender">
    <includeException>java.lang.Exception,java.lang.RuntimeException</includeException>
    <includeExceptionExtend>true</includeExceptionExtend>
    <excludeException>java.io.FileNotFoundException</excludeException>
    <excludeExceptionExtend>false</excludeExceptionExtend>
    <includeCallerData>true</includeCallerData>
    <appender-ref ref="Console"/>
</appender>
```

### Log4j 1.x

```xml
<appender name="AlarmLog" class="io.github.future0923.alarm.log.core.enhance.log4j.AlarmLogLog4jAsyncAppender">
    <param name="includeException" value="java.lang.Exception,java.lang.RuntimeException"/>
    <param name="includeExceptionExtend" value="true"/>
    <param name="excludeException" value="java.io.FileNotFoundException"/>
    <param name="excludeExceptionExtend" value="false"/>
    <appender-ref ref="Console"/>
</appender>
```

### Log4j2

Log4j2 的标签名必须是 `AlarmLog`。

```xml
<AlarmLog name="AlarmLog"
          includeException="java.lang.Exception,java.lang.RuntimeException"
          includeExceptionExtend="true"
          excludeException="java.io.FileNotFoundException"
          excludeExceptionExtend="false"/>
```

为了避免告警 Appender 自身日志造成递归调用，建议只把 `AlarmLog` 挂在 `ERROR` 级别：

```xml
<root level="INFO">
    <appender-ref ref="Console"/>
    <appender-ref ref="AlarmLog" level="ERROR"/>
</root>
```

完整同步/异步配置可以参考 `alarm-log-examples` 下对应日志框架的示例模块。

## 使用 `@Alarm`

Spring Boot starter 已包含 `alarm-log-aspect`。Spring MVC 或手动集成时需要额外引入：

```xml
<dependency>
    <groupId>io.github.future0923</groupId>
    <artifactId>alarm-log-aspect</artifactId>
    <version>${latest.version}</version>
</dependency>
```

`@Alarm` 可以标在类或方法上。标在类上时，对当前类的方法生效；标在方法上时，只对当前方法生效。

```java
@RestController
@Alarm(includeException = Exception.class, includeExceptionExtend = true)
public class TestController {

    @GetMapping("/test1")
    public void test1() {
        throw new IllegalStateException("test1 failed");
    }

    @GetMapping("/test2")
    @Alarm(includeException = TestAspectException.class, includeExceptionExtend = false)
    public void test2() throws TestAspectException {
        throw new TestAspectException();
    }

    @GetMapping("/test3")
    @Alarm(
        includeException = Exception.class,
        includeExceptionExtend = true,
        excludeException = IllegalArgumentException.class,
        excludeExceptionExtend = true
    )
    public void test3() {
        throw new NumberFormatException("ignored");
    }
}
```

说明：

- `test1` 会触发告警，因为 `IllegalStateException` 是 `Exception` 的子类。
- `test2` 只在抛出 `TestAspectException` 本身时触发告警。
- `test3` 不会触发告警，因为 `NumberFormatException` 是 `IllegalArgumentException` 的子类，命中了排除规则。
- `@Alarm` 捕获异常后仍会重新抛出原始异常，不会改变业务异常传播。

## Spring MVC 接入

Spring MVC 项目至少需要引入核心模块和需要的通知模块。

```xml
<dependency>
    <groupId>io.github.future0923</groupId>
    <artifactId>alarm-log-core</artifactId>
    <version>${latest.version}</version>
</dependency>
<dependency>
    <groupId>io.github.future0923</groupId>
    <artifactId>alarm-log-warn-dingtalk</artifactId>
    <version>${latest.version}</version>
</dependency>
```

在 Spring XML 中初始化异常匹配和通知服务：

```xml
<bean id="alarmLogConfigContext" class="io.github.future0923.alarm.log.common.context.AlarmLogContext">
    <property name="includeExceptionExtend" value="true"/>
    <property name="includeExceptionList">
        <list>
            <value>java.lang.Exception</value>
        </list>
    </property>
    <property name="excludeExceptionExtend" value="false"/>
    <property name="excludeExceptionList">
        <list>
            <value>java.io.FileNotFoundException</value>
        </list>
    </property>
    <property name="maxRetryTimes" value="3"/>
    <property name="retrySleepMillis" value="1000"/>
</bean>

<bean id="dingtalkWarnService" class="io.github.future0923.alarm.log.warn.dingtalk.DingtalkWarnService">
    <constructor-arg index="0" value="${alarmLog.warn.dingtalk.token}"/>
    <constructor-arg index="1" value="${alarmLog.warn.dingtalk.secret}"/>
</bean>

<bean id="alarmLogWarnServiceFactory" class="io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory">
    <constructor-arg>
        <list>
            <ref bean="dingtalkWarnService"/>
        </list>
    </constructor-arg>
</bean>
```

如果要使用 `@Alarm`，还需要扫描切面包并开启 AOP：

```xml
<context:component-scan base-package="io.github.future0923.alarm.log"/>
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

## 自定义告警内容

实现 `AlarmMessageContext` 可以完全自定义告警内容；继承 `DefaultAlarmMessageContext` 可以只覆盖某个通道。

Spring Boot 中把实现类声明为 Bean 即可：

```java
@Component
public class CustomAlarmMessageContext extends DefaultAlarmMessageContext {

    @Override
    public String dingtalkContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return "告警：" + context.getThrowableName() + " - " + context.getMessage();
    }
}
```

Spring MVC 或非 Spring 环境可以手动注入：

```java
AlarmLogContext.setAlarmMessageContext(new CustomAlarmMessageContext());
```

## 手动打印并触发告警

如果希望通过工具类主动打印日志并发送告警，可以使用 `AlarmLogHelper`：

```java
AlarmLogHelper.getPrintLogInstance(true).error("create order failed", exception);
```

`getPrintLogInstance()` 默认只打印日志；`getPrintLogInstance(true)` 会同时执行告警判断和发送。

## 示例工程

- Spring Boot 示例：`alarm-log-examples/alarm-log-examples-spring-boot`
- Spring MVC 示例：`alarm-log-examples/alarm-log-examples-spring-mvc`
- 每个示例下分别提供 Logback、Log4j 1.x、Log4j2 的配置文件。
