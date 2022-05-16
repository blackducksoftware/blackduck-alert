import React from 'react';
import { fireEvent, screen, render } from '@testing-library/react';
import '@testing-library/jest-dom';
import Enzyme from 'enzyme';
import renderer from 'react-test-renderer';
import Adapter from 'enzyme-adapter-react-15';
import CollapsiblePane from '../CollapsiblePane';

// TODO: Implement this icon library in a much more dynamic way - remove this when done
import registerFaIcons from '../../../icons';
registerFaIcons();

beforeAll(() => {
    Enzyme.configure({ adapter: new Adapter() });
});

const defaultChildren = ['Child 1', 'Child 2'];

describe('Testing CollapsiblePane', () => {
    it('Rendering default CollapsiblePane snapshot', () => {
        const component = renderer.create(<CollapsiblePane
            id={'collapsiblePaneId'}
            title={'collapsiblePane Configuration'}
            children={defaultChildren}
        />);
        const tree = component.toJSON();
        expect(tree).toMatchSnapshot();
    });

    it('Rendering CollapsiblePane toggle collapse', async () => {
        const { container } = render(<CollapsiblePane
            id={'collapsiblePaneId'}
            title={'collapsiblePane Configuration'}
            children={defaultChildren}
        />);

        // Locate toggle button
        const toggleBtn = screen.getByRole('button');
        expect(toggleBtn).toHaveTextContent('collapsiblePane Configuration');

        // Locate toggle icon
        const toggleIcon = container.querySelector("[data-icon='plus']");
        expect(toggleIcon).toHaveClass('fa-plus');

        // Fire user event clicking the toggle button AND expect the icon to change accordingly
        fireEvent.click(toggleBtn);
        await container.querySelector("[data-icon='minus']");
        expect(container.querySelector("[data-icon='minus']")).toHaveClass('fa-minus');
    });
});
