import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';
import renderer from 'react-test-renderer';
import SubmitButton from 'common/field/SubmitButton';

beforeAll(() => {
    Enzyme.configure({ adapter: new Adapter() });
});

test('Rendering default save button snapshot', () => {
    const button = renderer.create(<SubmitButton onClick={() => {
    }}
    />);
    const tree = button.toJSON();
    expect(tree).toMatchSnapshot();
});

test('SaveButton click handler', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<SubmitButton onClick={mockCallBack} />));

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);

    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(2);
});

test('SaveButton alt label', () => {
    const mockCallBack = jest.fn();
    const button = shallow((<SubmitButton onClick={mockCallBack}>Save My Form</SubmitButton>));

    expect(button.text()).toEqual('Save My Form');
    console.log(button.text());
});
