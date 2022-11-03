import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import { useForm } from "react-hook-form";
import { createUseStyles } from 'react-jss';
import { postGithubConfiguration } from 'store/actions/github';

const useStyles = createUseStyles({
    formContainer: {
        display: 'flex',
        flexDirection: 'column',
        width: '70%',
        margin: [0, 'auto']
    },
    form: {
        '& div:nth-child(n+2)': {
            marginTop: '20px'
        }
    },
    field: {
        marginBottom: 0,
        fontSize: '14px',
    },
    fieldRequired:{
        marginBottom: 0,
        fontSize: '14px',
        '&::after': {
            content: '"*"',
            color: 'red',
            fontSize: '0.8em',
            marginLeft: '4px'
        }
    },
    input: {
        width: '100%',
        padding: ['8px', '20px'],
        marginTop: '2px',
        display: 'inline-block',
        border: '1px solid #ccc',
        borderRadius: '4px',
        boxSizing: 'border-box'
    }
})

{/* 
Form Fields:
    Name (required): Github Account name
    Api token (required): API token
    Timeout: How long should we wait for the connection to timeout
*/}

const GithubAddUserModal = ({ data, isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const { register, handleSubmit: handleFormSubmit, watch, formState: { errors } } = useForm();
    const onSubmit = (data) => {
        dispatch(postGithubConfiguration(data));
    };

    function handleClose() {
        toggleModal(false);
    }
    
    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Add Github User Connection"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleFormSubmit(onSubmit)}
            submitText="Save"
        >   
            <div className={classes.formContainer}>
                <form className={classes.form}>
                    <div className={classes.inputcontainer}>
                        <label className={classes.fieldRequired}>Username</label>
                        <input className={classes.input} {...register("name", { required: true })} />
                        {errors.name && <span>Github username is required</span>}
                    </div>
                    
                    <div className={classes.inputcontainer}>
                        <label className={classes.fieldRequired}>API Token</label>
                        <input className={classes.input} {...register("apiToken", { required: true })} />
                        {errors.apiToken && <span>API Token is required</span>}
                    </div>

                    <div className={classes.inputcontainer}>
                        <label className={classes.field}>Timeout (in seconds)</label>
                        <input className={classes.input} {...register("timeoutInSeconds")} />
                    </div>

                    {errors.exampleRequired && <span>This field is required</span>}
                    
                </form>
            </div>
        </Modal>
    );
};

export default GithubAddUserModal;