import React from 'react';
import PageHeader from '../PageHeader';
import { renderComponent } from '../../../../../../../test/renderer';
import { screen } from '@testing-library/react';

describe('Testing PageHeader rendering', () => {
    const title = 'Page Title';
    const description = 'Page Description';
    const icon = 'cog';
    const lastUpdated = '2022-04-21 14:55 (UTC)';

    test('default render all props present', () => {
        renderComponent(<PageHeader title={title} description={description} icon={icon} lastUpdated={lastUpdated} />);

        expect(screen.getByText(title)).toBeInTheDocument();
        expect(screen.getByText(description)).toBeInTheDocument();
        expect(screen.getByText(lastUpdated)).toBeInTheDocument();
    });
});
