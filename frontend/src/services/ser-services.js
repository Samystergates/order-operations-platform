import { privateAxios } from "./helper";

export const loadSer = (serDto) => {
    return privateAxios.post(`/ser/get`, serDto)
        .then((response) => response.data);
};

export const loadAllMonteur = () => {
    return privateAxios.get(`/ser/monteur/getAll`)
        .then((response) => response.data);
};

export const loadAllSer = () => {
    return privateAxios.get(`/ser/getAll`)
        .then((response) => response.data);
};

export const saveSer = (serDto) => {
    return privateAxios.post(`/ser/save`, serDto)
        .then((response) => response.data);
};

export const updateSer = (serDto) => {
    return privateAxios.put(`/ser/update`, serDto)
        .then((response) => response.data);
};

export const deleteSer = (serId) => {
    return privateAxios.delete(`/ser/delete/${serId}`)
        .then((response) => response.data);
};
