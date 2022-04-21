import React from 'react';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    table: {
        width: '100%',
        border: 0,
        borderCollapse: 'separate'
    },
    tableHead: {
        backgroundColor: 'blue',
        '& > tr > *': {
            backgroundColor: 'yellow',
            borderBottom: [1, 'solid', 'green'],
            padding: '6px',
            textAlign: 'left',
            zIndex: 1
        }
    }
});

const Table = ({ columns }) => {
    const classes = useStyles();

    return (
        <>
            <table className={classes.table}>
                <thead className={classes.tableHead}>
                    <tr>
                        { columns.map((column, index) => (
                            <th key={index}>
                                <button role="button" onClick={() => {console.log('you clicked me')}}>
                                    {column.label}
                                </button>
                            </th>
                        ))}
                    </tr>
                </thead>
            </table>
        </>
    );
};

export default Table;
