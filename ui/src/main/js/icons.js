import { library } from '@fortawesome/fontawesome-svg-core';

import {
    faCheck,
    faCog,
    faPencilAlt,
    faPlus,
    faTimes,
    faTrash,
    faUserCog
} from '@fortawesome/free-solid-svg-icons';

export default function registerIcons() {
    library.add(
        faCheck,
        faCog,
        faPencilAlt,
        faPlus,
        faTimes,
        faTrash,
        faUserCog
    );
}
