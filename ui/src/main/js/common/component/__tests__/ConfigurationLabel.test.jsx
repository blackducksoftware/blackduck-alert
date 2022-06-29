import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfigurationLabel from '../ConfigurationLabel';

describe('Testing ConfigurationLabel', () => {
    it('Rendering ConfigurationLabel - all data rendered', async () => {
        const { container } = render(<ConfigurationLabel
            configurationName="Test Config Name"
            description="Test Configuration Description"
            lastUpdated="2022-06-03 12:50 (UTC)"
        />);

        expect(container).toHaveTextContent("Test Config Name");
        expect(container).toHaveTextContent("Test Configuration Description");
        expect(container).toHaveTextContent("2022-06-03 12:50 (UTC)");
    });
});
