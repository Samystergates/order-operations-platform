import { privateAxios } from "./helper";

export const loadAllSme = () => {
  return privateAxios
    .get(`/wheels/flow/sme/getAll`)
    .then((response) => response.data);
};

export const loadAllSpu = () => {
  return privateAxios
    .get(`/wheels/flow/spu/getAll`)
    .then((response) => response.data);
};

export const loadAllTra = () => {
  return privateAxios
    .get(`/transport/route/getAll`)
    .then((response) => response.data);
};

export const loadAllStickers = () => {
  return privateAxios
    .get(`/sticker/label/getAll`)
    .then((response) => response.data);
};

export const printingTraPdf = (key) => {
  return privateAxios
    .get(`/transport/route/TRA/printPdf/${key}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};

export const printingMonPdf = (key) => {
  return privateAxios
    .get(`/transport/route/MON/printPdf/${key}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};

export const printingSerPdf = (key) => {
  return privateAxios
    .get(`/ser/printPdf/${key}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};

export const printingSmePdf = (key) => {
  return privateAxios
    .get(`/wheels/flow/sme/printPdf/${key}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};

export const printingSpuPdf = (key) => {
  return privateAxios
    .get(`/wheels/flow/spu/printPdf/${key}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};

export const printingStickerPdf = (key,orderNumber) => {
  return privateAxios
    .get(`/sticker/label/printPdf/${key}/${orderNumber}`, {
      responseType: "blob",
    })
    .then((response) => {
      if (response.status >= 200 && response.status < 300) {
        return response.data;
      } else {
        throw new Error("Network response was not ok");
      }
    })
    .catch((error) => {
      console.log(error);
    });
};
