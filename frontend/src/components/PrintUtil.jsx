import {
  printingTraPdf,
  printingMonPdf,
  printingSerPdf,
  printingSmePdf,
  printingSpuPdf,
} from "../services/print-service";


export const printSmeExp = (key) => {

  printingSmePdf(key)
    .then((blob) => {
      const blobUrl = URL.createObjectURL(blob);

      const link = document.createElement("a");

      link.href = blobUrl;
      link.download = "OrderSme ID: " + key + ".pdf";
      link.click();

      URL.revokeObjectURL(blobUrl);
    })
    .catch((error) => {
      console.error("Error fetching or processing PDF:", error);
    });
};

export const printSpuExp = (key) => {
  printingSpuPdf(key)
    .then((blob) => {
      const blobUrl = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = "OrderSpu ID: " + key + ".pdf";
      link.click();

      URL.revokeObjectURL(blobUrl);
    })
    .catch((error) => {
      console.error("Error fetching or processing PDF:", error);
    });
};


export const printMonExp = (key) => {
  printingMonPdf(key)
    .then((blob) => {
      const blobUrl = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = "orderMon ID: " + key + ".pdf";
      link.click();

      URL.revokeObjectURL(blobUrl);
    })
    .catch((error) => {
      console.error("Error fetching or processing PDF:", error);
    });
};

export const printSer = (key) => {
  printingSerPdf(key)
    .then((blob) => {
      const blobUrl = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = "orderSer ID: " + key + ".pdf";
      link.click();

      URL.revokeObjectURL(blobUrl);
    })
    .catch((error) => {
      console.error("Error fetching or processing PDF:", error);
    });
};


export const printTraExp = (key) => {
  printingTraPdf(key)
    .then((blob) => {

      const blobUrl = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = "OrderTra ID: " + key + ".pdf";
      link.click();

      URL.revokeObjectURL(blobUrl);
    })
    .catch((error) => {
      console.error("Error fetching or processing PDF:", error);
    });
}
