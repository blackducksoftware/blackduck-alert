import React, { useEffect, useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { deleteGithubConfiguration, fetchGithub } from 'store/actions/github';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles({
    deleteConfirmMessage: {
        margin: [0, 0, '20px', '30px'],
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cardContainer: {
        display: 'flex',
        marginLeft: '50px'
    },
    accountCard: {
        display: 'flex',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        backgroundColor: '#e8e6e6',
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    accountIcon: {
        flexBasis: '20%',
        backgroundColor: 'white',
        border: ['1px', 'solid', '#D9D9D9'],
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    accountInfo: {
        flexGrow: 1,
        padding: ['5px', 0, 0, '15px']
    }
});


const GithubAccountDeleteModal = ({ isOpen, toggleModal, data, selected }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [selectedAccounts, setSelectedAccounts] = useState(getStagedForDelete());
    const isMultiAccountDelete = selectedAccounts.length > 1;

    function getStagedForDelete() {
        const staged = data.filter(account => selected.includes(account.id));
        return staged.map(account => ({ ...account, staged: true }));
    }

    useEffect(() => {
        setSelectedAccounts(getStagedForDelete());
    }, [selected]);

    function handleClose() {
        toggleModal(false);
        location.reload();
    }

    function handleDelete() {
        selectedAccounts.forEach((account) => {
            if (account.staged) {
                dispatch(deleteGithubConfiguration(account.id));
            }
        });
        
        handleClose();
    }

    function toggleSelect(selection) {
        const toggledAccounts = selectedAccounts.map((account) => {
            if (account.id === selection.id) {
                return {...account, staged: !account.staged}
            }
            return account;
        });

        setSelectedAccounts(toggledAccounts);
    }

    return (
        <>
            <Modal 
                isOpen={isOpen} 
                size="sm" 
                title={isMultiAccountDelete ? 'Remove Connections' : 'Remove Connection'}
                closeModal={handleClose}
                handleCancel={handleClose}
                handleSubmit={handleDelete}
                submitText="Confirm"
            >
                <div className={classes.deleteConfirmMessage}>
                    { isMultiAccountDelete ? 'Are you sure you want to delete these github account connections?' : 'Are you sure you want to delete this github account connection?' }
                </div>
                <div>
                    { selectedAccounts?.map((account) => {
                        return (
                            <div className={classes.cardContainer}>
                                <input type="checkbox" checked={account.staged} onChange={() => toggleSelect(account)}/>
                                <div className={classes.accountCard}>
                                    <div className={classes.accountIcon}>
                                        <FontAwesomeIcon icon={['fab', 'github']} size="3x"/>
                                    </div>
                                    <div className={classes.accountInfo}>
                                        <div>{account.name}</div>
                                        <div style={{fontSize: '1rem', fontStyle: 'italic'}}>Account Name</div>
                                    </div>
                                </div>
                            </div>
                        )
                    }) }
                </div>
            </Modal>
        </>

    );
};

export default GithubAccountDeleteModal;