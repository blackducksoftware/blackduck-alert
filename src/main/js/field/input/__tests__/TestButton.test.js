import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import Adapter from 'enzyme-adapter-react-15';
import TestButton from '../TestButton';

beforeAll(() => {
    Enzyme.configure({ adapter: new Adapter() });
});

test('Rendering default test button snapshot', () => {
    const button = renderer.create(
        <TestButton onClick={()=> {}} />
    );
    let tree = button.toJSON();
    expect(tree).toMatchSnapshot();
});

test('TestButton click handler', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<TestButton onClick={mockCallBack} />));

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(2);
});

test('TestButton alt label', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<TestButton onClick={mockCallBack}>Test Config</TestButton>));

    expect(button.text()).toEqual('Test Config');
    console.log(button.text());
});
