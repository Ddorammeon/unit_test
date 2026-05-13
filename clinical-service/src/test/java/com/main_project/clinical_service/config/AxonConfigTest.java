package com.main_project.clinical_service.config;

import com.thoughtworks.xstream.XStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AxonConfigTest {

    @Test
    @DisplayName("CLN-SRV-UT-049 - configureXStream should allow common wildcard types")
    void configureXStreamShouldAllowCommonWildcardTypes() {
        XStream xStream = mock(XStream.class);

        new AxonConfig().configureXStream(xStream);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(xStream).allowTypesByWildcard(captor.capture());
        org.junit.jupiter.api.Assertions.assertArrayEquals(new String[]{"com.do_an.common.**", "com.main_project.**"}, captor.getValue());
    }

    @Test
    @DisplayName("CLN-SRV-UT-050 - configureXStream should configure project wildcard types once")
    void configureXStreamShouldConfigureProjectWildcardTypesOnce() {
        XStream xStream = mock(XStream.class);

        new AxonConfig().configureXStream(xStream);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(xStream).allowTypesByWildcard(captor.capture());
        org.junit.jupiter.api.Assertions.assertArrayEquals(new String[]{"com.do_an.common.**", "com.main_project.**"}, captor.getValue());
    }
}
