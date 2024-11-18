package com.org.project.test_configs;

import com.org.project.security.OrganizationAuthorizationFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest
@ControllerTest
public abstract class BaseControllerTest {

    @MockBean
    protected OrganizationAuthorizationFilter organizationAuthorizationFilter;
}
