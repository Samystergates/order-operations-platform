import { privateAxios } from "./helper";

export const loadAllDepartments = () => {
  return privateAxios.get(`/user/departments`).then((respone) => {
    return respone.data;
  });
};
