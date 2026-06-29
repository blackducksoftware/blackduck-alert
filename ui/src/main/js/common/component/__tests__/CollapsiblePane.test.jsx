import React from 'react';
import { renderComponent } from '../../../../../../test/renderer';
import CollapsiblePane from '../CollapsiblePane';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

describe('Testing CollapsiblePane', () => {
    test('Rendering CollapsiblePane toggle collapse', async () => {
        const user = userEvent.setup();

        renderComponent(
            <CollapsiblePane
                id={'collapsiblePaneId'}
                title={'Collapsible Pane Configuration'}
            >
                <div>Test Child</div>
            </CollapsiblePane>
        );

        const childElement = screen.getByText('Test Child');
        // Expect the child element to be initially hidden
        expect(childElement.parentElement).toHaveClass('hidden');

        const toggleBtn = screen.getByRole('button');
        await user.click(toggleBtn);

        // Expect the child element to be shown after clicking the toggle button
        expect(childElement.parentElement).not.toHaveClass('hidden');
    });
});
