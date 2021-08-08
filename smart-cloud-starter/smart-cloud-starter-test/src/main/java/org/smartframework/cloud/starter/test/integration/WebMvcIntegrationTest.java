package org.smartframework.cloud.starter.test.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.smartframework.cloud.starter.test.vo.FileVO;
import org.smartframework.cloud.utility.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * webmvc集成测试父类
 *
 * @author liyulin
 * @date 2020-09-07
 */
@Slf4j
public class WebMvcIntegrationTest extends AbstractIntegrationTest implements IIntegrationTest {

    @Autowired
    protected WebApplicationContext applicationContext;
    protected MockMvc mockMvc = null;

    @BeforeEach
    public void initMock() {
        // 添加过滤器
        Map<String, Filter> filterMap = applicationContext.getBeansOfType(Filter.class);
        Filter[] filters = new Filter[filterMap.size()];
        int i = 0;
        for (Map.Entry<String, Filter> entry : filterMap.entrySet()) {
            filters[i++] = entry.getValue();
        }

        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).addFilters(filters).build();
    }

    @Override
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * post请求
     *
     * @param url           请求mapping的地址
     * @param req           请求参数
     * @param typeReference 返回对象类型
     * @return
     * @throws Exception
     */
    @Override
    public <T> T post(String url, Object req, TypeReference<T> typeReference) throws Exception {
        String requestBody = JacksonUtil.toJson(req);
        log.info("test.requestBody={}", requestBody);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(url)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE);
        if (requestBody != null) {
            mockHttpServletRequestBuilder.content(requestBody);
        }

        MockHttpServletResponse response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        return deserializeResponse(response.getContentAsByteArray(), typeReference, url);
    }

    /**
     * get请求
     *
     * @param url           请求mapping的地址
     * @param req           请求参数
     * @param typeReference 返回对象类型
     * @return
     * @throws Exception
     */
    @Override
    public <T> T get(String url, Object req, TypeReference<T> typeReference)
            throws Exception {
        String requestJsonStr = (req == null) ? null : JacksonUtil.toJson(req);
        log.info("test.requestBody={}", requestJsonStr);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.isNotBlank(requestJsonStr)) {
            JsonNode jsonNodeElements = JacksonUtil.parse(requestJsonStr);
            Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator = jsonNodeElements.fields();
            while (jsonNodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = jsonNodeIterator.next();
                JsonNode jsonNode = entry.getValue();
                if (jsonNode == null) {
                    continue;
                }
                if (jsonNode.isArray()) {
                    String[] values = new String[jsonNode.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = convert(jsonNode.get(i));
                    }
                    mockHttpServletRequestBuilder.param(entry.getKey(), values);
                } else if (!jsonNode.isNull()) {
                    mockHttpServletRequestBuilder.param(entry.getKey(), convert(jsonNode));
                }
            }
        }

        MockHttpServletResponse response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        return deserializeResponse(response.getContentAsByteArray(), typeReference, url);
    }

    private String convert(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return jsonNode.asText();
    }

    /**
     * get请求，通过body传参
     *
     * @param url           请求mapping的地址
     * @param req           请求参数
     * @param typeReference 返回对象类型
     * @return
     * @throws Exception
     */
    public <T> T getByBody(String url, Object req, TypeReference<T> typeReference)
            throws Exception {
        String requestJsonStr = (req == null) ? null : JacksonUtil.toJson(req);
        log.info("test.requestBody={}", requestJsonStr);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
                .content(requestJsonStr)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE);

        MockHttpServletResponse response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        return deserializeResponse(response.getContentAsByteArray(), typeReference, url);
    }

    public <T> T uploadFiles(String url, MultiValueMap<String, String> params, List<FileVO> files,
                             TypeReference<T> typeReference) throws Exception {
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = MockMvcRequestBuilders
                .multipart(url);
        for (FileVO fileVO : files) {
            File file = fileVO.getFile();
            MockMultipartFile mockMultipartFile = new MockMultipartFile(fileVO.getName(), file.getCanonicalPath(),
                    MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(file));
            mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);
        }
        if (null != params && params.size() > 0) {
            mockMultipartHttpServletRequestBuilder.params(params);
        }

        MockHttpServletResponse response = mockMvc.perform(mockMultipartHttpServletRequestBuilder)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String content = response.getContentAsString();

        log.info("test.result={}", content);

        return JacksonUtil.parseObject(content, typeReference);
    }

    /**
     * 下载文件
     *
     * @param url
     * @param req
     * @param dest
     * @return
     * @throws Exception
     */
    public File download(String url, Object req, String dest) throws Exception {
        String requestJsonStr = JacksonUtil.toJson(req);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(url)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        Map<String, String> requestMap = JacksonUtil.parseObject(requestJsonStr, new TypeReference<Map<String, String>>() {
        });
        if (requestMap != null) {
            requestMap.forEach(mockHttpServletRequestBuilder::param);
        }

        MockHttpServletResponse response = this.mockMvc.perform(mockHttpServletRequestBuilder)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        File destFile = new File(dest);
        FileUtils.writeByteArrayToFile(destFile, response.getContentAsByteArray());
        return destFile;
    }

}