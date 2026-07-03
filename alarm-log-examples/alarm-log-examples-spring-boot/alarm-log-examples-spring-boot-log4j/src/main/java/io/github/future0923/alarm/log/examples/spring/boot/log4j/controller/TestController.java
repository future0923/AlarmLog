package io.github.future0923.alarm.log.examples.spring.boot.log4j.controller;

import io.github.future0923.alarm.log.aspect.Alarm;
import io.github.future0923.alarm.log.examples.spring.boot.log4j.exception.TestAspectException;
import io.github.future0923.alarm.log.examples.spring.boot.log4j.exception.TestExtendsException;
import io.github.future0923.alarm.log.examples.spring.boot.log4j.exception.TestExtendsRuntimeException;
import io.github.future0923.alarm.log.examples.spring.boot.log4j.exception.TestImplException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author weilai
 */
@RestController
public class TestController {

    private static Logger logger = LogManager.getLogger(TestController.class);

    @GetMapping("/test1")
    public void test1() {
        logger.error("test1", new TestImplException());
    }

    @GetMapping("/test2")
    public void test2() throws TestExtendsException {
        logger.error("test2", new TestExtendsException());
    }

    @GetMapping("/test3")
    public void test3() {
        logger.error("test3", new TestExtendsRuntimeException());
    }

    @GetMapping("/test4")
    @Alarm(includeException = TestAspectException.class, includeExceptionExtend = false)
    public void test4() {
        logger.error("test4", new TestAspectException());
    }
}