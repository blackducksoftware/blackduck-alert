import React from 'react';
import Enzyme  from 'enzyme';
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

test('Rendering default collapsible pane snapshot', () => {
    const component = renderer.create(<CollapsiblePane
        id={'collapsiblePaneId'}
        title={'collapsiblePane Configuration'}
        children={defaultChildren}
    />);
    const tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
