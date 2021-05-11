import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import Adapter from 'enzyme-adapter-react-15';
import CancelButton from 'common/field/CancelButton';

beforeAll(() => {
    Enzyme.configure({ adapter: new Adapter() });
});

test('Rendering default cancel button snapshot', () => {
    const button = renderer.create(<CancelButton onClick={() => {
    }}
    />);
    const tree = button.toJSON();
    expect(tree).toMatchSnapshot();
});

test('CancelButton click handler', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<CancelButton onClick={mockCallBack} />));

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(2);
});

test('CancelButton alt label', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<CancelButton onClick={mockCallBack}>Test Cancel</CancelButton>));

    expect(button.text()).toEqual('Test Cancel');
    console.log(button.text());
});
