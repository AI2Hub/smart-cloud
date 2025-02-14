/*
 * Copyright © 2019 collin (1634753825@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.smart.cloud.starter.rpc.feign.test.unit;

import io.github.smart.cloud.starter.rpc.feign.test.prepare.Application;
import io.github.smart.cloud.starter.rpc.feign.test.prepare.rpc.FeignClientRpc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, args = "--spring.profiles.active=feign", webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FeignClientControllerTest {

    @Autowired
    private FeignClientRpc feignClientRpc;

    @Test
    void testGet() {
        String requestStr = "test feign";
        String response = feignClientRpc.get(requestStr);
        Assertions.assertThat(response).isEqualTo(requestStr);
    }

}