import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import ReadOnlyField from 'field/ReadOnlyField';

const CounterField = (props) => {
    const { value, countdown } = props;
    const [currentTime, setCurrentTime] = useState(value);
    const [intervalId, setIntervalId] = useState(null);

    const tickDown = () => {
        const time = currentTime || value;
        const parsedTime = parseInt(time, 10);
        if (!Number.isNaN(parsedTime)) {
            const nextTime = (parsedTime <= 0) ? countdown : (parsedTime - 1);
            setCurrentTime(nextTime.toString());
        }
    };

    useEffect(() => {
        setIntervalId(setInterval(tickDown, 1000));
        return () => {
            clearInterval(intervalId);
        };
    }, []);

    const updatedCount = { ...props, value: currentTime };
    return (
        <div>
            <ReadOnlyField {...updatedCount} />
        </div>
    );
};

CounterField.propTypes = {
    countdown: PropTypes.number.isRequired,
    value: PropTypes.string
};

CounterField.defaultProps = {
    value: ''
};

export default CounterField;
