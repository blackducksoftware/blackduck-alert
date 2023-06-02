import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Checkbox from 'common/component/input/Checkbox';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import Button from 'common/component/button/Button';

const useStyles = createUseStyles({
    permissionActionContainer: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-start',
        width: '100%',
        backgroundColor: '#EDEDED',
        borderRadius: '5px',
        marginBottom: '10px',
        paddingTop: '25px'
    },
    permissionOptions: {
        display: 'flex',
        justifyContent: 'flex-end',
        paddingRight: '95px',
        columnGap: '35px',
        marginBottom: '5px'
    },
    permissionFormActions: {
        display: 'flex',
        justifyContent: 'flex-end',
        padding: [0, '8px', '8px']
    }
});

const PermissionTableActions = ({ handleValidatePermission }) => {
    const classes = useStyles();
    const CONTEXT = 'context';
    const DESCRIPTOR = 'descriptorName';
    const [newPermission, setNewPermission] = useState({});
    const [fieldErrors, setFieldErrors] = useState({});
    const isDisabled = Object.values(newPermission).length === 0;
    const descriptors = useSelector((state) => state.descriptors.items);

    function createDescriptorOptions(options) {
        const descriptorOptions = [];
        const nameCache = [];

        options.forEach((descriptor) => {
            const { label } = descriptor;
            if (!nameCache.includes(label)) {
                nameCache.push(label);
                descriptorOptions.push({
                    label,
                    value: label
                });
            }
        });

        return descriptorOptions;
    }

    function createContextOptions(descriptorOptions, selectedPermission) {
        const availableContexts = [];
        if (selectedPermission.descriptorName) {
            descriptorOptions.forEach((descriptor) => {
                if (descriptor.name === selectedPermission.descriptorName) {
                    availableContexts.push(descriptor.context);
                }
            });
        }

        return availableContexts.map((context) => ({
            label: context,
            value: context
        }));
    }

    function handleCheckboxChange(e) {
        const { checked, name } = e;
        setNewPermission((permission) => ({ ...permission, [name]: checked }));
    }

    function handleDropdownChange(e) {
        const { value, name } = e.target;
        setNewPermission((permission) => ({ ...permission, [name]: value[0] }));
    }

    function handleDescriptorChange(e) {
        const { value, name } = e.target;
        const selectedValue = descriptors.find((descriptor) => descriptor.label === value[0]);
        setNewPermission((permission) => ({ ...permission, [name]: selectedValue.name, label: value[0] }));
    }

    function handleSave(permission) {
        if (!permission[DESCRIPTOR] && !permission[CONTEXT]) {
            setFieldErrors({
                descriptorName: {
                    fieldMessage: 'Descriptor is required',
                    fieldName: 'descriptorName',
                    severity: 'ERROR'
                },
                context: {
                    fieldMessage: 'Context is required',
                    fieldName: 'context',
                    severity: 'ERROR'
                }
            });
        } else if (!permission[DESCRIPTOR]) {
            setFieldErrors({
                descriptorName: {
                    fieldMessage: 'Descriptor is required',
                    fieldName: 'descriptorName',
                    severity: 'ERROR'
                }
            });
        } else if (!permission[CONTEXT]) {
            setFieldErrors({
                context: {
                    fieldMessage: 'Context is required',
                    fieldName: 'context',
                    severity: 'ERROR'
                }
            });
        } else {
            setFieldErrors({});
            handleValidatePermission(permission);
            setNewPermission({});
        }
    }

    return (
        <div className={classes.permissionActionContainer}>
            <div className={classes.createPermissionContainer}>
                <DynamicSelectInput
                    name={DESCRIPTOR}
                    id={DESCRIPTOR}
                    label="Descriptor Name"
                    options={createDescriptorOptions(descriptors)}
                    clearable={false}
                    onChange={handleDescriptorChange}
                    value={[newPermission.label]}
                    errorName={DESCRIPTOR}
                    errorValue={fieldErrors[DESCRIPTOR]}
                />
                <DynamicSelectInput
                    name={CONTEXT}
                    id={CONTEXT}
                    label="Context"
                    options={createContextOptions(descriptors, newPermission)}
                    clearable={false}
                    onChange={handleDropdownChange}
                    value={[newPermission.context]}
                    errorName={CONTEXT}
                    errorValue={fieldErrors[CONTEXT]}
                />
                <div className={classes.permissionOptions}>
                    <Checkbox
                        name="create"
                        id="create"
                        label="Create"
                        onChange={handleCheckboxChange}
                        isChecked={newPermission.create}
                    />
                    <Checkbox
                        name="delete"
                        id="delete"
                        label="Delete"
                        onChange={handleCheckboxChange}
                        isChecked={newPermission.delete}
                    />
                    <Checkbox
                        name="read"
                        id="read"
                        label="Read"
                        onChange={handleCheckboxChange}
                        isChecked={newPermission.read}
                        description="Testing Description"
                    />
                    <Checkbox
                        name="write"
                        id="write"
                        label="Write"
                        onChange={handleCheckboxChange}
                        isChecked={newPermission.write}
                    />
                    <Checkbox
                        name="execute"
                        id="execute"
                        label="Execute"
                        onChange={handleCheckboxChange}
                        isChecked={newPermission.execute}
                    />
                </div>
                <div className={classes.permissionFormActions}>
                    <Button
                        onClick={() => handleSave(newPermission)}
                        type="submit"
                        icon="plus"
                        text="Add Permission"
                        style="default"
                        isDisabled={isDisabled}
                        title={isDisabled ? 'Configure input above to add permission' : 'Add permission'}
                    />
                </div>
            </div>
        </div>
    );
};

PermissionTableActions.propTypes = {
    handleValidatePermission: PropTypes.func
};

export default PermissionTableActions;
