package com.campus.trading.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Campus Trading API")
            .version("v1")
            .description("校园二手交易平台接口文档，覆盖认证、商品、订单、租赁、捐赠、评价、举报、公告、聊天与推荐模块。"));
    }
}
