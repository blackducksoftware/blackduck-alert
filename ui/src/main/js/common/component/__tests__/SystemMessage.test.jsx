import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import SystemMessage from '../SystemMessage';

describe('Testing SystemMessage', () => {
    it('Rendering SystemMessage - all data rendered', async () => {
        const { container } = render(<SystemMessage
            createdAt="Test Config Name"
            content="Black Duck configuration is invalid. Black Duck configurations missing."
            severity="WARNING"
            id="systemMessageId"
        />);

        expect(container).toHaveTextContent("2022-06-09T18:00:00.109-04");
        expect(container).toHaveTextContent("Black Duck configuration is invalid. Black Duck configurations missing.");
        expect(container).toHaveTextContent("WARNING");
        expect(container).toHaveTextContent("systemMessageId");
    });
});
