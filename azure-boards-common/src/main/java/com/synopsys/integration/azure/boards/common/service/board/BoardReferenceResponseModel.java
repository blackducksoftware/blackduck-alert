package com.synopsys.integration.azure.boards.common.service.board;

public class BoardReferenceResponseModel {
    private String id;
    private String name;
    private String url;

    public BoardReferenceResponseModel() {
        // For serialization
    }

    public BoardReferenceResponseModel(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

}
