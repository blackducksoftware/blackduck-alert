package com.synopsys.integration.alert.channel.azure.boards.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

// FIXME implement
public class AzureBoardsAccessTokenDataStore implements DataStore<String> {
    @Override
    public DataStoreFactory getDataStoreFactory() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int size() throws IOException {
        return 0;
    }

    @Override
    public boolean isEmpty() throws IOException {
        return false;
    }

    @Override
    public boolean containsKey(String key) throws IOException {
        return false;
    }

    @Override
    public boolean containsValue(String value) throws IOException {
        return false;
    }

    @Override
    public Set<String> keySet() throws IOException {
        return null;
    }

    @Override
    public Collection<String> values() throws IOException {
        return null;
    }

    @Override
    public String get(String key) throws IOException {
        return null;
    }

    @Override
    public DataStore<String> set(String key, String value) throws IOException {
        return null;
    }

    @Override
    public DataStore<String> clear() throws IOException {
        return null;
    }

    @Override
    public DataStore<String> delete(String key) throws IOException {
        return null;
    }

}
