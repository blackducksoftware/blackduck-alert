import React from 'react';
import SystemMessage from '../SystemMessage';
import { renderComponent } from '../../../../../../test/renderer';
import { screen } from '@testing-library/react';

describe('Testing SystemMessage', () => {
    test('Rendering SystemMessage - all data rendered', async () => {
        renderComponent(<SystemMessage
            createdAt="2022-06-09T18:00:00.109-04"
            content="Black Duck configuration is invalid. Black Duck configurations missing."
            severity="WARNING"
            id="systemMessageId"
        />);

        expect(screen.getByText("2022-06-09T18:00:00.109-04")).toBeInTheDocument();
        expect(screen.getByText("Black Duck configuration is invalid. Black Duck configurations missing.")).toBeInTheDocument();
    });
});
