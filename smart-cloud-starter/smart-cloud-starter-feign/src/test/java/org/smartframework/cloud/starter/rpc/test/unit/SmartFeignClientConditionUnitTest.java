package org.smartframework.cloud.starter.rpc.test.unit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smartframework.cloud.starter.core.constants.PackageConfig;
import org.smartframework.cloud.starter.rpc.feign.annotation.SmartFeignClient;
import org.smartframework.cloud.starter.rpc.feign.condition.SmartFeignClientCondition;
import org.smartframework.cloud.starter.rpc.test.SuiteTest;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

public class SmartFeignClientConditionUnitTest {

    @Test
    public void test() {
        PackageConfig.setBasePackages(new String[]{SuiteTest.class.getPackage().getName()});
        SmartFeignClientCondition smartFeignClientCondition = new SmartFeignClientCondition();

        AnnotationMetadata annotationMetadata1 = AnnotationMetadata.introspect(TestFeign1.class);
        boolean result1 = smartFeignClientCondition.matches(null, annotationMetadata1);
        Assertions.assertThat(result1).isTrue();


        AnnotationMetadata annotationMetadata2 = AnnotationMetadata.introspect(TestFeign2.class);
        boolean result2 = smartFeignClientCondition.matches(null, annotationMetadata2);
        Assertions.assertThat(result2).isFalse();
    }

    @SmartFeignClient(url = "test")
    public interface TestFeign1 {
        @GetMapping
        String get();
    }

    @SmartFeignClient(url = "test")
    public interface TestFeign2 {
        @PostMapping
        String get();
    }

    @RestController
    public class TestFeignImpl implements TestFeign2 {

        @Override
        public String get() {
            return null;
        }
    }

}