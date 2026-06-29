import React from 'react';
import { render } from '@testing-library/react';
import { ThemeProvider } from 'react-jss';
import { Provider } from 'react-redux';
import theme from '../src/main/js/_theme';

/**
 * This file provides a custom render function that wraps test components with necessary information such as providers and themes.
 * It is used in test files to ensure that components are rendered with the appropriate context for testing.
 */

function wrapComponent(component, store) {
    return (
        <ThemeProvider theme={theme}>
            {store ? (
                <Provider store={store}>
                    {component}
                </Provider>
            ) : component}
        </ThemeProvider>
    );
}

export function renderComponent(component, { store, ...renderOptions } = {}) {
    const result = render(wrapComponent(component, store), renderOptions);
    return {
        ...result,
        rerender: (updatedComponent) => result.rerender(wrapComponent(updatedComponent, store)),
        store,
    };
}