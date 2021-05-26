package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProviderMessageHolderTest {
    private static final LinkableItem PROVIDER_ITEM = new LinkableItem("BlackDuck", "test-server01");
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, PROVIDER_ITEM);
    private static final SimpleMessage SIMPLE_MESSAGE = SimpleMessage.original(PROVIDER_DETAILS, "Subject", "Description", List.of());
    private static final ProjectMessage PROJECT_MESSAGE = ProjectMessage.projectStatusInfo(PROVIDER_DETAILS, new LinkableItem("Project", "My Project"), ProjectOperation.CREATE);

    @Test
    public void reducePopulatedTest() {
        ProviderMessageHolder left = new ProviderMessageHolder(List.of(PROJECT_MESSAGE, PROJECT_MESSAGE, PROJECT_MESSAGE), List.of(SIMPLE_MESSAGE));
        ProviderMessageHolder right = new ProviderMessageHolder(List.of(PROJECT_MESSAGE), List.of(SIMPLE_MESSAGE, SIMPLE_MESSAGE));
        reduceAndAssert(left, right);
    }

    @Test
    public void reduceEmptyTest() {
        ProviderMessageHolder left = ProviderMessageHolder.empty();
        ProviderMessageHolder right = ProviderMessageHolder.empty();
        reduceAndAssert(left, right);
    }

    @Test
    public void reduceEmptyLeftTest() {
        ProviderMessageHolder left = ProviderMessageHolder.empty();
        ProviderMessageHolder right = new ProviderMessageHolder(List.of(), List.of(SIMPLE_MESSAGE));
        reduceAndAssert(left, right);
    }

    @Test
    public void reduceEmptyRightTest() {
        ProviderMessageHolder left = new ProviderMessageHolder(List.of(), List.of(SIMPLE_MESSAGE));
        ProviderMessageHolder right = ProviderMessageHolder.empty();
        reduceAndAssert(left, right);
    }

    private void reduceAndAssert(ProviderMessageHolder left, ProviderMessageHolder right) {
        ProviderMessageHolder reduced = ProviderMessageHolder.reduce(left, right);
        assertGetter(left, right, reduced, ProviderMessageHolder::getProjectMessages);
        assertGetter(left, right, reduced, ProviderMessageHolder::getSimpleMessages);
    }

    private void assertGetter(ProviderMessageHolder left, ProviderMessageHolder right, ProviderMessageHolder reduced, Function<ProviderMessageHolder, List<? extends ProviderMessage<?>>> getter) {
        int projectMessageSize = getter.apply(left).size() + getter.apply(right).size();
        assertEquals(projectMessageSize, getter.apply(reduced).size());
    }

}
