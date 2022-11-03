import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import { fetchGithub, postGithubConfiguration, validateGitHubConfiguration } from 'store/actions/github';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

{/* 
Form Fields:
    Name (required): Github Account name
    Api token (required): API token
    Timeout: How long should we wait for the connection to timeout
*/}

const GithubAddUserModal = ({ isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const [newGitHubUser, setNewGitHubUser] = useState({});

    const fieldErrors = useSelector(state => state.github.error.fieldErrors);
    const inProgress = useSelector(state => state.github.inProgress);
    const saveStatus = useSelector(state => state.github.saveStatus);

    useEffect(() => {
        if ( saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus]);

    function clearInputs() {
        setNewGitHubUser({});
    }

    function handleClose() {
        toggleModal(false);
        clearInputs();
        dispatch(fetchGithub());
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setNewGitHubUser(userData => ({...userData, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(postGithubConfiguration(newGitHubUser));
        handleClose();
    }

    function handleSubmit() {
        dispatch(validateGitHubConfiguration(newGitHubUser));
    }
    
    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Add Github User Connection"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Save"
        >   
            <div>
                <TextInput
                    id="name"
                    name="name"
                    label="Username"
                    description="The user's username."
                    placeholder="Enter username..."
                    readOnly={false}
                    required={false}
                    onChange={handleOnChange('name')}
                    value={newGitHubUser['name']}
                    errorName="name"
                    errorValue={fieldErrors['name']}
                />
                <TextInput
                    id="apiToken"
                    name="apiToken"
                    label="Email"
                    description="The user's email."
                    placeholder="Enter email..."
                    readOnly={false}
                    required={false}
                    onChange={handleOnChange('apiToken')}
                    value={newGitHubUser['apiToken']}
                    errorName="apiToken"
                    errorValue={fieldErrors['apiToken']}
                />
                <TextInput
                    id="timeoutInSeconds"
                    name="timeoutInSeconds"
                    label="Email"
                    description="The user's email."
                    placeholder="Enter email..."
                    readOnly={false}
                    required={false}
                    onChange={handleOnChange('timeoutInSeconds')}
                    value={newGitHubUser['timeoutInSeconds']}
                    errorName="timeoutInSeconds"
                    errorValue={fieldErrors['timeoutInSeconds']}
                />
            </div>
        </Modal>
    );
};

export default GithubAddUserModal;