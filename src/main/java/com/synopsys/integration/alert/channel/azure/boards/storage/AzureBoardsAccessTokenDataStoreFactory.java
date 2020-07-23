package com.synopsys.integration.alert.channel.azure.boards.storage;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.api.client.util.store.DataStoreFactory;

@Component
public class AzureBoardsAccessTokenDataStoreFactory implements DataStoreFactory {
    @Override
    public AzureBoardsAccessTokenDataStore getDataStore(String id) throws IOException {
        // FIXME implement
        return null;
    }

}
