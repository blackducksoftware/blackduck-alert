import SelectInput from "../field/input/SelectInput";
import TextInput from "../field/input/TextInput";
import React from "react";
import TextArea from "../field/input/TextArea";
import PasswordInput from "../field/input/PasswordInput";
import NumberInput from "../field/input/NumberInput";
import CheckboxInput from "../field/input/CheckboxInput";
import ReadOnlyField from "../field/ReadOnlyField";

export function getField(fieldType, props) {
    switch (fieldType) {
        case "Select":
            return <SelectInput {...props} />;
        case "TextInput":
            return <TextInput {...props} />;
        case "TextArea":
            return <TextArea {...props} />;
        case "PasswordInput":
            return <PasswordInput {...props} />
        case "NumberInput":
            return <NumberInput {...props} />
        case "CheckboxInput":
            return <CheckboxInput {...props} />
        case "ReadOnlyField":
            return <ReadOnlyField {...props} />
    }
}