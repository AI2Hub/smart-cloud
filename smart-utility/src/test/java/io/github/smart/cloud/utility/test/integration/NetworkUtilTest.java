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
package io.github.smart.cloud.utility.test.integration;

import io.github.smart.cloud.utility.NetworkUtil;
import io.github.smart.cloud.utility.test.integration.prepare.TestApplication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class NetworkUtilTest {

    @Test
    void testTelnet() {
        Assertions.assertThat(NetworkUtil.isOk("localhost", 40007, 3000, 3)).isTrue();
        Assertions.assertThat(NetworkUtil.isOk("localhost", 8081, 500, 3)).isFalse();
    }

    @Test
    void testGetLocalIpAddress() {
        String localIpAddress = NetworkUtil.getLocalIpAddress();
        Assertions.assertThat(localIpAddress).isNotBlank();
        Assertions.assertThat(localIpAddress).isNotEqualTo("127.0.0.1");
    }

}