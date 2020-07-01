package com.synopsys.integration.azure.boards.common.service.board;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayWithCountResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

public class AzureBoardService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_TEAM_BOARDS = new AzureSpecTemplate("/{organization}/{project}/{team}/_apis/work/boards");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_TEAM_BOARDS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/{project}/{team}/_apis/work/boards/{boardId}");

    private final AzureHttpService azureHttpService;

    public AzureBoardService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayWithCountResponseModel<BoardReferenceResponseModel> getBoards(String organizationName, String projectIdOrName, String teamIdOrName) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_TEAM_BOARDS
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .defineReplacement("{team}", teamIdOrName)
                                 .populateSpec();
        Type responseType = new TypeToken<AzureArrayWithCountResponseModel<BoardReferenceResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public BoardResponseModel getBoard(String organizationName, String projectIdOrName, String teamIdOrName, String boardIdOrBacklogLevel) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_TEAM_BOARDS_INDIVIDUAL
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .defineReplacement("{team}", teamIdOrName)
                                 .defineReplacement("{boardId}", boardIdOrBacklogLevel)
                                 .populateSpec();
        return azureHttpService.get(requestSpec, BoardResponseModel.class);
    }

}
