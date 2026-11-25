package com.tiendajuegos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendajuegos.common.GlobalExceptionHandler;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** Configuración común de MockMvc standalone para los tests de controllers. */
public final class MvcTestSupport {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private MvcTestSupport() {
    }

    public static MockMvc mvc(Object controller) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(MAPPER))
                .build();
    }
}
