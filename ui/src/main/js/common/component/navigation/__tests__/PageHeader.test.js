import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import PageHeader from '../PageHeader';

// TODO: Implement this icon library in a much more dynamic way - remove this when done
import registerFaIcons from '../../../../icons';

registerFaIcons();

describe('Testing PageHeader rendering', () => {
    const title = 'Page Title';
    const description = 'Page Description';
    const icon = 'cog';
    const lastUpdated = '2022-04-21 14:55 (UTC)';

    test('default render all props present', () => {
        const {queryByText} = render(
            <PageHeader title={title} description={description} icon={icon} lastUpdated={lastUpdated} />,
        );

        expect(queryByText(title)).toBeInTheDocument();
        expect(queryByText(description)).toBeInTheDocument();
        // TODO: Add testing for font-awesome icons
        // expect(queryByText(icon)).toBeInTheDocument();
        expect(queryByText(lastUpdated)).toBeInTheDocument();
    });
});
