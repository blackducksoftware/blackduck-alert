package com.synopsys.integration.azure.boards.common.service.board;

public class BoardRowModel {
    private String id;
    private String name;

    public BoardRowModel() {
        // For serialization
    }

    public BoardRowModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
