import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import Checkbox  from 'common/component/input/Checkbox';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import { useSelector } from 'react-redux';

const useStyles = createUseStyles({
    createRoleBtn: {
        background: 'none',
        width: '100%',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        }
    },
    activeCreateBtn: {
        border: ['solid', '.5px', '#2E3B4E'],
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:hover': {
            backgroundColor: '#37475e'
        }
    },
    mutedCreateBtn: {
        border: 'none',
        backgroundColor: '#bdbdbd',
        color: '#2E3B4E',
        '&:hover': {
            backgroundColor: '#b0b0b0'
        }
    },
    deleteUserBtn: {
        background: 'none',
        color: 'inherit',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '6px',
        fontSize: '14px',
        backgroundColor: '#E03C31',
        color: 'white',
        '&:focus': {
            outline: 0
        },
        '& > *': {
            marginRight: '5px'
        },
        '&:disabled': {
            border: ['1px', 'solid', '#D9D9D9'],
            backgroundColor: '#D9D9D9',
            color: '#666666',
            cursor: 'not-allowed'
        }
    },
    permissionActionContainer: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-start',
        width: '100%',
        backgroundColor: '#EDEDED',
        borderRadius: '5px',
        marginBottom: '10px',
    },
    permissionOptions: {
        display: 'flex',
        justifyContent: 'flex-end',
        paddingRight: '95px',
        columnGap: '35px',
        marginBottom: '5px'
    }
});

const PermissionTableActions = ({ handleValidatePermission }) => {
    const classes = useStyles();
    const CONTEXT = 'context';
    const DESCRIPTOR = 'descriptorName';
    const [showCreatePermission, setShowCreatePermission] = useState(false);
    const [newPermission, setNewPermission] = useState({'uploadDelete': undefined, 'uploadWrite': undefined, 'uploadRead': undefined});
    const [fieldErrors, setFieldErrors] = useState({});

    const descriptors = useSelector(state => state.descriptors.items);

    function getBtnStyle(muted) {
        return classNames(classes.createRoleBtn, {
            [classes.activeCreateBtn]: !muted,
            [classes.mutedCreateBtn]: muted,
        });
    }

    function createDescriptorOptions(descriptors) {
        const descriptorOptions = [];
        const nameCache = [];

        descriptors.forEach((descriptor) => {
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

    function createContextOptions(descriptors, selectedPermission) {
        const availableContexts = [];
        if (selectedPermission.descriptorName) {
            descriptors.forEach((descriptor) => {
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
    function handleCreateUserClick() {
        setShowCreatePermission(!showCreatePermission);
    }

    function handleCheckboxChange(e) {
        const { checked, name } = e;
        setNewPermission(permission => ({...permission, [name]: checked}));
    }

    function handleDropdownChange(e) {
        const { value, name } = e.target;
        setNewPermission(permission => ({...permission, [name]: value[0]}));
    }

    function handleDescriptorChange(e) {
        const { value, name } = e.target;
        const selectedValue = descriptors.find(descriptor => {return descriptor.label === value[0]});
        setNewPermission(permission => ({...permission, [name]: selectedValue.name, label: value[0]}));
    }

    function handleSave(newPermission) {
        if(!newPermission[DESCRIPTOR] && !newPermission[CONTEXT]) {
            setFieldErrors({
                'descriptorName': { 
                    'fieldMessage': 'Descriptor is required',
                    'fieldName': 'descriptorName',
                    'severity': 'ERROR'
                },
                'context': { 
                    'fieldMessage': 'Context is required',
                    'fieldName': 'context',
                    'severity': 'ERROR'
                }
            });
        } else if (!newPermission[DESCRIPTOR]) {
            setFieldErrors({
                'descriptorName': { 
                    'fieldMessage': 'Descriptor is required',
                    'fieldName': 'descriptorName',
                    'severity': 'ERROR'
                }
            });
        } else if (!newPermission[CONTEXT]) {
            setFieldErrors({
                'context': { 
                    'fieldMessage': 'Context is required',
                    'fieldName': 'context',
                    'severity': 'ERROR'
                }
            });
        } else {
            setFieldErrors({});
            handleValidatePermission(newPermission);
        }
    }


    return (
        <div className={classes.permissionActionContainer}>
            <button className={getBtnStyle(showCreatePermission)} onClick={handleCreateUserClick}>
                {showCreatePermission ? (
                    <>
                        <FontAwesomeIcon icon="minus" />
                        Cancel Create Permission
                    </>
                ) : (
                    <>
                        <FontAwesomeIcon icon="plus" />
                        Create Permission
                    </> 
                )}                
            </button>

            { showCreatePermission ? (
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
                    <button className={getBtnStyle(false)} onClick={() => handleSave(newPermission)}>
                        <FontAwesomeIcon icon="plus" />
                        Add Permission
                    </button>
                </div>
            ) : null }
        </div>

    );
};

export default PermissionTableActions;