import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import Adapter from 'enzyme-adapter-react-15';
import GeneralButton from 'common/field/GeneralButton';

beforeAll(() => {
    Enzyme.configure({ adapter: new Adapter() });
});

test('Rendering default test button snapshot', () => {
    const button = renderer.create(<GeneralButton onClick={() => {
    }}
    />);
    const tree = button.toJSON();
    expect(tree).toMatchSnapshot();
});

test('GeneralButton click handler', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<GeneralButton onClick={mockCallBack} />));

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(2);
});

test('GeneralButton alt label', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<GeneralButton onClick={mockCallBack}>Test Config</GeneralButton>));

    expect(button.text()).toEqual('Test Config');
    console.log(button.text());
});
