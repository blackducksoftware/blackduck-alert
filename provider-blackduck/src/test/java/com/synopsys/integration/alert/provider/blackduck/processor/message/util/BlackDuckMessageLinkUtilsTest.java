package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckMessageLinkUtilsTest {
    public static final String EXAMPLE_BLACKDUCK_PROJECT_VERSION_URL = "https://a-blackduck-server/api/projects/73962982-3b6f-441f-b3b7-1dc78d14611a/versions/77a39f9d-8538-4216-84f4-514964536e41";

    @Test
    public void createProjectVersionComponentsLinkTest() throws IntegrationException {
        String expectUrl = EXAMPLE_BLACKDUCK_PROJECT_VERSION_URL + BlackDuckMessageLinkUtils.URI_PIECE_COMPONENTS;
        String inputUrl = expectUrl + "/bb9a56d3-8a48-43fd-8db1-5a7529b857f0/versions/9b36b6b4-8221-4071-8685-8c567d68e90e/licenses/7cae335f-1193-421e-92f1-8802b4243e93";
        HttpUrl inputHttpUrl = new HttpUrl(inputUrl);

        ProjectVersionComponentVersionView bomComponent = Mockito.mock(ProjectVersionComponentVersionView.class);
        Mockito.when(bomComponent.getHref()).thenReturn(inputHttpUrl);

        String projectVersionComponentsLink = BlackDuckMessageLinkUtils.createProjectVersionComponentsLink(bomComponent);
        assertEquals(expectUrl, projectVersionComponentsLink);
    }

    @Test
    public void createComponentQueryLinkTest() throws IntegrationException {
        String componentName = "An Example Component";
        String encodedComponentName = componentName.replace(" ", "%20");

        String componentsUrl = EXAMPLE_BLACKDUCK_PROJECT_VERSION_URL + BlackDuckMessageLinkUtils.URI_PIECE_COMPONENTS;
        String expectedUrl = String.format("%s?q=%s:%s", componentsUrl, BlackDuckMessageLinkUtils.QUERY_PARAM_COMPONENT_NAME, encodedComponentName);
        String inputUrl = componentsUrl + "/bb9a56d3-8a48-43fd-8db1-5a7529b857f0/versions/9b36b6b4-8221-4071-8685-8c567d68e90e/licenses/7cae335f-1193-421e-92f1-8802b4243e93";
        HttpUrl inputHttpUrl = new HttpUrl(inputUrl);

        ProjectVersionComponentVersionView bomComponent = Mockito.mock(ProjectVersionComponentVersionView.class);
        Mockito.when(bomComponent.getComponentName()).thenReturn(componentName);
        Mockito.when(bomComponent.getHref()).thenReturn(inputHttpUrl);

        String componentQueryLink = BlackDuckMessageLinkUtils.createComponentQueryLink(bomComponent);
        assertEquals(expectedUrl, componentQueryLink);
    }

}
