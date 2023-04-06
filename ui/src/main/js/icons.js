import { library } from '@fortawesome/fontawesome-svg-core';

import {
    faAngleDoubleLeft,
    faAngleDoubleRight,
    faAngleLeft,
    faAngleRight,
    faCheck,
    faCog,
    faHandshake,
    faPencilAlt,
    faPlus,
    faTimes,
    faTrash,
    faUserCog
} from '@fortawesome/free-solid-svg-icons';

export default function registerIcons() {
    library.add(
        faAngleDoubleLeft,
        faAngleDoubleRight,
        faAngleLeft,
        faAngleRight,
        faCheck,
        faCog,
        faHandshake,
        faPencilAlt,
        faPlus,
        faTimes,
        faTrash,
        faUserCog
    );
}
